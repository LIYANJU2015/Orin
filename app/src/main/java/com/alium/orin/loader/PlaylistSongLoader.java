package com.alium.orin.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.alium.orin.model.PlaylistSong;
import com.alium.orin.provider.PlayListProvider;

import java.util.ArrayList;

public class PlaylistSongLoader {

    @NonNull
    public static ArrayList<PlaylistSong> getPlaylistSongList(@NonNull final Context context, final int playlistId) {
        ArrayList<PlaylistSong> songs = new ArrayList<>();
        Cursor cursor = makePlaylistSongCursor(context, playlistId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    @NonNull
    private static PlaylistSong getPlaylistSongFromCursorImpl(@NonNull Cursor cursor, int playlistId) {
        final int id = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.ID));
        final String title = cursor.getString(cursor.getColumnIndex(PlayListProvider.PlayListSong.TITLE));
        final int trackNumber = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.TRACK));
        final int year = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.YEAR));
        final long duration = cursor.getLong(cursor.getColumnIndex(PlayListProvider.PlayListSong.DURATION));
        final String data = cursor.getString(cursor.getColumnIndex(PlayListProvider.PlayListSong.DATA));
        final int dateModified = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.DATE_MODIFIED));
        final int albumId = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.ALBUM_ID));
        final String albumName = cursor.getString(cursor.getColumnIndex(PlayListProvider.PlayListSong.ALBUM));
        final int artistId = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.ARTIST_ID));
        final String artistName = cursor.getString(cursor.getColumnIndex(PlayListProvider.PlayListSong.ARTIST));
        final int idInPlaylist = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong._ID));
        final boolean isLocal = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong.IS_LOCAL)) == 1;
        String albuImage = cursor.getString(cursor.getColumnIndex(PlayListProvider.PlayListSong.ALBUM_IMAGE));

        return new PlaylistSong(albuImage, isLocal, id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName, playlistId, idInPlaylist);
    }

    public static Cursor makePlaylistSongCursor(@NonNull final Context context, final int playlistId) {
        try {
            return context.getContentResolver().query(
                    PlayListProvider.PlayListSong.URI,null,
                    PlayListProvider.PlayListSong.TITLE + " != ''", null,
                    PlayListProvider.PlayListSong.PLAY_ORDER);
        } catch (SecurityException e) {
            return null;
        }
    }
}
