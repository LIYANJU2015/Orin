package com.alium.orin.ui.fragments.mainactivity.library.pager;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.admodule.AdModule;
import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.model.Song;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.SoundCloudClient;
import com.alium.orin.ui.activities.HomeSoundListActivity;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.ACache;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.StatReportUtils;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by liyanju on 2017/11/24.
 */

public class HomeFragment extends AbsMusicServiceFragment {

    private Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    @BindView(android.R.id.empty)
    RelativeLayout empty;

    @BindView(R.id.app_bar_under_color)
    View behaviourOverLapEmulator;
    @BindView(R.id.loading_mb)
    View loading_mb;

    public static ArrayList<HomeSoundInAdapter> mDatas = new ArrayList<>();

    private MultiItemTypeAdapter itemTypeAdapter;

    public static ArrayList<Song> getHomeSoundContents(String title) {
        for (HomeSoundInAdapter soundInAdapter : mDatas) {
            if (soundInAdapter.contentsBean2 != null && title.equals(soundInAdapter.contentsBean2.getName())) {
                return soundInAdapter.contentsBean2.getContents();
            } else if (soundInAdapter.contentsBeanX != null && title.equals(soundInAdapter.contentsBeanX.getName())) {
                return soundInAdapter.contentsBeanX.contents;
            }
        }
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_main_home_view, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        behaviourOverLapEmulator.setBackgroundColor(ThemeStore.primaryColor(getActivity()));


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        itemTypeAdapter = new MultiItemTypeAdapter(getActivity(), mDatas);
        itemTypeAdapter.addItemViewDelegate(new TitleItemDelagate());
        itemTypeAdapter.addItemViewDelegate(new ListItemDelagate());
        itemTypeAdapter.addItemViewDelegate(new SingleItemDelagate());
        recyclerView.setAdapter(itemTypeAdapter);

        if (mDatas.size() == 0) {
            showHomeSoundData();
        }

        AdModule.getInstance().getFacebookAd().loadAd(true, "1305172892959949_1313403128803592");
    }

