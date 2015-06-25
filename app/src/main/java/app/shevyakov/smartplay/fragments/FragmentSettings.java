package app.shevyakov.smartplay.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import app.shevyakov.smartplay.MainActivity;
import app.shevyakov.smartplay.R;
import app.shevyakov.smartplay.adapters.ListAdapter;
import app.shevyakov.smartplay.db.RetrieveRemDB;
import app.shevyakov.smartplay.models.Song;

/**
 * Created by Andrey Shevyakov on 19/02/2015.
 */
public class FragmentSettings extends android.support.v4.app.Fragment {

    public static RelativeLayout bgs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_fragment, container, false);

        final EditText minLengthSet = (EditText) rootView.findViewById(R.id.settings_minLengthVal);
        minLengthSet.append(String.valueOf(MainActivity.minSongDur));

        // Minimum Song Length Validation
        minLengthSet.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try
                {
                    if (s.length() > 3) {
                        minLengthSet.setText(null);
                    }

                    else if (s.toString().contains("-")) {
                        minLengthSet.setText(null);
                        minLengthSet.append(String.valueOf((Integer.parseInt(s.toString()) * -1)));
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Setting Minimum Length For A Song
        minLengthSet.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!minLengthSet.getText().toString().isEmpty() && Integer.parseInt(minLengthSet.getText().toString()) < MainActivity.songWMaxDur)
                    {
                        setMinLength (minLengthSet);
                    }

                    else if (!minLengthSet.getText().toString().isEmpty() && Integer.parseInt(minLengthSet.getText().toString()) >= MainActivity.songWMaxDur) {
                        minLengthSet.setText(String.valueOf(MainActivity.songWMaxDur - 1));
                        setMinLength (minLengthSet);
                    }

                    else if (minLengthSet.getText().toString().isEmpty())
                    {
                        minLengthSet.setText(String.valueOf(MainActivity.minSongDur));
                    }
                    return false;
                }
                return false;
            }
        });

            Button refLib = (Button) rootView.findViewById(R.id.refresh_music_lib_button);

            refLib.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

                    builder.setTitle("Confirm");
                    builder.setMessage("Are you sure?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
                            builder.setTitle("Confirm");
                            builder.setMessage("Do you want to restore all deleted tracks as well?");

                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refLib(true);
                                }
                            });

                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refLib(false);
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();

                            dialog.dismiss();

                        }

                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });


            bgs =(RelativeLayout)rootView.findViewById(R.id.settingsL);

            // Setting Background Colour
            bgs.setBackgroundResource(MainActivity.colourScheme.equals("BLUE") ? R.color.blue_bg :
                            MainActivity.colourScheme.equals("GREEN") ? R.color.green_bg : R.color.gray_bg);

            Button selectCS = (Button) rootView.findViewById(R.id.change_colour_scheme_button);

            final ArrayAdapter<String> dAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                    R.layout.dialog_item,
                    getActivity().getApplicationContext().getResources().getStringArray(R.array.colour_schemes));


            // Changing App Colour Scheme
            selectCS.setOnClickListener(new View.OnClickListener()

            {

                @Override
                public void onClick (View w){
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                        .setTitle("Select Preferred Color Scheme")
                        .setAdapter(dAdapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) 
                                MainActivity.colourScheme = "BLUE";
                            
                                else if (which == 1) 
                                MainActivity.colourScheme = "GREEN";
                            
                                else if (which == 2) 
                                MainActivity.colourScheme = "GRAY";

                                FragmentPlaylist.lv.setDivider((Drawable) getResources().getDrawable(which == 0 ? R.color.list_blue_divider : which == 1 ? R.color.list_green_divider : android.R.color.black));
                                FragmentPlaylist.lv.setDividerHeight(1);


                                    FragmentPlay.bgp.setBackgroundResource(which == 0 ? R.color.blue_bg : which == 1 ? R.color.green_bg : R.color.gray_bg);
                                    FragmentPlaylist.bgpl.setBackgroundResource(which == 0 ? R.color.blue_bg : which == 1 ? R.color.green_bg : R.color.gray_bg);
                                    FragmentInfo.bgi.setBackgroundResource(which == 0 ? R.color.blue_bg : which == 1 ? R.color.green_bg : R.color.gray_bg);
                                    bgs.setBackgroundResource(which == 0 ? R.color.blue_bg : which == 1 ? R.color.green_bg : R.color.gray_bg);


                                ((BaseAdapter) FragmentPlaylist.lv.getAdapter()).notifyDataSetChanged();

                                MainActivity.editor.putString("colour_scheme", MainActivity.colourScheme);
                                MainActivity.editor.commit();

                                dialog.dismiss();
                            }
                        }).create().show();
            }
            }

            );

            return rootView;

        }

        // Redisplaying The App Data After The Minimum Song Length Has Been Changed
        public void guiRefresh()
        {
            Song in = MainActivity.playListIn.get(MainActivity.currentPlayingPos);

            ((TextView) getActivity().findViewById(R.id.songPopularity)).setText(R.string.retr);

            RetrieveRemDB retr = new RetrieveRemDB(new FragmentPlay.FragmentCallback() {
                @Override
                public void onTaskDone(String output) {
                    ((TextView) getActivity().findViewById(R.id.songPopularity)).setText("Chart Position: " + output);
                }
            });

            String [] parameters = {in.toString()};
            retr.execute(parameters);

            ((TextView) getActivity().findViewById(R.id.songTitleDesc)).setText("Title: " + in.getSongTitle());
            ((TextView) getActivity().findViewById(R.id.songArtistDesc)).setText("Artist: " + in.getSongArtist());
            ((TextView) getActivity().findViewById(R.id.songAlbumDesc)).setText("Album: " + in.getSongAlbum());

            ((TextView) getActivity().findViewById(R.id.songDurationDesc)).setText(in.getSongLength(0));
            ((TextView) getActivity().findViewById(R.id.songYearDesc)).setText("Year: " + in.getSongYear());
            ((TextView) getActivity().findViewById(R.id.songGenreDesc)).setText("Genre: " + in.getSongGenre());
            ((TextView) getActivity().findViewById(R.id.songBitrateDesc)).setText("Bitrate: " + in.getSongBitrate() + " kbps");

            ((RatingBar) getActivity().findViewById(R.id.ratingBar)).setProgress(MainActivity.dbManager.getSongRating(in));

            ImageView albumCoverInf = (ImageView) getActivity().findViewById(R.id.albumImageDesc);
            ImageView albumCoverPlay = (ImageView) getActivity().findViewById(R.id.albumImage);

            try
            {
                albumCoverInf.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
                albumCoverPlay.setImageBitmap(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getAlbumCover());
            }

            catch(Exception ee)
            {
                albumCoverInf.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
                albumCoverPlay.setImageDrawable(getResources().getDrawable(R.drawable.cover_placeholder));
            }

            // Refreshing FragmentPlay

            ((TextView) getActivity().findViewById(R.id.songDescriptionPlay)).setText(in.toString());

            ((TextView) getActivity().findViewById(R.id.songLength)).setText(in.getSongLength(1));

            ((SeekBar) getActivity().findViewById(R.id.songProgressBar)).setProgress(0);

            ((SeekBar) getActivity().findViewById(R.id.songProgressBar)).setMax(Integer.parseInt(in.getSongLength(2)));

            ((ImageView) getActivity().findViewById(R.id.playButton)).setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));

            // Refreshing Playlist
            FragmentPlaylist.lv.setAdapter(new ListAdapter(getActivity().getApplicationContext(), MainActivity.playListIn, false));
        }

        private void setMinLength (EditText minLengthSet)
    {
        MainActivity.mp.stop();

        // Refresh Playlist Accordingly
        MainActivity.minSongDur = Integer.parseInt(minLengthSet.getText().toString());
        MainActivity.populatePlayList(false);
        MainActivity.currentPlayingPos = 0;

        // Refresh GUI
        guiRefresh();

        // Restart Music
        MainActivity.mp = MediaPlayer.create(getActivity().getApplicationContext(), Uri.parse(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getSongPath()));
        MainActivity.mp.start();

        MainActivity.editor.putInt("min_dur", MainActivity.minSongDur);
        MainActivity.editor.commit();
    }

    public void refLib (boolean delR)
    {
        if (delR == true) MainActivity.dbManager.restoreDeletedSongs();
        MainActivity.mp.stop();
        MainActivity.populatePlayList(true);
        guiRefresh();
        MainActivity.mp = MediaPlayer.create(getActivity().getApplicationContext(), Uri.parse(MainActivity.playListIn.get(MainActivity.currentPlayingPos).getSongPath()));
        MainActivity.mp.start();
    }
    }
