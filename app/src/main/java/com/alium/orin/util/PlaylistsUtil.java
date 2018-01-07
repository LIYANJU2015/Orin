package com.alium.orin.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.alium.orin.R;
import com.alium.orin.helper.M3UWriter;
import com.alium.orin.model.Playlist;
import com.alium.orin.model.PlaylistSong;
import com.alium.orin.model.Song;
import com.alium.orin.provider.PlayListProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *歌单 添加删除工具类
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {

    public static int createPlaylist(@NonNull final Context context, @Nullable final String name) {
        int id = -1;
        if (name != null && name.length() > 0) {
            try {
                Cursor cursor = context.getContentResolver().query(PlayListProvider.PlayList.URI,
                        new String[]{PlayListProvider.PlayList._ID}, PlayListProvider.PlayList.NAME + "=?", new String[]{name}, null);
                if (cursor == null || cursor.getCount() < 1) {
                    final ContentValues values = new ContentValues(1);
                    values.put(PlayListProvider.PlayList.NAME, name);
                    final Uri uri = context.getContentResolver().insert(
                            PlayListProvider.PlayList.URI,
                            values);
                    if (uri != null) {
                        // necessary because somehow the MediaStoreObserver is not notified when adding a playlist
                        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                        Toast.makeText(context, context.getResources().getString(
                                R.string.created_playlist_x, name), Toast.LENGTH_SHORT).show();
                        id = Integer.parseInt(uri.getLastPathSegment());
                    }
                } else {
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SecurityException ignored) {
            }
        }
        if (id == -1) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.could_not_create_playlist), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<Playlist> playlists) {
        final StringBuilder selection = new StringBuilder();
        selection.append(PlayListProvider.PlayList._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        try {
            context.getContentResolver().delete(PlayListProvider.PlayList.URI, selection.toString(), null);
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final int playlistId, final boolean showToastOnFinish) {
        List<Song> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final int playlistId, final boolean showToastOnFinish) {
        final int size = songs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + PlayListProvider.PlayListSong.PLAY_ORDER + ")",
        };
        Cursor cursor = null;
        int base = 0;

        try {
            try {

                cursor = resolver.query(PlayListProvider.PlayListSong.URI, projection, PlayListProvider.PlayListSong.PLAY_LIST_ID + "=" + playlistId,
                        null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    base = cursor.getInt(0) + 1;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            for (int offSet = 0; offSet < size; offSet += 1000) {
                resolver.bulkInsert(PlayListProvider.PlayListSong.URI, makeInsertItems(songs, offSet, 1000, base, playlistId));
            }

            if (showToastOnFinish) {
                try {
                    Toast.makeText(context, context.getResources().getString(
                            R.string.inserted_x_songs_into_playlist_x, size, getNameForPlaylist(context, playlistId)), Toast.LENGTH_SHORT).show();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException ignored) {
        }
    }

    @NonNull
    public static ContentValues[] makeInsertItems(@NonNull final List<Song> songs, final int offset,
                                                  int len, final int base, final int playlistId) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(PlayListProvider.PlayListSong.PLAY_ORDER, base + offset + i);
            contentValues[i].put(PlayListProvider.PlayListSong.ID, songs.get(offset + i).id);
            contentValues[i].put(PlayListProvider.PlayListSong.TITLE, songs.get(offset + i).title);
            contentValues[i].put(PlayListProvider.PlayListSong.TRACK, songs.get(offset + i).trackNumber);
            contentValues[i].put(PlayListProvider.PlayListSong.YEAR, songs.get(offset + i).year);
            contentValues[i].put(PlayListProvider.PlayListSong.DURATION, songs.get(offset + i).getDuration());
            contentValues[i].put(PlayListProvider.PlayListSong.DATA, songs.get(offset + i).getPath());
            contentValues[i].put(PlayListProvider.PlayListSong.DATE_MODIFIED, songs.get(offset + i).dateModified);
            contentValues[i].put(PlayListProvider.PlayListSong.ALBUM_ID, songs.get(offset + i).albumId);
            contentValues[i].put(PlayListProvider.PlayListSong.ALBUM, songs.get(offset + i).albumName);
            contentValues[i].put(PlayListProvider.PlayListSong.ARTIST_ID, songs.get(offset + i).artistId);
            contentValues[i].put(PlayListProvider.PlayListSong.ARTIST, songs.get(offset + i).artistName);
            contentValues[i].put(PlayListProvider.PlayListSong.PLAY_LIST_ID, playlistId);
            contentValues[i].put(PlayListProvider.PlayListSong.IS_LOCAL, songs.get(offset + i).isLocalSong());
            contentValues[i].put(PlayListProvider.PlayListSong.ALBUM_IMAGE, songs.get(offset + i).getAlbum_images());

        }
        return contentValues;
    }

    public static void removeFromPlaylist(@NonNull final Context context,
                                          @NonNull final Song song,
                                          int playlistId) {
        String selection = PlayListProvider.PlayListSong.PLAY_LIST_ID + " = ？" + PlayListProvider.PlayListSong.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(playlistId), String.valueOf(song.id)};

        try {
            context.getContentResolver().delete(PlayListProvider.PlayListSong.URI, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).idInPlayList);
        }
        String selection = PlayListProvider.PlayListSong._ID + " in (";
        //noinspection unused
        for (String selectionArg : selectionArgs) selection += "?, ";
        selection = selection.substring(0, selection.length() - 2) + ")";

        try {
            context.getContentResolver().delete(PlayListProvider.PlayListSong.URI, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static boolean doPlaylistContains(@NonNull final Context context, final long playlistId, final int songId) {
        if (playlistId != -1) {
            try {
                Cursor c = context.getContentResolver().query(PlayListProvider.PlayListSong.URI,
                        new String[]{PlayListProvider.PlayListSong.ID}, PlayListProvider.PlayListSong.PLAY_LIST_ID + " = " + playlistId
                                + " and " + PlayListProvider.PlayListSong.ID + "=?", new String[]{String.valueOf(songId)}, null);
                int count = 0;
                if (c != null) {
                    count = c.getCount();
                    c.close();
                }
                return count > 0;
            } catch (SecurityException ignored) {
            }
        }
        return false;
    }

    public static boolean moveItem(@NonNull final Context context, int playlistId, int from, int to) {
        to = to + 1;
        from = from + 1;
        LogUtil.v("playlist", "moveItem from " + from + " to :" + to);
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(PlayListProvider.PlayListSong.PLAY_ORDER, to);

        Cursor cursor = resolver.query(PlayListProvider.PlayListSong.URI, new String[]{PlayListProvider.PlayListSong._ID},
                PlayListProvider.PlayListSong.PLAY_LIST_ID + " = " + playlistId + " and "
                        + PlayListProvider.PlayListSong.PLAY_ORDER + " = " + to ,
                null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(PlayListProvider.PlayListSong._ID));

            int updateId = resolver.update(PlayListProvider.PlayListSong.URI, contentValues,
                    PlayListProvider.PlayListSong.PLAY_LIST_ID + " = " + playlistId + " and "
                            + PlayListProvider.PlayListSong.PLAY_ORDER + " = " + from,
                    null);
            LogUtil.v("playlist", "moveItem11 updateId " + updateId);

            contentValues = new ContentValues(1);
            contentValues.put(PlayListProvider.PlayListSong.PLAY_ORDER, from);

            updateId = resolver.update(PlayListProvider.PlayListSong.URI,contentValues,
                    PlayListProvider.PlayListSong._ID + " = " + id,
                    null);
            LogUtil.v("playlist", "moveItem22 updateId " + updateId);
            return true;
        }
        return false;
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlayListProvider.PlayList.NAME, newName);
        try {
            context.getContentResolver().update(PlayListProvider.PlayList.URI,
                    contentValues,
                    PlayListProvider.PlayList._ID + "=?",
                    new String[]{String.valueOf(id)});
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        try {
            Cursor cursor = context.getContentResolver().query(
                    PlayListProvider.PlayList.URI,
                    new String[]{PlayListProvider.PlayList.NAME},
                    PlayListProvider.PlayList._ID + "=?",
                    new String[]{String.valueOf(id)},
                    null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SecurityException ignored) {
        }
        return "";
    }

    public static File savePlaylist(Context context, Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }
}