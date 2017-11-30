package com.alium.orin.provider;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.SparseArray;

import com.alium.orin.provider.base.BaseContentProvider;
import com.alium.orin.provider.base.TableInfo;

import java.util.Map;

/**
 * Created by liyanju on 2017/11/29.
 */

public class PlayListProvider extends BaseContentProvider{

    public static final String DATA_BASE_NAME = "play_list";
    public static final String AUTHORITIES = "com.alium.orin.provider.PlayListProvider";

    public static final int PLAY_LIST = 11;
    public static final int PLAY_LIST_SONG = 22;

    @Override
    public void onAddTableInfo(SparseArray<TableInfo> tableInfoArray) {
        tableInfoArray.put(PLAY_LIST, new PlayList());
        tableInfoArray.put(PLAY_LIST_SONG, new PlayListSong());
    }

    @Override
    public String onDataBaseName() {
        return DATA_BASE_NAME;
    }

    @Override
    public int onDataBaseVersion() {
        return 1;
    }

    @Override
    public void onDBUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class PlayListSong extends TableInfo {

        public static final String TABLE_NAME = "PlayListSong";
        public static final Uri URI = Uri.parse("content://" + PlayListProvider.AUTHORITIES + "/" + TABLE_NAME);

        public static final String ID = "id"; //歌曲ID
        public static final String TITLE = "title";
        public static final String TRACK = "track";
        public static final String YEAR = "year";
        public static final String DURATION = "duration";
        public static final String DATA = "data";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String ALBUM_ID = "album_id";
        public static final String ALBUM = "album";
        public static final String ARTIST_ID = "artist_id";
        public static final String ARTIST = "artist";
        public static final String PLAY_ORDER = "play_order";
        public static final String PLAY_LIST_ID = "play_list_id"; //歌单ID

        public static final String IS_LOCAL = "is_local";
        public static final String ALBUM_IMAGE = "album_image";
        @Override
        public String onTableName() {
            return TABLE_NAME;
        }

        @Override
        public Uri onContentUri() {
            return URI;
        }

        @Override
        public void onInitColumnsMap(Map<String, String> columnsMap) {
            columnsMap.put(ID, "int");
            columnsMap.put(TITLE, "text");
            columnsMap.put(TRACK, "text");
            columnsMap.put(YEAR, "text");
            columnsMap.put(DURATION, "int");
            columnsMap.put(DATA, "text");
            columnsMap.put(DATE_MODIFIED, "long");
            columnsMap.put(ALBUM_ID, "int");
            columnsMap.put(ALBUM, "text");
            columnsMap.put(ARTIST_ID, "int");
            columnsMap.put(ARTIST, "text");
            columnsMap.put(PLAY_ORDER, "int");
            columnsMap.put(PLAY_LIST_ID, "int");
            columnsMap.put(IS_LOCAL, "int");
            columnsMap.put(ALBUM_IMAGE, "text");

        }
    }

    public static class PlayList extends TableInfo {

        public static final String TABLE_NAME = "PlayList";

        public static final Uri URI = Uri.parse("content://" + PlayListProvider.AUTHORITIES + "/" + TABLE_NAME);

        public static final String NAME = "playlistname";
        public static final String COUNT = "count";

        @Override
        public String onTableName() {
            return TABLE_NAME;
        }

        @Override
        public Uri onContentUri() {
            return URI;
        }

        @Override
        public void onInitColumnsMap(Map<String, String> columnsMap) {
            columnsMap.put(NAME, "text");
            columnsMap.put(COUNT, "int");
        }
    }
}
