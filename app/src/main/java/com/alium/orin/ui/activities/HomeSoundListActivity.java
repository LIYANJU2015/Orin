package com.alium.orin.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alium.orin.R;
import com.alium.orin.glide.SongGlideRequest;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.model.Song;
import com.alium.orin.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.alium.orin.ui.fragments.mainactivity.library.pager.HomeFragment;
import com.bumptech.glide.Glide;
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

public class HomeSoundListActivity extends AbsSlidingMusicPanelActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;

    private String mTitle;

    private ArrayList<Song> mData;

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.home_sound_list_layout);
    }

    public static void launch(Context context, String title) {
        Intent intent = new Intent(context, HomeSoundListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("title", title);
        context.startActivity(intent);
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

        mData = HomeFragment.getHomeSoundContents(mTitle);

        setUpRecyclerView();

        setUpToolBar();
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        recyclerView.setAdapter(new CommonAdapter<Song>(this,
                R.layout.item_list, mData) {
            @Override
            protected void convert(final ViewHolder holder, Song song,
                                   int position) {
                holder.getView(R.id.menu).setVisibility(View.INVISIBLE);

                Glide.with(HomeSoundListActivity.this).load(song.getAlbum_images())
                        .crossFade().placeholder(R.drawable.default_album_art)
                        .into((ImageView) holder.getView(R.id.image));

                TextView titleTV = holder.getView(R.id.title);
                titleTV.setText(song.title);

                TextView textTV = holder.getView(R.id.text);
                textTV.setText(song.artistName);

                holder.setOnClickListener(R.id.list_item_frame, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MusicPlayerRemote.openQueue(mData, holder.getAdapterPosition(), true);
                    }
                });
            }
        });
        recyclerView.setItemAnimator(animator);
    }
}