    private void showHomeSoundData() {
        new AsyncTask<Void, Void, HomeSound>() {

            @Override
            protected HomeSound doInBackground(Void... voids) {
                try {
                    if (App.isLoadLocalHomeSound() && App.sHomeSound != null) {
                        LogUtil.v("home", "load local homesound ....");
                        return App.sHomeSound;
                    }

                    Object obj = ACache.get(mContext).getAsObject(SoundCloudClient.HOME_SOUND_URL);
                    if (obj != null) {
                        LogUtil.v("home", "load ACache homesound ....");
                        return (HomeSound) obj;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(HomeSound homeSound) {
                super.onPostExecute(homeSound);
                LogUtil.v("home", "showHomeSoundData onPostExecute homeSound "
                        + homeSound);
                if (homeSound == null) {
                    requestHomeSound();
                } else {
                    showHomeSoundView(homeSound);
                }
            }
        }.execute();
    }

    private void requestHomeSound() {
        if (loading_mb != null) {
            loading_mb.setVisibility(View.VISIBLE);
        }
        SoundCloudClient.getHomeSoundRetrofit(mContext).getHomeSound(SoundCloudClient.getClientId())
                .enqueue(new Callback<HomeSound>() {
                    @Override
                    public void onResponse(Call<HomeSound> call, final Response<HomeSound> response) {
                        final HomeSound homeSound = response.body();
                        LogUtil.v("home", " requestHomeSoundData onResponse " + homeSound);
                        showHomeSoundView(homeSound);
                        App.sHomeSound = null;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ACache.get(mContext).put(SoundCloudClient.HOME_SOUND_URL,
                                        homeSound, 60 * 60 * 24 * 5);
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Call<HomeSound> call, Throwable throwable) {
                        throwable.printStackTrace();
                        LogUtil.e("home", " onFailure " + App.sHomeSound);
                        if (App.sHomeSound != null) {
                            showHomeSoundView(App.sHomeSound);
                        } else {
                            showEmptyView();
                        }
                    }
                });
    }


    private void showHomeSoundView(HomeSound homeSound) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        empty.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        if (loading_mb != null) {
            loading_mb.setVisibility(View.INVISIBLE);
        }

        try {
            HomeSoundInAdapter.convertHomeSound(homeSound, mDatas);
            itemTypeAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            mDatas.clear();
            showEmptyView();
        }
    }

    private void showEmptyView() {
        empty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class SingleItemDelagate implements ItemViewDelegate<HomeSoundInAdapter> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_singleitem_layout;
        }

        @Override
        public boolean isForViewType(HomeSoundInAdapter item, int position) {
            return item.type == HomeSoundInAdapter.SINGLE_ITEM_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, final HomeSoundInAdapter homeSoundInAdapter, int position) {
            holder.setText(R.id.single_title_tv, homeSoundInAdapter.contentsBean2.getName());

            Glide.with(mContext).load(homeSoundInAdapter.contentsBean2.getContents().get(0).getAlbum_images()).crossFade()
                    .placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art).centerCrop()
                    .into((ImageView) holder.getView(R.id.item_album_iv22));
            holder.setOnClickListener(R.id.home_single_relative, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatReportUtils.trackCustomEvent("home_page", "singleItem click");
                    HomeSoundListActivity.launch(mContext, homeSoundInAdapter.contentsBean2.getName());
                }
            });
            ((ImageView)holder.getView(R.id.genres_home_item_iv)).setColorFilter(Color.parseColor("#757575"),
                    PorterDuff.Mode.SRC_IN);
        }
    }

    class ListItemDelagate implements ItemViewDelegate<HomeSoundInAdapter> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_list_item_layout;
        }

        @Override
        public boolean isForViewType(HomeSoundInAdapter item, int position) {
            return item.type == HomeSoundInAdapter.LIST_ITEM_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, final HomeSoundInAdapter homeSoundInAdapter, int position) {
            holder.setText(R.id.song_name_tv1, homeSoundInAdapter.list.get(0).title);
            holder.setText(R.id.song_singer_tv1, homeSoundInAdapter.list.get(0).artistName);
            Glide.with(mContext).load(homeSoundInAdapter.list.get(0).getAlbum_images()).crossFade().centerCrop()
                    .placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art)
                    .into((ImageView) holder.getView(R.id.song_image1));
            holder.setOnClickListener(R.id.list_item_linear1, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayerRemote.openQueue(homeSoundInAdapter.list,
                            homeSoundInAdapter.list.get(0).getPosition(), true);
                }
            });

            holder.setText(R.id.song_name_tv2, homeSoundInAdapter.list.get(1).title);
            holder.setText(R.id.song_singer_tv2, homeSoundInAdapter.list.get(1).artistName);
            Glide.with(mContext).load(homeSoundInAdapter.list.get(1).getAlbum_images()).crossFade().centerCrop()
                    .placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art)
                    .into((ImageView) holder.getView(R.id.song_image2));
            holder.setOnClickListener(R.id.list_item_linear2, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayerRemote.openQueue(homeSoundInAdapter.list,
                            homeSoundInAdapter.list.get(1).getPosition(), true);
                }
            });

            holder.setText(R.id.song_name_tv3, homeSoundInAdapter.list.get(2).title);
            holder.setText(R.id.song_singer_tv3, homeSoundInAdapter.list.get(2).artistName);
            Glide.with(mContext).load(homeSoundInAdapter.list.get(2).getAlbum_images()).crossFade().centerCrop()
                    .placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art)
                    .into((ImageView) holder.getView(R.id.song_image3));
            holder.setOnClickListener(R.id.list_item_linear3, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatReportUtils.trackCustomEvent("home_page", "listItem click");
                    MusicPlayerRemote.openQueue(homeSoundInAdapter.list,
                            homeSoundInAdapter.list.get(2).getPosition(), true);
                }
            });
        }
    }

    class TitleItemDelagate implements ItemViewDelegate<HomeSoundInAdapter> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_title_item_layout;
        }

        @Override
        public boolean isForViewType(HomeSoundInAdapter item, int position) {
            return item.type == HomeSoundInAdapter.TITLE_ITEM_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, final HomeSoundInAdapter homeSoundInAdapter, int position) {
            holder.setText(R.id.home_item_title, homeSoundInAdapter.contentsBeanX.getName());
            holder.setOnClickListener(R.id.title_relative, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeSoundListActivity.launch(mContext, homeSoundInAdapter.contentsBeanX.getName());
                }
            });
            if (homeSoundInAdapter.contentsBeanX.getData_type() == 0) {
                holder.getView(R.id.home_back_iv).setVisibility(View.INVISIBLE);
            } else {
                holder.getView(R.id.home_back_iv).setVisibility(View.VISIBLE);
            }
        }
    }

    static class HomeSoundInAdapter {

        public static final int TITLE_ITEM_TYPE = 1;
        public static final int LIST_ITEM_TYPE = 2;
        public static final int SINGLE_ITEM_TYPE = 3;

        public int type;

        public ArrayList<Song> list = new ArrayList<>();

        public HomeSound.ContentsBean2 contentsBean2;

        public HomeSound.ContentsBeanX contentsBeanX;


        public static void convertHomeSound(HomeSound homeSound, ArrayList<HomeSoundInAdapter> homeSoundInAdapters) {
            List<HomeSound.ContentsBeanX> arrayList = homeSound.getContents();

            for (HomeSound.ContentsBeanX contentsBeanX : arrayList) {
                HomeSoundInAdapter homeSoundInAdapter = new HomeSoundInAdapter();
                if (contentsBeanX.getData_type() == 0) {
                    homeSoundInAdapter.type = TITLE_ITEM_TYPE;
                    homeSoundInAdapter.contentsBeanX = new HomeSound.ContentsBeanX();
                    homeSoundInAdapter.contentsBeanX.setName(contentsBeanX.getName());
                    homeSoundInAdapters.add(homeSoundInAdapter);

                    for (HomeSound.ContentsBean2 contentsBean2 : contentsBeanX.contents2) {
                        homeSoundInAdapter = new HomeSoundInAdapter();
                        homeSoundInAdapter.type = SINGLE_ITEM_TYPE;
                        homeSoundInAdapter.contentsBean2 = contentsBean2;
                        homeSoundInAdapters.add(homeSoundInAdapter);
                    }

                } else {

                    homeSoundInAdapter.type = TITLE_ITEM_TYPE;
                    homeSoundInAdapter.contentsBeanX = contentsBeanX;
                    homeSoundInAdapters.add(homeSoundInAdapter);

                    homeSoundInAdapter = new HomeSoundInAdapter();
                    homeSoundInAdapter.type = LIST_ITEM_TYPE;
                    homeSoundInAdapter.list.add(contentsBeanX.contents.get(0));
                    homeSoundInAdapter.list.add(contentsBeanX.contents.get(1));
                    homeSoundInAdapter.list.add(contentsBeanX.contents.get(2));
                    homeSoundInAdapters.add(homeSoundInAdapter);
                }
            }
        }
    }
}
