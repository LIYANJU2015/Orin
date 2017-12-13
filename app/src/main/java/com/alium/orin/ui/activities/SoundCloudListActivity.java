package com.alium.orin.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.admodule.AdModule;
import com.alium.orin.R;
import com.alium.orin.adapter.AdViewWrapperAdapter;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.helper.menu.SongMenuHelper;
import com.alium.orin.model.Song;
import com.alium.orin.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.alium.orin.ui.fragments.mainactivity.library.pager.SoundCloudFragment;
import com.alium.orin.util.StatReportUtils;
import com.bumptech.glide.Glide;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by liyanju on 2017/11/26.
 */

public class SoundCloudListActivity extends AbsSlidingMusicPanelActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    RelativeLayout empty;

    private String mTitle;

    private ArrayList<Song> mData;

    private CommonAdapter commonadapter;

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.home_sound_list_layout);
    }

    public static void launch(Activity activity, String title) {
        Intent intent = new Intent(activity, SoundCloudListActivity.class);
        intent.putExtra("title", title);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_bottom_in, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_bottom_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        mTitle = getIntent().getStringExtra("title");

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        mData = SoundCloudFragment.getHomeSoundContents(mTitle);

        setUpRecyclerView();

        setUpToolBar();
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdModule.getInstance().getFacebookAd().loadAd(true, "1305172892959949_1313403128803592");
    }

    private AdViewWrapperAdapter adViewWrapperAdapter;

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        commonadapter = new CommonAdapter<Song>(this,
                R.layout.item_list, mData) {
            @Override
            protected void convert(final ViewHolder holder, final Song song,
                                   int position) {
                Glide.with(SoundCloudListActivity.this).load(song.getAlbum_images())
                        .crossFade().placeholder(R.drawable.default_album_art)
                        .into((ImageView) holder.getView(R.id.image));

                TextView titleTV = holder.getView(R.id.title);
                titleTV.setText(song.title);

                TextView textTV = holder.getView(R.id.text);
                textTV.setText(song.artistName);

                holder.setOnClickListener(R.id.list_item_frame, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        if (adViewWrapperAdapter != null) {
                            position = adViewWrapperAdapter.getAdViewCountBeforeByPostion(holder.getAdapterPosition());
                            position = holder.getAdapterPosition() - position;
                        }
                        AdModule.getInstance().getAdMob().showInterstitialAd();
                        MusicPlayerRemote.openQueue(mData, position, true);

                        StatReportUtils.trackCustomEvent("home_list_page", "item click");
                    }
                });
                final ImageView overflowButton = holder.getView(R.id.menu);
                overflowButton.setOnClickListener(new SongMenuHelper.OnClickSongMenu(SoundCloudListActivity.this) {

                    @Override
                    public int getMenuRes() {
                        return R.menu.menu_item_online_song;
                    }

                    @Override
                    public Song getSong() {
                        return song;
                    }
                });
            }
        };

        RecyclerView.Adapter adapter;
        NativeAd nativeAd = AdModule.getInstance().getFacebookAd().getNativeAd();
        if (nativeAd != null && nativeAd.isAdLoaded()) {
            adViewWrapperAdapter = new AdViewWrapperAdapter(commonadapter);
            adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
                    AdViewItem(setUpNativeAdView(nativeAd), 1));
            adapter = adViewWrapperAdapter;
            StatReportUtils.trackCustomEvent("home_list_page", "ad show");
        } else {
            adapter = commonadapter;
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(animator);
    }

    private View setUpNativeAdView(NativeAd nativeAd) {
        nativeAd.unregisterView();

        View adView = LayoutInflater.from(this).inflate(R.layout.home_list_ad_item, null);

        FrameLayout adChoicesFrame = (FrameLayout)adView.findViewById(R.id.fb_adChoices2);
        ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.image_ad);
        TextView nativeAdTitle = (TextView) adView.findViewById(R.id.title);
        TextView nativeAdBody = (TextView) adView.findViewById(R.id.text);
        TextView nativeAdCallToAction = (TextView) adView.findViewById(R.id.call_btn_tv);

        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdTitle.setText(nativeAd.getAdTitle());
        nativeAdBody.setText(nativeAd.getAdBody());

        // Downloading and setting the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

        // Add adChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(this, nativeAd, true);
        adChoicesFrame.addView(adChoicesView, 0);
        adChoicesFrame.setVisibility(View.VISIBLE);

        nativeAd.registerViewForInteraction(adView);

        return adView;
    }
}
