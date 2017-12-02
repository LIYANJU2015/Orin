package com.alium.orin.ui.fragments.player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alium.orin.R;
import com.alium.orin.glide.BlurTransformation;
import com.alium.orin.glide.SongGlideRequest;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.helper.MusicProgressViewUpdateHelper;
import com.alium.orin.helper.PlayPauseButtonOnClickHandler;
import com.alium.orin.model.Song;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.LogUtil;
import com.alium.orin.views.PlayPauseDrawable;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MiniPlayerFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {

    private Unbinder unbinder;

    @BindView(R.id.mini_player_title)
    TextView miniPlayerTitle;
    @BindView(R.id.mini_player_play_pause_button)
    ImageView miniPlayerPlayPauseButton;
    @BindView(R.id.progress_bar)
    MaterialProgressBar progressBar;
    @BindView(R.id.mimi_player_pb)
    ImageView loadingPB;
    @BindView(R.id.mimi_play_iv_bg)
    ImageView mimiPlayerIVBg;

    private PlayPauseDrawable miniPlayerPlayPauseDrawable;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        loadingPB.setColorFilter(ATHUtil.resolveColor(getActivity(), R.attr.iconColor, ThemeStore.primaryColor(getActivity())), PorterDuff.Mode.SRC_IN);

        view.setOnTouchListener(new FlingPlayBackController(getActivity()));
        setUpMiniPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setUpMiniPlayer() {
        setUpPlayPauseButton();
        progressBar.setProgressTintList(ColorStateList.valueOf(ThemeStore.accentColor(getActivity())));
    }

    private void setUpPlayPauseButton() {
        miniPlayerPlayPauseDrawable = new PlayPauseDrawable(getActivity());
        miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable);
        miniPlayerPlayPauseButton.setColorFilter(ATHUtil.resolveColor(getActivity(), R.attr.iconColor, ThemeStore.textColorSecondary(getActivity())), PorterDuff.Mode.SRC_IN);
        miniPlayerPlayPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
    }

    private void updateSongTitle() {
        miniPlayerTitle.setText(MusicPlayerRemote.getCurrentSong().title);
    }

    @Override
    public void onServiceConnected() {
        updateSongTitle();
        updatePlayPauseDrawableState(false);
    }

    private boolean isShowLoading = false;

    @Override
    public void onPlayingMetaChanged() {
        updateSongTitle();
        LogUtil.v("cardplayer", "onPlayingMetaChanged");
        isShowLoading = !MusicPlayerRemote.getCurrentSong().isLocalSong();
        if (miniPlayerPlayPauseButton.getVisibility() == View.INVISIBLE) {
            miniPlayerPlayPauseButton.setVisibility(View.VISIBLE);
        }
//        SongGlideRequest.Builder.from(Glide.with(this), MusicPlayerRemote.getCurrentSong())
//                .checkIgnoreMediaStore(mContext).build().placeholder(R.drawable.while_bg)
//                .into(mimiPlayerIVBg);
        BitmapRequestBuilder<?, Bitmap> request = SongGlideRequest.Builder.from(Glide.with(mContext),
                MusicPlayerRemote.getCurrentSong())
                .checkIgnoreMediaStore(mContext)
                .asBitmap().build();
        request.transform(new BlurTransformation.Builder(mContext).build()).into(mimiPlayerIVBg);
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onPlayError() {
        super.onPlayError();
        LogUtil.v("mini", "onPlayError");
        isShowLoading = false;
        loadingPB.setVisibility(View.INVISIBLE);
        miniPlayerPlayPauseDrawable.setPlay(true);
        if (miniPlayerPlayPauseButton.getVisibility() == View.INVISIBLE) {
            miniPlayerPlayPauseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlayerStartPreper() {
        LogUtil.v("mini", "onPlayerStartPreper");
        if (!MusicPlayerRemote.getCurrentSong().isLocalSong()) {
            isShowLoading = true;
            loadingPB.setVisibility(View.VISIBLE);
            miniPlayerPlayPauseButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPlayerEndPrepered() {
        super.onPlayerEndPrepered();
        isShowLoading = false;
        loadingPB.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingPB.setVisibility(View.INVISIBLE);
            }
        }, 500);
        if (miniPlayerPlayPauseButton.getVisibility() == View.INVISIBLE) {
            miniPlayerPlayPauseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(progress);
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

    private static class FlingPlayBackController implements View.OnTouchListener {

        GestureDetector flingPlayBackController;

        public FlingPlayBackController(Context context) {
            flingPlayBackController = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong();
                            return true;
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return flingPlayBackController.onTouchEvent(event);
        }
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        Song song = MusicPlayerRemote.getCurrentSong();
        if (MusicPlayerRemote.isPlaying()) {
            if (!song.isLocalSong() && isShowLoading) {
                loadingPB.setVisibility(View.VISIBLE);
            } else {
                loadingPB.setVisibility(View.INVISIBLE);
            }
            miniPlayerPlayPauseDrawable.setPause(animate);
        } else {
            if (!song.isLocalSong()) {
                loadingPB.setVisibility(View.INVISIBLE);
            }
            miniPlayerPlayPauseDrawable.setPlay(animate);
        }
    }
}
