package com.alium.orin.ui.fragments.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.audiofx.AudioEffect;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.dialogs.AddToPlaylistDialog;
import com.alium.orin.dialogs.SleepTimerDialog;
import com.alium.orin.dialogs.SongDetailDialog;
import com.alium.orin.dialogs.SongShareDialog;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.interfaces.PaletteColorHolder;
import com.alium.orin.loader.SongLoader;
import com.alium.orin.model.Song;
import com.alium.orin.ui.activities.EqualizerActivity;
import com.alium.orin.ui.activities.tageditor.AbsTagEditorActivity;
import com.alium.orin.ui.activities.tageditor.SongTagEditorActivity;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.MusicUtil;
import com.alium.orin.util.NavigationUtil;
import com.alium.orin.util.PreferenceUtil;

public abstract class AbsPlayerFragment extends AbsMusicServiceFragment implements Toolbar.OnMenuItemClickListener, PaletteColorHolder {
    public static final String TAG = AbsPlayerFragment.class.getSimpleName();

    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must implement " + Callbacks.class.getSimpleName());
        }
        isShowLyric = PreferenceUtil.getInstance(App.sContext).isShowLyric();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    protected boolean isShowLyric;

    public void onHandleLyric(boolean isShow) {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final Song song = MusicPlayerRemote.getCurrentSong();
        switch (item.getItemId()) {
            case R.id.action_toggle_lyric:
                if (PreferenceUtil.getInstance(App.sContext).isShowLyric()) {
                    isShowLyric = false;
                    PreferenceUtil.getInstance(App.sContext).setShowLyric(false);
                    onHandleLyric(false);
                    item.getIcon().mutate().setColorFilter(Color.parseColor("#ffffff"),
                            PorterDuff.Mode.SRC_IN);
                } else {
                    isShowLyric = true;
                    PreferenceUtil.getInstance(App.sContext).setShowLyric(true);
                    onHandleLyric(true);
                    item.getIcon().mutate().setColorFilter(Color.parseColor("#757575"),
                            PorterDuff.Mode.SRC_IN);
                }
                return true;
            case R.id.action_toggle_equalizer:
                int sessionId = MusicPlayerRemote.getAudioSessionId();
                if (sessionId != AudioEffect.ERROR_BAD_VALUE && sessionId != -1) {
                    EqualizerActivity.launch(activity);
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.no_audio_ID),
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_toggle_favorite:
                toggleFavorite(song);
                return true;
            case R.id.action_share:
                SongShareDialog.create(song).show(getFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(getActivity()), true);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(getFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_clear_playing_queue:
                MusicPlayerRemote.clearQueue();
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(getActivity(), SongTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                startActivity(intent);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(getFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(getActivity(), song.albumId);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(getActivity(), song.artistId);
                return true;
        }
        return false;
    }

    protected void toggleFavorite(Song song) {
        MusicUtil.toggleFavorite(getActivity(), song);
    }

    protected String getUpNextAndQueueTime() {
        return getResources().getString(R.string.up_next) + "  •  " + MusicUtil.getReadableDurationString(MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.getPosition()));
    }

    public abstract void onShow();

    public abstract void onHide();

    public abstract boolean onBackPressed();

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public interface Callbacks {
        void onPaletteColorChanged();
    }
}
