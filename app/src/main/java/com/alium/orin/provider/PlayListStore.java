package com.alium.orin.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

/**
 * Created by liyanju on 2017/11/28.
 */

public class PlayListStore extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "playlist.db";
    private static final int VERSION = 1;

    private static PlayListStore sInstance;

    public PlayListStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @NonNull
    public static synchronized PlayListStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PlayListStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String TRACK = "track";
    public static final String YEAR = "year";
    public static final String DURATION = "duration";

    //    BaseColumns._ID,// 0
//    AudioColumns.TITLE,// 1
//    AudioColumns.TRACK,// 2
//    AudioColumns.YEAR,// 3
//    AudioColumns.DURATION,// 4
//    AudioColumns.DATA,// 5
//    AudioColumns.DATE_MODIFIED,// 6
//    AudioColumns.ALBUM_ID,// 7
//    AudioColumns.ALBUM,// 8
//    AudioColumns.ARTIST_ID,// 9
//    AudioColumns.ARTIST,// 10
}
