package app.shevyakov.smartplay.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import app.shevyakov.smartplay.MainActivity;
import app.shevyakov.smartplay.R;
import app.shevyakov.smartplay.db.RetrieveRemDB;
import app.shevyakov.smartplay.models.Song;

/**
 * Created by Andrey Shevyakov on 01/02/2015.
 */
public class FragmentPlay extends android.support.v4.app.Fragment {

    private Handler progHandler = new Handler();
    private Handler shuffleHandler = new Handler();
    private int shuffleInARow;
    private boolean isPlaying = true;
    public static RelativeLayout bgp;
    public static int listeningTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pl_fragment, container, false);

        final ImageView play = (ImageView) rootView.findViewById(R.id.playButton);
        final ImageView prev = (ImageView) rootView.findViewById(R.id.prevButton);
        final ImageView next = (ImageView) rootView.findViewById(R.id.nextButton);
        final ImageView shuffle = (ImageView) rootView.findViewById(R.id.shuffleButton);
        final ImageView albumCover = (ImageView) rootView.findViewById(R.id.albumImage);

        final TextView songLength = (TextView) rootView.findViewById(R.id.songLength);
        final TextView songProgressCounter = (TextView) rootView.findViewById(R.id.songProgressCounter);
        final TextView songDesc = (TextView)rootView.findViewById(R.id.songDescriptionPlay);

        final SeekBar songProgress = (SeekBar) rootView.findViewById(R.id.songProgressBar);


        // Setting Background Colour
        bgp = (RelativeLayout) rootView.findViewById(R.id.playL);


        bgp.setBackgroundResource(
                MainActivity.colourScheme.equals("BLUE") ? R.color.blue_bg :
                        MainActivity.colourScheme.equals("GREEN") ? R.color.green_bg : R.color.gray_bg);


        songDesc.setSelected(true);

        // Changing Song Position Using Seek Bar
        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

              if (fromUser == true)
              {
                  MainActivity.mp.seekTo(songProgress.getProgress()*1000);

                  int durationMs = MainActivity.mp.getCurrentPosition();
                  int duration = durationMs / 1000;
                  int m = duration/ 60;
                  int s = duration - (m * 60);

                  songProgressCounter.setText(m + "." + ((String.valueOf(s).length() < 2) ? "0" + s : s));

                  if (isPlaying == false)
                  {
                      play.setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));
                      isPlaying = true;
                      MainActivity.mp.start();
                  }
              }
            }
        });

        // Shuffle Button Click
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shuffle.setClickable(false);

                Toast.makeText(getActivity().getApplicationContext(), "Shuffled!", Toast.LENGTH_LONG).show();

                if (shuffleInARow > 3) Collections.shuffle(MainActivity.playListIn);
                else smartShuffle(MainActivity.playListIn);

                ((BaseAdapter) FragmentPlaylist.lv.getAdapter()).notifyDataSetChanged();

                songChange(0, songProgressCounter, play, songLength,
                            songProgress, albumCover, songDesc);

                shuffleInARow = 0;

                Runnable shufflePause = new Runnable() {
                    @Override
                    public void run() {
                        shuffle.setClickable(true);
                    }
                };

                shuffleHandler.postDelayed(shufflePause, 5000);

            }
        });

        // Play Button Click (Pause / Unpause)
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(MainActivity.mp.isPlaying()){
                    play.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
                    isPlaying = false;
                    MainActivity.mp.pause();
                } else {
                    play.setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));
                    isPlaying = true;
                    MainActivity.mp.start();
                }

            }
        });

        // Next Song Button Click
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                if (listeningTime >= 5) {

                    Song pass = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

                    MainActivity.dbManager.updSongSkipped(pass);
                    MainActivity.dbManager.updSongAvgLTime(pass, listeningTime);
                }

                songChange (1, songProgressCounter, play, songLength,
                            songProgress, albumCover, songDesc);

            }
        });

        // Previous Song Button Click
        prev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                songChange (-1, songProgressCounter, play, songLength,
                            songProgress, albumCover, songDesc);
            }
        });

        // Called When The App Is Firstly Run
        init (rootView, songLength, songProgress, songDesc);

        // Setting Album Imagery
        try
        {
            albumCover.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
        }

        catch(Exception ee)
        {
            albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
        }

        // Updating Seek Bar According To Song Position
        Runnable progUpd = new Runnable() {
            @Override
            public void run() {
      /* do what you need to do */
                    if (songProgress.getProgress() < songProgress.getMax() && isPlaying == true) {
                        listeningTime++;

                        int durationMs = MainActivity.mp.getCurrentPosition();
                        int duration = durationMs / 1000;
                        int m = duration/ 60;
                        int s = duration - (m * 60);

                        songProgressCounter.setText(m + "." + ((String.valueOf(s).length() < 2) ? "0" + s : s));
                        songProgress.setProgress(songProgress.getProgress() + 1);
                    }

                    // Changing Song When Seek Bar Is Full
                    else if (songProgress.getProgress() == songProgress.getMax() && isPlaying == true) {

                        Song pass = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

                        MainActivity.dbManager.updSongFinished(pass);
                        MainActivity.dbManager.updSongAvgLTime(pass, listeningTime);

                        songChange (1, songProgressCounter, play, songLength,
                                songProgress, albumCover, songDesc);


                    }

                progHandler.postDelayed(this, 1000);
            }
        };

        progHandler.post(progUpd);

        return rootView;
    }

    // Changing Song
    public void songChange (int dir, TextView songProgressCounter, ImageView play, TextView songLength,
                            SeekBar songProgress, ImageView albumCover, TextView songDesc)
    {
        songProgressCounter.setText("0.00");
        listeningTime = 0;
        shuffleInARow = 0;

        // dir values: next - 1, prev - -1, shuffle - 0
        if (dir == 1)
        {
            if (MainActivity.currentPlayingPos < MainActivity.playListIn.size() - 1) {
                MainActivity.currentPlayingPos++;
            }
            else
            {
                MainActivity.currentPlayingPos = 0;
            }
        }

        else if (dir == -1)
        {
            if (MainActivity.currentPlayingPos > 0) {
                MainActivity.currentPlayingPos--;
            }
            else
            {
                MainActivity.currentPlayingPos = MainActivity.playListIn.size() - 1;
            }
        }

        else if (dir == 0) MainActivity.currentPlayingPos = 0;

        Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

        MainActivity.mp.stop();
        MainActivity.mp = MediaPlayer.create(getView().getContext(), Uri.parse(in.getSongPath()));
        MainActivity.mp.start();

        isPlaying = true;

        play.setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));

        ((BaseAdapter)FragmentPlaylist.lv.getAdapter()).notifyDataSetChanged();

        songDesc.setText(in.toString());

        songLength.setText(in.getSongLength(1));

        songProgress.setProgress(0);

        songProgress.setMax(Integer.parseInt(in.getSongLength(2)));

        try
        {
            albumCover.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
        }

        catch(Exception ee)
        {
            albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
        }

        updInfo (in);

    }

    // Initialize The Very First Song
    public void init (View rootView, TextView songLength, SeekBar songProgress, TextView songDesc)
    {
        Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

        songDesc.setText(in.toString());

        songLength.setText(in.getSongLength(1));

        songProgress.setProgress(0);

        songProgress.setMax(Integer.parseInt(in.getSongLength(2)));

    }

    // Update Song Info In FragmentInfo
    public void updInfo (Song in)
    {
            ((TextView) getActivity().findViewById(R.id.songTitleDesc)).setText("Title: " + in.getSongTitle());
            ((TextView) getActivity().findViewById(R.id.songArtistDesc)).setText("Artist: " + in.getSongArtist());
            ((TextView) getActivity().findViewById(R.id.songAlbumDesc)).setText("Album: " + in.getSongAlbum());

            ((TextView) getActivity().findViewById(R.id.songDurationDesc)).setText(in.getSongLength(0));
            ((TextView) getActivity().findViewById(R.id.songYearDesc)).setText("Year: " + in.getSongYear());
            ((TextView) getActivity().findViewById(R.id.songGenreDesc)).setText("Genre: " + in.getSongGenre());
            ((TextView) getActivity().findViewById(R.id.songBitrateDesc)).setText("Bitrate: " + in.getSongBitrate() + " kbps");

            ((TextView) getActivity().findViewById(R.id.songPopularity)).setText(R.string.retr);

            RetrieveRemDB retr = new RetrieveRemDB(new FragmentCallback() {
            @Override
            public void onTaskDone(String output) {
                ((TextView) getActivity().findViewById(R.id.songPopularity)).setText("Chart Position: " + output);
            }
            });

            String [] parameters = {in.toString()};
            retr.execute(parameters);

            ((RatingBar) getActivity().findViewById(R.id.ratingBar)).setProgress(MainActivity.dbManager.getSongRating(in));

            ImageView albumCover = (ImageView) getActivity().findViewById(R.id.albumImageDesc);

            try
            {
                albumCover.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
            }

            catch(Exception ee)
            {
                albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
            }
    }

    // Smart Shuffle Algorithm
    private void smartShuffle (List <Song> playListForShuffle)
    {
        shuffleInARow++;

        // Sort Accordingly To Shuffle Sum
        Collections.sort(playListForShuffle, new Comparator<Song>() {
            @Override
            public int compare(Song s1, Song s2) {
                float sum1 = s1.getShuffleSum();
                float sum2 = s2.getShuffleSum();
                if (sum1 > sum2) return 1;
                if (sum2 < sum1) return -1;
                if (sum1 == sum2) return 1;
                return -1;
            }
        });

        Collections.reverse(playListForShuffle); // Top Songs First

        // Randomize The Result A Bit
        for (int i = 0; i < playListForShuffle.size(); i++)
        {
           int rand = new Random().nextInt(10);

           // Place Songs Not Yet Listened To At Random Positions In The Playlist
           if (playListForShuffle.get(i).getShuffleSum() == 0)
           {
               int newPos = new Random().nextInt(playListForShuffle.size());
               Collections.swap(playListForShuffle, i, newPos);
           }

           if (rand == 1)
           {
               if (i < playListForShuffle.size() - 1)
               {
                   Collections.swap(playListForShuffle, i, i+1);
               }
           }

            else if (rand == 2)
           {
               if (i > 0)
               {
                   Collections.swap(playListForShuffle, i, i-1);
               }
           }

           else if (rand == 3)
           {
               if (i > 1)
               {
                   Collections.swap(playListForShuffle, i, i-2);
               }
           }

           else if (rand == 4)
           {
               if (i < playListForShuffle.size() - 2)
               {
                   Collections.swap(playListForShuffle, i, i+2);
               }
           }

           else if (rand == 5)
           {
               int newPos = new Random().nextInt(playListForShuffle.size());
               Collections.swap(playListForShuffle, i, newPos);
           }

        }

    }

    public interface FragmentCallback {
        public void onTaskDone(String output);
    }
}
