package app.shevyakov.smartplay.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.shevyakov.smartplay.MainActivity;
import app.shevyakov.smartplay.R;
import app.shevyakov.smartplay.models.Song;

/**
 * Created by Andrey Shevyakov on 15/02/2015.
 */
public class ListAdapter extends BaseAdapter implements Swappable {

    boolean search = false;
    List <Song> items;
    Context context;

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public ListAdapter(Context context, List<Song> items, boolean search) {
        this.items = items;
        this.context = context;
        this.search = search;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(R.layout.listrow, null);

        }

        Song s = items.get(position);


        // Changing Item Background In Accordance To Whether The Song Is Playing Or Not
        if (s != null) {
            TextView info = (TextView) v.findViewById(R.id.song_info);
            info.setText(s.toString());

            if (position == MainActivity.currentPlayingPos && search == false) {
                info.setBackgroundResource(R.color.black_bg);
                info.setTextColor(Color.WHITE);
            }

            else {

                    if (MainActivity.colourScheme.equals("BLUE")) {
                            info.setBackgroundResource(R.color.blue_bg);
                    }

                    else if (MainActivity.colourScheme.equals("GREEN")) {
                            info.setBackgroundResource(R.color.green_bg);
                    }

                    else if (MainActivity.colourScheme.equals("GRAY")) {
                            info.setBackgroundResource(R.color.gray_bg);
                    }

                    info.setTextColor(Color.BLACK);
            }
        }

        return v;

    }

    @Override
    public void swapItems(int i, int i2) {
        Collections.swap(items, i, i2);
        if (i2 == MainActivity.currentPlayingPos) MainActivity.currentPlayingPos = i;
        else if (i == MainActivity.currentPlayingPos) MainActivity.currentPlayingPos = i2;
    }
}
