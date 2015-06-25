package app.shevyakov.smartplay;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import app.shevyakov.smartplay.adapters.FragmentAdapter;
import app.shevyakov.smartplay.adapters.ListAdapter;
import app.shevyakov.smartplay.db.AddUpdSongRemDB;
import app.shevyakov.smartplay.db.DBManager;
import app.shevyakov.smartplay.fragments.FragmentInfo;
import app.shevyakov.smartplay.fragments.FragmentPlay;
import app.shevyakov.smartplay.fragments.FragmentPlaylist;
import app.shevyakov.smartplay.fragments.FragmentSettings;
import app.shevyakov.smartplay.models.Song;
import app.shevyakov.smartplay.support.SongsRetriever;
/**
 * Created by Andrey Shevyakov on 01/02/2015.
 */
public class MainActivity extends FragmentActivity {

    public static final String PREFS_NAME = "SmartPlayPrefs"; // Preferences To Store Current Colour Scheme

    public static ViewPager viewPager;
    public static List<Fragment> fragments;
    public static MediaPlayer mp;
    public static List<Song> playListInOrg = new ArrayList();
    public static List<Song> playListIn = new ArrayList();
    public static int currentPlayingPos = 0;
    public static String colourScheme = "BLUE";
    public static int minSongDur; // If Song Length Is More Than minSongDur Than It Is Not Displayed
    public static int songWMaxDur; // Song With Maximum Duration, Used For Minimum Song Duration Change Validation
    public static DBManager dbManager;
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();

        String storedC = prefs.getString("colour_scheme", null);
        int storedMinDur = prefs.getInt("min_dur", -1);

        if (storedC == null) colourScheme = "BLUE";
        else colourScheme = storedC;

        if (storedMinDur == -1) minSongDur = 15;
        else minSongDur = storedMinDur;

        try {
            dbManager = new DBManager(this);
            dbManager.open();

            populatePlayList(false);

            Collections.shuffle(playListIn);

            setContentView(R.layout.activity_main);

            fragments = new Vector<Fragment>();
            fragments.add(Fragment.instantiate(this, FragmentInfo.class.getName()));
            fragments.add(Fragment.instantiate(this, FragmentPlay.class.getName()));
            fragments.add(Fragment.instantiate(this, FragmentPlaylist.class.getName()));
            fragments.add(Fragment.instantiate(this, FragmentSettings.class.getName()));

            viewPager = (ViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), fragments));

            viewPager.setCurrentItem(1);
            viewPager.setOffscreenPageLimit(3);

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrollStateChanged(int state) {}
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    SearchView searchView = (SearchView) findViewById(R.id.songSearch);
                    searchView.clearFocus();
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    FragmentPlaylist.searchMode = 0;
                    FragmentPlaylist.lv.setAdapter(new ListAdapter(getApplicationContext(), MainActivity.playListIn, false));
                    if (FragmentPlaylist.tb.isChecked()) FragmentPlaylist.tb.toggle();
                }

            });

            mp = MediaPlayer.create(this, Uri.parse(playListIn.get(currentPlayingPos).getSongPath()));
            mp.start();
        }

        catch (Exception e) {
            e.printStackTrace();
            setContentView(R.layout.no_music);
        }

    }

    public static void populatePlayList (boolean refLib) {

    if (!playListIn.isEmpty()) playListIn = new ArrayList<Song>();

    if (dbManager.getAllSongs().isEmpty() || refLib == true) {

        SongsRetriever sr = new SongsRetriever();

        ArrayList plist = sr.getPlayList();


        for (int i = 0; i < plist.size(); i++) {
            Song in = new Song(plist.get(i).toString());

            String [] parameters = {"0",in.toString(),"0","0"};
            new AddUpdSongRemDB().execute(parameters);

            if (dbManager.getSongLibraryStatus(in) == 0) {
                dbManager.addSong(in);
                playListIn.add(in);
            }
        }

    }

    else
    {
        ArrayList <String> songPaths = dbManager.getAllSongs();

        for (int i = 0; i < songPaths.size(); i++) {
            try { playListIn.add(new Song(songPaths.get(i)));}
            catch (Exception e) {}
        }

    }

    playListInOrg = playListIn;
    minDurSet();

    }

    public static void minDurSet() {

        playListIn = playListInOrg;

        for (int i = 0; i < playListIn.size(); i++) {
            if (Integer.parseInt(playListIn.get(i).getSongLength(2)) > songWMaxDur)
                songWMaxDur = Integer.parseInt(playListIn.get(i).getSongLength(2));
        }

        for (int i = 0; i < playListIn.size(); i++) {
            if (Integer.parseInt(playListIn.get(i).getSongLength(2)) < minSongDur) {
                playListIn.remove(i);
                i = i - 1;
            }
        }

    }

    @Override
    protected void onDestroy() {

        dbManager.close();
        super.onDestroy();
    }

}