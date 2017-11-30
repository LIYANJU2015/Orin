package com.alium.orin.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alium.orin.model.Playlist;
import com.alium.orin.provider.PlayListProvider;

import java.util.ArrayList;

public class PlaylistLoader {

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@NonNull final Context context) {
        return getAllPlaylists(makePlaylistCursor(context, null, null));
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final int playlistId) {
        return getPlaylist(makePlaylistCursor(
                context,
                BaseColumns._ID + "=?",
                new String[]{
                        String.valueOf(playlistId)
                }
        ));
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final String playlistName) {
        return getPlaylist(makePlaylistCursor(
                context,
                PlayListProvider.PlayList.NAME + "=?",
                new String[]{
                        playlistName
                }
        ));
    }

    @NonNull
    public static Playlist getPlaylist(@Nullable final Cursor cursor) {
        Playlist playlist = new Playlist();

        if (cursor != null && cursor.moveToFirst()) {
            playlist = getPlaylistFromCursorImpl(cursor);
        }
        if (cursor != null)
            cursor.close();
        return playlist;
    }

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@Nullable final Cursor cursor) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return playlists;
    }

    @NonNull
    private static Playlist getPlaylistFromCursorImpl(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        return new Playlist(id, name);
    }

    @Nullable
    public static Cursor makePlaylistCursor(@NonNull final Context context, final String selection, final String[] values) {
        try {
            return context.getContentResolver().query(PlayListProvider.PlayList.URI,
                    new String[]{
                        /* 0 */
                            PlayListProvider.PlayList._ID,
                        /* 1 */
                            PlayListProvider.PlayList.NAME
                    }, selection, values, PlayListProvider.PlayList._ID);
        } catch (SecurityException e) {
            return null;
        }
    }
}