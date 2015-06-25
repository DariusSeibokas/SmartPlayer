package app.shevyakov.smartplay.fragments;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import java.util.ArrayList;
import java.util.List;

import app.shevyakov.smartplay.MainActivity;
import app.shevyakov.smartplay.R;
import app.shevyakov.smartplay.adapters.ListAdapter;
import app.shevyakov.smartplay.models.Song;

/**
 * Created by Kebab on 01/02/2015.
 */
public class FragmentPlaylist extends android.support.v4.app.ListFragment {

    public static DynamicListView lv;
    public static ToggleButton tb;
    public static LinearLayout bgpl;
    public static int searchMode;
    public List<Integer> searchPositions = new ArrayList<Integer>();

    private SearchView searchField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pll_fragment, container, false);

        setListAdapter(new ListAdapter(getActivity().getApplicationContext(), MainActivity.playListIn, false));

        lv = (DynamicListView) rootView.findViewById(android.R.id.list);

        bgpl = (LinearLayout) rootView.findViewById(R.id.playlistL);

        searchField = ((SearchView) rootView.findViewById(R.id.songSearch));

        searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!searchField.isIconified()) {

                    searchMode = 1;

                    List<Song> searchList = new ArrayList<Song>();
                    searchPositions.clear();

                    for (int i = 0; i < MainActivity.playListIn.size(); i++) {
                        Song temp = MainActivity.playListIn.get(i);
                        String tsInfo = temp.getSongArtist() + " " + temp.getSongTitle();
                        tsInfo = tsInfo.trim();
                        String[] tsInfoA = tsInfo.split(" ");

                        for (int z = 0; z < tsInfoA.length; z++) {
                            if (tsInfoA[z].toUpperCase().startsWith(newText.toUpperCase())) {
                                searchList.add(temp);
                                searchPositions.add(i);
                                break;
                            }
                        }

                    }

                    setListAdapter(new ListAdapter(getActivity().getApplicationContext(), searchList, true));
                    return true;
                }

                return false;
            }
        });



            bgpl.setBackgroundResource(MainActivity.colourScheme.equals("BLUE") ? R.color.blue_bg :
                            MainActivity.colourScheme.equals("GREEN") ? R.color.green_bg : R.color.gray_bg);


        lv.enableDragAndDrop();

        lv.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                                   final int position, final long id) {
                        lv.startDragging(position);
                        return true;
                    }
                }
        );

        tb = (ToggleButton)rootView.findViewById(R.id.toggleDelete);

        return rootView;

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if (!tb.isChecked()) {
            Song pass = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

            MainActivity.dbManager.updSongFinished(pass);
            MainActivity.dbManager.updSongAvgLTime(pass, FragmentPlay.listeningTime);

            FragmentPlay.listeningTime = 0;

            ((BaseAdapter) FragmentPlaylist.lv.getAdapter()).notifyDataSetChanged();

            if (searchMode == 0) MainActivity.currentPlayingPos = position;
            else {
                MainActivity.currentPlayingPos = searchPositions.get(position);
                searchField.clearFocus();
                searchField.setQuery("", false);
                searchField.setIconified(true);
                FragmentPlaylist.searchMode = 0;
                FragmentPlaylist.lv.setAdapter(new ListAdapter(getActivity().getApplicationContext(), MainActivity.playListIn, false));
            }

            Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

            ImageView play = (ImageView) getActivity().findViewById(R.id.playButton);
            play.setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));

            TextView songDesc = (TextView) getActivity().findViewById(R.id.songDescriptionPlay);
            songDesc.setText(in.toString());

            MainActivity.mp.stop();
            MainActivity.mp = MediaPlayer.create(getView().getContext(), Uri.parse(in.getSongPath()));
            MainActivity.mp.start();

            TextView songLength = (TextView) getActivity().findViewById(R.id.songLength);

            songLength.setText(in.getSongLength(1));

            SeekBar songProgress = (SeekBar) getActivity().findViewById(R.id.songProgressBar);

            songProgress.setProgress(0);

            songProgress.setMax(Integer.parseInt(in.getSongLength(2)));


            ImageView albumCover = (ImageView) getActivity().findViewById(R.id.albumImage);

            try {
                albumCover.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
            } catch (Exception ee) {
                albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
            }

            MainActivity.dbManager.updSongClicked(in);

            updInfo(in);
        }

        else {
            if (position != MainActivity.currentPlayingPos) {
                MainActivity.dbManager.deleteSongFromLib(MainActivity.playListIn.get(position));
                MainActivity.playListIn.remove(position);

                if (MainActivity.currentPlayingPos != 0 && MainActivity.currentPlayingPos > position) MainActivity.currentPlayingPos-=1;

                ((BaseAdapter) FragmentPlaylist.lv.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    public void updInfo (Song in)
    {
        ((TextView) getActivity().findViewById(R.id.songTitleDesc)).setText("Title: " + in.getSongTitle());
        ((TextView) getActivity().findViewById(R.id.songArtistDesc)).setText("Artist: " + in.getSongArtist());
        ((TextView) getActivity().findViewById(R.id.songAlbumDesc)).setText("Album: " + in.getSongAlbum());

        ((TextView) getActivity().findViewById(R.id.songDurationDesc)).setText(in.getSongLength(0));
        ((TextView) getActivity().findViewById(R.id.songYearDesc)).setText("Year: " + in.getSongYear());
        ((TextView) getActivity().findViewById(R.id.songGenreDesc)).setText("Genre: " + in.getSongGenre());
        ((TextView) getActivity().findViewById(R.id.songBitrateDesc)).setText("Bitrate: " + in.getSongBitrate() + " kbps");

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
}
