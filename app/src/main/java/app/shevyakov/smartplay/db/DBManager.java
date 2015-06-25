package app.shevyakov.smartplay.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Random;

import app.shevyakov.smartplay.models.Song;

public class DBManager {

    private SQLiteDatabase database;
    private DBDesigner dbHelper;

    public DBManager(Context context) {
        dbHelper = new DBDesigner(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
    }

    public void addSong(Song in) {

        ContentValues values = new ContentValues();

        String path = in.getSongPath();
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        String genre = in.getSongGenre();

        int duration = Integer.parseInt(in.getSongLength(2));
        int del = 0;

        Boolean songExists = checkIfSongExists(title, artist, duration);

        if (songExists == false) {

            values.put(DBDesigner.COLUMN_SONG_PATH, path);
            values.put(DBDesigner.COLUMN_SONG_TITLE, title);
            values.put(DBDesigner.COLUMN_SONG_ARTIST, artist);
            values.put(DBDesigner.COLUMN_SONG_GENRE, genre);
            values.put(DBDesigner.COLUMN_SONG_DURATION, Integer.parseInt(in.getSongLength(2)));
            values.put(DBDesigner.COLUMN_SONG_DELETED, del);

            database.insert(DBDesigner.TABLE_SONGS, null, values);

            Boolean genreExists = checkIfGenreExists(genre);

            if (genreExists == false) {
                values = new ContentValues();
                values.put(DBDesigner.COLUMN_GENRE_TITLE, genre);

                database.insert(DBDesigner.TABLE_GENRES, null, values);
            }
        }

    }

    public void deleteSongFromLib(Song in) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        values.put(DBDesigner.COLUMN_SONG_DELETED, 1);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

    }

    public void restoreDeletedSongs() {
        ContentValues values = new ContentValues();

        values.put(DBDesigner.COLUMN_SONG_DELETED, 0);

        database.update(DBDesigner.TABLE_SONGS, values, null, null);

    }

