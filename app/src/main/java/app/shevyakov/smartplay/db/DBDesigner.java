package app.shevyakov.smartplay.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBDesigner extends SQLiteOpenHelper {

    public static final String TABLE_SONGS = "table_songs";
    public static final String COLUMN_SONG_ID = "s_id";
    public static final String COLUMN_SONG_PATH = "s_path";
    public static final String COLUMN_SONG_TITLE = "s_title";
    public static final String COLUMN_SONG_ARTIST = "s_artist";
    public static final String COLUMN_SONG_GENRE = "s_genre";
    public static final String COLUMN_SONG_DURATION = "s_duration";
    public static final String COLUMN_SONG_TIMES_CLICKED = "s_times_clicked";
    public static final String COLUMN_SONG_TIMES_FINISHED = "s_times_finished";
    public static final String COLUMN_SONG_TIMES_SKIPPED = "s_times_skipped";
    public static final String COLUMN_SONG_AVG_LISTENING_TIME = "s_avg_listening_time";
    public static final String COLUMN_SONG_RATING = "s_rating";
    public static final String COLUMN_SONG_DELETED = "s_deleted";

    // Genre Table Is Going To Be Utilized In The Upcoming Updates
    public static final String TABLE_GENRES = "table_genres";
    public static final String COLUMN_GENRE_ID = "g_id";
    public static final String COLUMN_GENRE_TITLE = "g_title";

    private static final String DATABASE_NAME = "smartplay.db";
    private static final int 	DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_TABLE_SONGS = "create table "
            + TABLE_SONGS + "( " + COLUMN_SONG_ID + " integer primary key autoincrement, "
            + COLUMN_SONG_PATH + " text not null,"
            + COLUMN_SONG_TITLE + " text not null,"
            + COLUMN_SONG_ARTIST + " text not null,"
            + COLUMN_SONG_GENRE + " text not null,"
            + COLUMN_SONG_DURATION + " integer not null,"
            + COLUMN_SONG_TIMES_CLICKED + " integer,"
            + COLUMN_SONG_TIMES_FINISHED + " integer,"
            + COLUMN_SONG_TIMES_SKIPPED + " integer,"
            + COLUMN_SONG_AVG_LISTENING_TIME + " integer,"
            + COLUMN_SONG_RATING + " integer,"
            + COLUMN_SONG_DELETED + " integer not null);";

    private static final String DATABASE_CREATE_TABLE_GENRES = "create table "
            + TABLE_GENRES + "( " + COLUMN_GENRE_ID + " integer primary key autoincrement, "
            + COLUMN_GENRE_TITLE + " text not null);";


    public DBDesigner(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL(DATABASE_CREATE_TABLE_SONGS);
        database.execSQL(DATABASE_CREATE_TABLE_GENRES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBDesigner.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GENRES);
        onCreate(db);
    }
}