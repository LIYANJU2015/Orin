package com.alium.orin.ui.fragments.mainactivity.library.pager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.ui.activities.HomeYouTubeListActivity;
import com.alium.orin.ui.activities.YouTubePlayerActivity;
import com.alium.orin.ui.fragments.AbsMusicServiceFragment;
import com.alium.orin.util.ACache;
import com.alium.orin.util.InstallReferrerReceiver;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.Util;
import com.alium.orin.youtube.YouTubeModel;
import com.alium.orin.youtube.YoutubeClient;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Response;

/**
 * Created by liyanju on 2017/12/12.
 */

public class HomeFragment extends AbsMusicServiceFragment {

    private static final String TAG = "HomeFragment";

    private Unbinder unbinder;

    private static ArrayList<Object> mDatas = new ArrayList<>();

    private MultiItemTypeAdapter itemTypeAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    @BindView(android.R.id.empty)
    RelativeLayout empty;

    @BindView(R.id.app_bar_under_color)
    View behaviourOverLapEmulator;
    @BindView(R.id.loading_mb)
    View loading_mb;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_main_home_view, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        behaviourOverLapEmulator.setBackgroundColor(ThemeStore.primaryColor(getActivity()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        itemTypeAdapter = new MultiItemTypeAdapter(getActivity(), mDatas);
        itemTypeAdapter.addItemViewDelegate(new TitleItemDelagate());
        itemTypeAdapter.addItemViewDelegate(new GridItemDelagate());
        recyclerView.setAdapter(itemTypeAdapter);

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mDatas.size() == 0) {
                            requestHomeData();
                        }
                    }
                });
            }
        });
    }

    private void requestHomeData() {
        new AsyncTask<Void, Void, YouTubeModel>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading_mb.setVisibility(View.VISIBLE);
            }

            @Override
            protected YouTubeModel doInBackground(Void... voids) {
                try {
                    if (App.sYoutubeModel != null && App.isLoadLocalHomeYouTube()) {
                        LogUtil.v(TAG, "loading local youtube data....");
                        return App.sYoutubeModel;
                    }

                    Object cacheObj = ACache.get(mContext).getAsObject(YoutubeClient.HOME_YOUTUBE_URL);
                    if (cacheObj != null && cacheObj instanceof YouTubeModel) {
                        LogUtil.v(TAG, "loading cache youtube data....");
                        return (YouTubeModel)cacheObj;
                    }

                    Response<YouTubeModel> response = YoutubeClient.getYouTubeRetrofit(mContext)
                            .getYoutubeMusic("en").execute();
                    YouTubeModel youTubeModel = response.body();
                    if (youTubeModel != null && youTubeModel.youTubeItems.size() > 0) {
                        ACache.get(mContext).put(YoutubeClient.HOME_YOUTUBE_URL,
                                youTubeModel, 60 * 60 * 24 * 5);
                    }
                    LogUtil.v(TAG, "loading network youtube data....");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(YouTubeModel youTubeModel) {
                super.onPostExecute(youTubeModel);
                if (youTubeModel == null) {
                    youTubeModel = App.sYoutubeModel;
                }
                if (youTubeModel != null) {
                    showHomeData(youTubeModel);
                } else {
                    showEmptyView();
                }
            }
        }.executeOnExecutor(Util.sExecutorService);
    }

    private void showHomeData(YouTubeModel youTubeModel) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        loading_mb.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

        mDatas.clear();
        mDatas.addAll(youTubeModel.youTubeItems);
        itemTypeAdapter.notifyDataSetChanged();
    }

    private void showEmptyView() {
        loading_mb.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public ArrayList<YouTubeModel.YouTubeContent> getContentByName(YouTubeModel.Title title) {
        for (Object obj : mDatas) {
            if (obj instanceof HashMap) {
                ArrayList<YouTubeModel.YouTubeContent> list = (ArrayList<YouTubeModel.YouTubeContent>)
                        ((HashMap)obj).get(title);
                if (list != null) {
                    return list;
                }
            }
        }
        return null;
    }

    class GridItemDelagate implements ItemViewDelegate<Object> {
        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_grid_item_layout;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof HashMap;
        }

        private YouTubeModel.YouTubeContent getContent(ArrayList<YouTubeModel.YouTubeContent> arrayList, int postion) {
            if (arrayList.size() > postion) {
                return arrayList.get(postion);
            }
            return null;
        }

        private void setData(TextView textView, ImageView imageView, View view, int postion,
                             ArrayList<YouTubeModel.YouTubeContent> list) {
            final YouTubeModel.YouTubeContent content = getContent(list, postion);
            if (content != null) {
                view.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(content.icon).crossFade().centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(imageView);
                textView.setText(content.name);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        YouTubePlayerActivity.launch(activity, content.extra, content.name);
                    }
                });
            } else {
                view.setVisibility(View.INVISIBLE);
            }
        }

        private void setData2(TextView textView, ImageView imageView, View view, int postion,
                             ArrayList<YouTubeModel.YouTubeContent> list) {
            final YouTubeModel.YouTubeContent content = getContent(list, postion);
            if (content != null) {
                view.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(content.icon).crossFade().centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(imageView);
                textView.setText(content.name);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        YouTubePlayerActivity.launch(activity, content.extra, content.name);
                    }
                });
            } else {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void convert(ViewHolder holder, Object o, int position) {
            HashMap<YouTubeModel.Title, ArrayList<YouTubeModel.YouTubeContent>> map = (HashMap<YouTubeModel.Title,
                    ArrayList<YouTubeModel.YouTubeContent>>)o;
            YouTubeModel.Title key = map.keySet().iterator().next();
            ArrayList<YouTubeModel.YouTubeContent> arrayList = map.get(key);

            View viewRaw1 = holder.getView(R.id.home_grid_raw1);
            View viewRaw2 = holder.getView(R.id.home_grid_raw2);

            ImageView imageView1 = holder.getView(R.id.song_image1);
            TextView namgeTV1 = holder.getView(R.id.song_name_tv1);
            View view1 = holder.getView(R.id.list_item_linear1);

            ImageView imageView2 = holder.getView(R.id.song_image2);
            TextView namgeTV2 = holder.getView(R.id.song_name_tv2);
            View view2 = holder.getView(R.id.list_item_linear2);

            ImageView imageView3 = holder.getView(R.id.song_image3);
            TextView namgeTV3 = holder.getView(R.id.song_name_tv3);
            View view3 = holder.getView(R.id.list_item_linear3);

            ImageView imageView11 = holder.getView(R.id.song_image11);
            TextView namgeTV11 = holder.getView(R.id.song_name_tv11);
            View view11 = holder.getView(R.id.list_item_linear11);

            ImageView imageView22 = holder.getView(R.id.song_image22);
            TextView namgeTV22 = holder.getView(R.id.song_name_tv22);
            View view22 = holder.getView(R.id.list_item_linear22);

            ImageView imageView33 = holder.getView(R.id.song_image33);
            TextView namgeTV33 = holder.getView(R.id.song_name_tv33);
            View view33 = holder.getView(R.id.list_item_linear33);

            if ("fiveBoxView".equals(key.style) || arrayList.size() < 3) {
                viewRaw2.setVisibility(View.GONE);
                setData2(namgeTV1, imageView1, view1, 0, arrayList);
                setData2(namgeTV2, imageView2, view2, 1, arrayList);
                setData2(namgeTV3, imageView3, view3, 2, arrayList);
                return;
            }

            setData(namgeTV1, imageView1, view1, 0, arrayList);


            setData(namgeTV2, imageView2, view2, 1, arrayList);

            setData(namgeTV3, imageView3, view3, 2, arrayList);

            if (arrayList.size() <= 3) {
                viewRaw2.setVisibility(View.GONE);
                return;
            }

            viewRaw2.setVisibility(View.VISIBLE);

            setData(namgeTV11, imageView11, view11, 3, arrayList);

            setData(namgeTV22, imageView22, view22, 4, arrayList);

            setData(namgeTV33, imageView33, view33, 5, arrayList);
        }
    }

    class TitleItemDelagate implements ItemViewDelegate<Object> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.home_title_item_layout;
        }

        @Override
        public boolean isForViewType(Object item, int position) {
            return item instanceof YouTubeModel.Title;
        }

        @Override
        public void convert(ViewHolder holder, final Object object, int position) {
            final YouTubeModel.Title title = (YouTubeModel.Title) object;
            holder.setText(R.id.home_item_title, title.name);
            holder.setOnClickListener(R.id.title_relative, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeYouTubeListActivity.launch(activity, title.name, getContentByName(title));
                }
            });
        }
    }

}
