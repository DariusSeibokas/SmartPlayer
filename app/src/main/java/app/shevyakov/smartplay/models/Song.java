package app.shevyakov.smartplay.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import app.shevyakov.smartplay.MainActivity;

/**
 * Created by Andrey Shevyakov on 04/02/2015.
 */
public class Song {

    MediaMetadataRetriever metaRetriever; // To Retrieve Metadata
    String songPath;

    public Song (String songPath)
    {
        metaRetriever = new MediaMetadataRetriever();

        metaRetriever.setDataSource(songPath);

        this.songPath = songPath;
    }

    @Override
    public String toString ()
    {
        return (getSongArtist() + " - " + getSongTitle());
    }

    public String getSongTitle ()
    {
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (title == null || (title != null && title.isEmpty())) title = "Unknown Title";
        return title;
    }

    public String getSongArtist ()
    {
        String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if (artist == null || (artist != null && artist.isEmpty())) artist = "Unknown Artist";
        return artist;
    }

    public String getSongAlbum ()
    {
        String album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        if (album == null || (album != null && album.isEmpty())) album = "Unknown Album";
        return album;
    }

    public String getSongYear ()
    {
        String year = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        if (year == null || (year != null && year.isEmpty())) year = "Unknown Year";
        return year;
    }

    public String getSongGenre ()
    {

        File ff = new File(getSongPath());
        MusicMetadataSet src_set = null;

        try {
            src_set = new MyID3().read(ff);
        } catch (IOException e) {
            e.printStackTrace();
        }

        IMusicMetadata metadata = src_set.getSimplified();

        String genre = metadata.getGenre();

        if(genre == null)
        {
            try
            {
                genre = metaRetriever.extractMetadata(metaRetriever.METADATA_KEY_GENRE);
                genre = genre.split("\\)")[1];
                if (genre == null || genre == " ") genre = "Unknown Genre";
            }
            catch (Exception e) {genre = "Unknown Genre";}
        }

        return genre;
    }

    public String getSongBitrate ()
    {
        return String.valueOf(Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))/1000);
    }

    public String getSongPath ()
    {
        return songPath;
    }

    // Modes: 0 - Description, 1 - Progress, 2 - Seek Bar Value (Seconds)
    public String getSongLength (int mode)
    {
        long durationMs = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        long duration = durationMs / 1000;
        long m = duration/ 60;
        long s = duration - (m * 60);

        if (mode == 0) return "Duration: "
                + m + "." + ((String.valueOf(s).length() < 2) ? "0".concat(String.valueOf(s)) : s);

        else if (mode == 1) return m + "." + ((String.valueOf(s).length() < 2) ? "0".concat(String.valueOf(s)) : s);

        else if (mode == 2) return String.valueOf(s + (m * 60));

        return null;
    }


    public Bitmap getAlbumCover ()
    {
        ByteArrayInputStream is = new ByteArrayInputStream(metaRetriever.getEmbeddedPicture());
        return BitmapFactory.decodeStream(is);
    }

    public float getShuffleSum ()
    {
        return MainActivity.dbManager.getShuffleSum(this);
    }
}
