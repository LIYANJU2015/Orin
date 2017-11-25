package com.alium.orin.ui.fragments.mainactivity.library.pager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.model.Song;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.SoundCloundClient;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.ACache;
import com.alium.orin.util.LogUtil;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.eclipse.egit.github.core.client.GsonUtils;

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

public class HomeFragment extends AbsMusicServiceFragment{

    private Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    @BindView(android.R.id.empty)
    TextView empty;

    @BindView(R.id.app_bar_under_color)
    View behaviourOverLapEmulator;

    private static ArrayList<HomeSoundInAdapter> mDatas = new ArrayList<>();

    private MultiItemTypeAdapter itemTypeAdapter;

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


        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        itemTypeAdapter = new MultiItemTypeAdapter(mContext, mDatas);
        itemTypeAdapter.addItemViewDelegate(new TitleItemDelagate());
        itemTypeAdapter.addItemViewDelegate(new ListItemDelagate());
        itemTypeAdapter.addItemViewDelegate(new SingleItemDelagate());
        recyclerView.setAdapter(itemTypeAdapter);

        if (mDatas.size() == 0) {
            showHomeSoundData();
        }
    }

    private void showHomeSoundData() {
        new AsyncTask<Void, Void, HomeSound>() {
            @Override
            protected HomeSound doInBackground(Void... voids) {
                String body = ACache.get(mContext).getAsString(SoundCloundClient.HOME_SOUND_URL);
                if (!TextUtils.isEmpty(body)) {
                    try {
                        return GsonUtils.fromJson(body, HomeSound.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        SoundCloundClient.getHomeSoundRetrofit(mContext).getHomeSound(SoundCloundClient.getClientId())
                .enqueue(new Callback<HomeSound>() {
                    @Override
                    public void onResponse(Call<HomeSound> call, final Response<HomeSound> response) {
                        HomeSound homeSound = response.body();
                        LogUtil.v("home", " requestHomeSoundData onResponse " + homeSound);
                        showHomeSoundView(homeSound);
                        App.sHomeSound = null;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ACache.get(mContext).put(SoundCloundClient.HOME_SOUND_URL,
                                        response.message(), 60*60*24*5);
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Call<HomeSound> call, Throwable throwable) {
                        throwable.printStackTrace();
                        if (App.sHomeSound != null) {
                            showHomeSoundView(App.sHomeSound);
                        } else {
                            showEmptyView();
                        }
                    }
                });
    }


    private void showHomeSoundView(HomeSound homeSound) {
        empty.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

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
        public void convert(ViewHolder holder, HomeSoundInAdapter homeSoundInAdapter, int position) {
            holder.setText(R.id.single_title_tv, homeSoundInAdapter.contentsBean2.getName());

            Glide.with(mContext).load(homeSoundInAdapter.contentsBean2.getContents().get(0).getAlbum_images()).crossFade()
                    .placeholder(R.drawable.default_album_art).error(R.drawable.default_album_art).centerCrop()
                    .into((ImageView) holder.getView(R.id.item_album_iv));
            holder.setOnClickListener(R.id.home_single_relative, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
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
                    MusicPlayerRemote.openQueue(homeSoundInAdapter.list,
                            homeSoundInAdapter.list.get(2).getPosition(), true);
                }
            });
        }
    }

    static class TitleItemDelagate implements ItemViewDelegate<HomeSoundInAdapter> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_title_item_layout;
        }

        @Override
        public boolean isForViewType(HomeSoundInAdapter item, int position) {
            return item.type == HomeSoundInAdapter.TITLE_ITEM_TYPE;
        }

        @Override
        public void convert(ViewHolder holder, HomeSoundInAdapter homeSoundInAdapter, int position) {
            holder.setText(R.id.home_item_title, homeSoundInAdapter.contentsBeanX.getName());
            holder.setOnClickListener(R.id.title_relative, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