    public void updSongRating(Song in, int rating) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        values.put(DBDesigner.COLUMN_SONG_RATING, rating);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist +
                "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);
    }

    public void updSongSkipped(Song in) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int timesSkippedS = getSongSkipped(in) + 1;

        values.put(DBDesigner.COLUMN_SONG_TIMES_SKIPPED, timesSkippedS);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist +
                "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);
    }

    public void updSongFinished(Song in) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int timesFinishedS = getSongFinished(in) + 1;

        String [] parameters = {"1", in.toString(), String.valueOf(getSongRating(in)), "1"};
        new AddUpdSongRemDB().execute(parameters);

        values.put(DBDesigner.COLUMN_SONG_TIMES_FINISHED, timesFinishedS);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

    }

    public void updSongClicked(Song in) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int timesClickedS = getSongClicked(in) + 1;

        values.put(DBDesigner.COLUMN_SONG_TIMES_CLICKED, timesClickedS);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

    }

    // Update Average Song Listening Time
    public void updSongAvgLTime(Song in, int ltime) {
        ContentValues values = new ContentValues();

        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int avgLTime = getSongAvgLTime(in);
        if (avgLTime != 0) avgLTime = (avgLTime + ltime) / 2;
        if (avgLTime == 0) avgLTime = ltime;

        values.put(DBDesigner.COLUMN_SONG_AVG_LISTENING_TIME, avgLTime);

        database.update(DBDesigner.TABLE_SONGS, values, DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

    }

    // Check If The Song Is Deleted Or Not
    public int getSongLibraryStatus (Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int returnVal = 0;

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_DELETED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast())

        {
            returnVal = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return returnVal;
    }

    public int getSongRating (Song in)
    {
    String title = in.getSongTitle();
    String artist = in.getSongArtist();
    int duration = Integer.parseInt(in.getSongLength(2));

    int returnVal = 0;

    Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_RATING + " FROM "
            + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
            + " LIKE '" + title +
            "' AND "
            + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
            + "' AND "
            + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

    cursor.moveToFirst();

    while(!cursor.isAfterLast())

    {
        returnVal = cursor.getInt(0);
        cursor.moveToNext();
    }

    cursor.close();

    return returnVal;
    }

    public int getSongSkipped (Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int returnVal = 0;

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_SKIPPED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            returnVal = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return returnVal;
    }

    public int getSongClicked(Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int returnVal = 0;

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_CLICKED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            returnVal = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return returnVal;
    }

    public int getSongFinished(Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int returnVal = 0;

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_FINISHED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            returnVal = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return returnVal;
    }

    //Get Average Song Listening Time
    public int getSongAvgLTime(Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();
        int duration = Integer.parseInt(in.getSongLength(2));

        int returnVal = 0;

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_AVG_LISTENING_TIME + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            returnVal = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return returnVal;
    }

    // The Larger Shuffle Sum Is The More Likable The Song Is By The User
    public float getShuffleSum(Song in)
    {
        String title = in.getSongTitle();
        String artist = in.getSongArtist();

        float duration = Float.parseFloat(in.getSongLength(2));

        float avgListeningTime = 0;
        float finished = 0;
        float skipped = 0;
        float rating = 0;

        int clicked = 0;

        float returnVal;

        // Retrieving Average Listening Time
        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_AVG_LISTENING_TIME + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            avgListeningTime = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        // Retrieving Times Song Been Clicked
        cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_CLICKED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            clicked = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        // Retrieving Times Song Been Finished
        cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_FINISHED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            finished = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        // Retrieving Times Song Been Skipped
        cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TIMES_SKIPPED + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            skipped = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        // Retrieving Song Ration
        cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_RATING + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_TITLE
                + " LIKE '" + title +
                "' AND "
                + DBDesigner.COLUMN_SONG_ARTIST + " LIKE '" + artist
                + "' AND "
                + DBDesigner.COLUMN_SONG_DURATION + " = " + duration, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            rating = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        // Calculating Shuffle Sum (
        float avgLTimeP = (avgListeningTime/duration)*100; // Average Listening Time Percentage
        float avgFTimeP; // Average Finishing Percentage
        if (finished != 0) avgFTimeP = 100 - ((skipped/finished)*100);
        else avgFTimeP = 0;

        float noRating = clicked + avgLTimeP + avgFTimeP;

        if (new Random().nextInt(2) + 1 == 1 && rating == 0 && noRating == 0) returnVal = 9999; // A Chance To Place A New Song On Top
        else
        {
            if (rating != 0)
                returnVal = noRating * rating;
            else
                returnVal = noRating;
        }

        return returnVal;
    }

    // Retrieve All Song Paths
    public ArrayList<String> getAllSongs()
    {

        ArrayList <String> songPaths = new ArrayList<String>();

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_PATH + " FROM "
                + DBDesigner.TABLE_SONGS + " WHERE " + DBDesigner.COLUMN_SONG_DELETED + " = 0", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            songPaths.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();

        return songPaths;
    }

    private Boolean checkIfSongExists(String title, String artist, int duration)
    {

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_SONG_TITLE
                + ", "
                + DBDesigner.COLUMN_SONG_ARTIST
                + ", "
                + DBDesigner.COLUMN_SONG_DURATION
                + " FROM "
                + DBDesigner.TABLE_SONGS, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if(cursor.getString(0).equalsIgnoreCase(title)
               && cursor.getString(1).equalsIgnoreCase(artist)
               && cursor.getInt(2) == duration) return true;
            cursor.moveToNext();
        }

        return false;
    }

    private Boolean checkIfGenreExists(String genre)
    {

        Cursor cursor = database.rawQuery("SELECT " + DBDesigner.COLUMN_GENRE_TITLE
                + " FROM "
                + DBDesigner.TABLE_GENRES, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if(cursor.getString(0).equalsIgnoreCase(genre)) return true;
            cursor.moveToNext();
        }

        return false;
    }

}
