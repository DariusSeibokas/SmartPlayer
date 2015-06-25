package app.shevyakov.smartplay.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.shevyakov.smartplay.MainActivity;
import app.shevyakov.smartplay.R;
import app.shevyakov.smartplay.db.AddUpdSongRemDB;
import app.shevyakov.smartplay.db.RetrieveRemDB;
import app.shevyakov.smartplay.models.Song;

/**
 * Created by Andrey Shevyakov on 18/02/2015.
 */
public class FragmentInfo extends android.support.v4.app.Fragment {

    public static RelativeLayout bgi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.info_fragment, container, false);

        // Setting Background Colour
        bgi = (RelativeLayout) rootView.findViewById(R.id.infoL);

        bgi.setBackgroundResource(
                    MainActivity.colourScheme.equals("BLUE") ? R.color.blue_bg :
                    MainActivity.colourScheme.equals("GREEN") ? R.color.green_bg : R.color.gray_bg);


        // Displaying Song Info
        Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

        ((TextView) rootView.findViewById(R.id.songTitleDesc)).setText("Title: " + in.getSongTitle());
        ((TextView) rootView.findViewById(R.id.songArtistDesc)).setText("Artist: " + in.getSongArtist());
        ((TextView) rootView.findViewById(R.id.songAlbumDesc)).setText("Album: " + in.getSongAlbum());

        ((TextView) rootView.findViewById(R.id.songDurationDesc)).setText(in.getSongLength(0));
        ((TextView) rootView.findViewById(R.id.songYearDesc)).setText("Year: " + in.getSongYear());
        ((TextView) rootView.findViewById(R.id.songGenreDesc)).setText("Genre: " + in.getSongGenre());
        ((TextView) rootView.findViewById(R.id.songBitrateDesc)).setText("Bitrate: " + in.getSongBitrate() + " kbps");

        ImageView albumCover = (ImageView) rootView.findViewById(R.id.albumImageDesc);

        try
        {
            albumCover.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
        }

        catch(Exception ee)
        {
            albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
        }

        RatingBar rb = ((RatingBar) rootView.findViewById(R.id.ratingBar));

        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser == true) {
                    Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

                    int prevRating = MainActivity.dbManager.getSongRating(in);
                    int updChartRating = 0;

                    if (prevRating < (int) rating) updChartRating = (int) rating - prevRating;
                    else updChartRating = -(prevRating - (int) rating);

                    String[] parameters = {"1", in.toString(), String.valueOf(updChartRating), String.valueOf(MainActivity.dbManager.getSongFinished(in))};
                    new AddUpdSongRemDB().execute(parameters);

                    MainActivity.dbManager.updSongRating(MainActivity.playListIn.get(MainActivity.currentPlayingPos), (int) rating);
                }
            }

        });

        rb.setProgress(MainActivity.dbManager.getSongRating(in));

        ((TextView) rootView.findViewById(R.id.songPopularity)).setText(R.string.retr);

        RetrieveRemDB retr = new RetrieveRemDB(new FragmentPlay.FragmentCallback() {
            @Override
            public void onTaskDone(String output) {
                ((TextView) getActivity().findViewById(R.id.songPopularity)).setText("Chart Position: " + output);
            }
        });

        String [] parameters = {in.toString()};
        retr.execute(parameters);

        return rootView;
    }

}
