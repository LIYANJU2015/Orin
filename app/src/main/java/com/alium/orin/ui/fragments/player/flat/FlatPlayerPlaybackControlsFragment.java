package com.alium.orin.ui.fragments.player.flat;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.helper.MusicProgressViewUpdateHelper;
import com.alium.orin.helper.PlayPauseButtonOnClickHandler;
import com.alium.orin.misc.SimpleOnSeekbarChangeListener;
import com.alium.orin.model.Song;
import com.alium.orin.service.MusicService;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.MusicUtil;
import com.alium.orin.views.LoadingProgressBar;
import com.alium.orin.views.PlayPauseDrawable;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.alium.orin.R;

import java.util.Collection;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class FlatPlayerPlaybackControlsFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {

    private Unbinder unbinder;

    @BindView(R.id.player_play_pause__button)
    ImageButton playPauseButton;
    @BindView(R.id.player_prev_button)
    ImageButton prevButton;
    @BindView(R.id.player_next_button)
    ImageButton nextButton;
    @BindView(R.id.player_repeat_button)
    ImageButton repeatButton;
    @BindView(R.id.player_shuffle_button)
    ImageButton shuffleButton;

    @BindView(R.id.player_progress_slider)
    SeekBar progressSlider;
    @BindView(R.id.player_song_total_time)
    TextView songTotalTime;
    @BindView(R.id.player_song_current_progress)
    TextView songCurrentProgress;
    @BindView(R.id.loading_pb)
    LoadingProgressBar loadingPB;

    private PlayPauseDrawable playPauseDrawable;

    private int lastPlaybackControlsColor;
    private int lastDisabledPlaybackControlsColor;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    private AnimatorSet musicControllerAnimationSet;

    private boolean isShowLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flat_player_playback_controls, container, false);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        LogUtil.v("flatplayer", "onPlayingMetaChanged");
        isShowLoading = !MusicPlayerRemote.getCurrentSong().isLocalSong();
        playPauseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayerStartPreper() {
        super.onPlayerStartPreper();
        LogUtil.v("flatplayer", "onPlayerStartPreper");
        if (!MusicPlayerRemote.getCurrentSong().isLocalSong()) {
            isShowLoading = true;
            loadingPB.setVisibility(View.VISIBLE);
            playPauseButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPlayerEndPrepered() {
        super.onPlayerEndPrepered();
        LogUtil.v("flatplayer", "onPlayerEndPrepered");
        isShowLoading = false;
        loadingPB.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingPB != null) {
                    loadingPB.setVisibility(View.INVISIBLE);
                }
            }
        }, 500);

        playPauseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayError() {
        super.onPlayError();
        LogUtil.v("cardplayer", "onPlayError");
        isShowLoading = false;
        loadingPB.setVisibility(View.INVISIBLE);
        playPauseDrawable.setPlay(true);
        playPauseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        setUpMusicControllers();
        updateProgressTextColor();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        updatePlayPauseDrawableState(false);
        updateRepeatState();
        updateShuffleState();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    public void setDark(boolean dark) {
        if (dark) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(getActivity(), true);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getSecondaryDisabledTextColor(getActivity(), true);
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(getActivity(), false);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getPrimaryDisabledTextColor(getActivity(), false);
        }

        updateRepeatState();
        updateShuffleState();
        updatePrevNextColor();
        updatePlayPauseColor();
        updateProgressTextColor();
    }

    private void setUpPlayPauseButton() {
        playPauseDrawable = new PlayPauseDrawable(getActivity());
        playPauseButton.setImageDrawable(playPauseDrawable);
        updatePlayPauseColor();
        playPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
        playPauseButton.post(new Runnable() {
            @Override
            public void run() {
                if (playPauseButton != null) {
                    playPauseButton.setPivotX(playPauseButton.getWidth() / 2);
                    playPauseButton.setPivotY(playPauseButton.getHeight() / 2);
                }
            }
        });
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        Song song = MusicPlayerRemote.getCurrentSong();
        if (MusicPlayerRemote.isPlaying()) {
            if (!song.isLocalSong() && isShowLoading) {
                loadingPB.setVisibility(View.VISIBLE);
            } else {
                loadingPB.setVisibility(View.INVISIBLE);
            }
            playPauseDrawable.setPause(animate);
        } else {
            if (!song.isLocalSong()) {
                loadingPB.setVisibility(View.INVISIBLE);
            }
            playPauseDrawable.setPlay(animate);
        }
    }

    private void setUpMusicControllers() {
        setUpPlayPauseButton();
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setUpPrevNext() {
        updatePrevNextColor();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.playNextSong();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.back();
            }
        });
    }

    private void updateProgressTextColor() {
        int color = MaterialValueHelper.getPrimaryTextColor(getContext(), false);
        songTotalTime.setTextColor(color);
        songCurrentProgress.setTextColor(color);
    }

    private void updatePrevNextColor() {
        nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        prevButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private void updatePlayPauseColor() {
        playPauseButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private void setUpShuffleButton() {
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.toggleShuffleMode();
            }
        });
    }

    private void updateShuffleState() {
        switch (MusicPlayerRemote.getShuffleMode()) {
            case MusicService.SHUFFLE_MODE_SHUFFLE:
                shuffleButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            default:
                shuffleButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    private void setUpRepeatButton() {
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.cycleRepeatMode();
            }
        });
    }

    private void updateRepeatState() {
        switch (MusicPlayerRemote.getRepeatMode()) {
            case MusicService.REPEAT_MODE_NONE:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_THIS:
                repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    public void show() {
        if (musicControllerAnimationSet == null) {
            TimeInterpolator interpolator = new FastOutSlowInInterpolator();
            final int duration = 300;

            LinkedList<Animator> animators = new LinkedList<>();

            addAnimation(animators, playPauseButton, interpolator, duration, 0);
            addAnimation(animators, nextButton, interpolator, duration, 100);
            addAnimation(animators, prevButton, interpolator, duration, 100);
            addAnimation(animators, shuffleButton, interpolator, duration, 200);
            addAnimation(animators, repeatButton, interpolator, duration, 200);


            musicControllerAnimationSet = new AnimatorSet();
            musicControllerAnimationSet.playTogether(animators);
        } else {
            musicControllerAnimationSet.cancel();
        }
        musicControllerAnimationSet.start();
    }

    public void hide() {
        if (musicControllerAnimationSet != null) {
            musicControllerAnimationSet.cancel();
        }
        prepareForAnimation(playPauseButton);
        prepareForAnimation(nextButton);
        prepareForAnimation(prevButton);
        prepareForAnimation(shuffleButton);
        prepareForAnimation(repeatButton);
    }

    private static void addAnimation(Collection<Animator> animators, View view, TimeInterpolator interpolator, int duration, int delay) {
        Animator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f);
        scaleX.setInterpolator(interpolator);
        scaleX.setDuration(duration);
        scaleX.setStartDelay(delay);
        animators.add(scaleX);

        Animator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f);
        scaleY.setInterpolator(interpolator);
        scaleY.setDuration(duration);
        scaleY.setStartDelay(delay);
        animators.add(scaleY);
    }

    private static void prepareForAnimation(View view) {
        if (view != null) {
            view.setScaleX(0f);
            view.setScaleY(0f);
        }
    }

    private void setUpProgressSlider() {
        int color = MaterialValueHelper.getPrimaryTextColor(getContext(), false);
        progressSlider.getThumb().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        progressSlider.getProgressDrawable().mutate().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);

        progressSlider.setOnSeekBarChangeListener(new SimpleOnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress);
                    onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(), MusicPlayerRemote.getSongDurationMillis());
                }
            }
        });
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressSlider.setMax(total);
        progressSlider.setProgress(progress);
        songTotalTime.setText(MusicUtil.getReadableDurationString(total));
        songCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }
}
