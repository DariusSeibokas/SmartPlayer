package app.shevyakov.smartplay.support;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Harmeet Singh, 2013
 */
public class SongsRetriever {

    final String MEDIA_PATH_INTERNAL = Environment.getExternalStorageDirectory().getPath() + "/";
    final String MEDIA_PATH_EXTERNAL = System.getenv("SECONDARY_STORAGE") + "/";
    private ArrayList<String> songsList = new ArrayList<String>();
    private String mp3Pattern = ".mp3";

    public SongsRetriever() {}

    public ArrayList<String> getPlayList() {

        if (MEDIA_PATH_INTERNAL != null) {
            File home = new File(MEDIA_PATH_INTERNAL);
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }

            if (MEDIA_PATH_EXTERNAL != null) {
                File homeEx = new File(MEDIA_PATH_EXTERNAL);
                File[] listFilesEx = homeEx.listFiles();
                if (listFilesEx != null && listFilesEx.length > 0) {
                    for (File file : listFilesEx) {
                        if (file.isDirectory()) {
                            scanDirectory(file);
                        } else {
                            addSongToList(file);
                        }
                    }
                }
            }
        }

        return songsList;
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void addSongToList(File song) {
        if (song.getName().endsWith(mp3Pattern)) {
            songsList.add(song.getPath());
        }
    }
}
