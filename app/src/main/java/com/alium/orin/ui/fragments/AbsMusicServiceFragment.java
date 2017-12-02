package com.alium.orin.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.alium.orin.interfaces.MusicServiceEventListener;
import com.alium.orin.ui.activities.base.AbsMusicServiceActivity;
import com.alium.orin.util.LogUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsMusicServiceFragment extends Fragment implements MusicServiceEventListener {
    protected AbsMusicServiceActivity activity;

    protected Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContext = context;
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        LogUtil.v("Player", "onPlayingMetaChanged");
    }

    @Override
    public void onServiceConnected() {
        LogUtil.v("Player", "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected() {
        LogUtil.v("Player", "onServiceDisconnected");
    }

    @Override
    public void onQueueChanged() {
        LogUtil.v("Player", "onQueueChanged");
    }

    @Override
    public void onPlayStateChanged() {
        LogUtil.v("Player", "onPlayStateChanged");
    }

    @Override
    public void onRepeatModeChanged() {
        LogUtil.v("Player", "onRepeatModeChanged");
    }

    @Override
    public void onShuffleModeChanged() {
        LogUtil.v("Player", "onShuffleModeChanged");
    }

    @Override
    public void onMediaStoreChanged() {
        LogUtil.v("Player", "onMediaStoreChanged");
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void onPlayerEndPrepered() {
        LogUtil.v("Player", "onPlayerEndPrepered >>");
    }

    @Override
    public void onPlayerStartPreper() {

    }
}
