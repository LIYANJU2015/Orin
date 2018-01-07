package com.alium.orin.ui.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alium.orin.loader.AlbumLoader;
import com.alium.orin.loader.ArtistLoader;
import com.alium.orin.soundcloud.SoundCloudClient;
import com.alium.orin.soundcloud.Track;
import com.alium.orin.util.LogUtil;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.alium.orin.R;
import com.alium.orin.adapter.SearchAdapter;
import com.alium.orin.interfaces.LoaderIds;
import com.alium.orin.loader.SongLoader;
import com.alium.orin.misc.WrappedAsyncTaskLoader;
import com.alium.orin.ui.activities.base.AbsMusicServiceActivity;
import com.alium.orin.util.Util;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemCreator;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Object>> {
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY = "query";
    private static final int LOADER_ID = LoaderIds.SEARCH_ACTIVITY;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    RelativeLayout empty;

    SearchView searchView;

    private SearchAdapter adapter;
    private String query;

    private Context mContext;

    private Paginate mPaginate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = getApplicationContext();
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this, Collections.emptyList());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        mPaginate = Paginate.with(recyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .build();
        mPaginate.setHasMoreDataToLoad(false);

        setUpToolBar();

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY);
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private boolean isLoading;
    private boolean isLoaded = true;

    Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {

        }

        @Override
        public boolean isLoading() {
            return isLoading;
        }

        @Override
        public boolean hasLoadedAllItems() {
            return isLoaded;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        MenuItemCompat.expandActionView(searchItem);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                try {
                    onBackPressed();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        searchView.setQuery(query, false);
        searchView.post(new Runnable() {
            @Override
            public void run() {
                searchView.setOnQueryTextListener(SearchActivity.this);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        cancelQuerySoundCloud();
        this.query = query;
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        cancelQuerySoundCloud();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search(newText);
        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mQueryAsyncTask != null) {
            mQueryAsyncTask.cancel(true);
        }
        mPaginate.unbind();
    }

    private AsyncTask mQueryAsyncTask;

    private void cancelQuerySoundCloud() {
        if (mQueryAsyncTask != null) {
            mQueryAsyncTask.cancel(true);
        }
    }

    public void querySoundCloud(final String query, final List<Object> results) {
        cancelQuerySoundCloud();

        mQueryAsyncTask = new AsyncTask<Void, Void,List<Track>>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                isLoading = true;
                isLoaded = false;
                mPaginate.setHasMoreDataToLoad(true);
            }

            @Override
            protected List<Track> doInBackground(Void... voids) {
                try {
                    Response<List<Track>> response = SoundCloudClient.getSoundCloudRetrofit(mContext).getTracks(query, 50).execute();
                    return response.body();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Track> tracks) {
                super.onPostExecute(tracks);
                if (isCancelled()){
                    return;
                }

                isLoading = false;
                isLoaded = true;
                mPaginate.setHasMoreDataToLoad(false);

                if (tracks != null && !tracks.isEmpty() && results != null) {
                    for (int i = 0; i < results.size(); i++) {
                        Object object = results.get(i);
                        if (object instanceof String
                                && ((String)object).equals(mContext.getResources()
                                .getString(R.string.sound_cloud))) {
                            results.add(i+1, mContext.getResources().getString(R.string.sound_cloud));
                            results.addAll(i + 2, tracks);
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }
                    results.add(mContext.getResources().getString(R.string.sound_cloud));
                    results.addAll(tracks);
                    adapter.notifyDataSetChanged();
                }
            }
        }.executeOnExecutor(Util.sExecutorService);
    }

    @Override
    public Loader<List<Object>> onCreateLoader(int id, Bundle args) {
        return new AsyncSearchResultLoader(this,this, query);
    }

    @Override
    public void onLoadFinished(Loader<List<Object>> loader, List<Object> data) {
        adapter.swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Object>> loader) {
        adapter.swapDataSet(Collections.emptyList());
    }

    private static class AsyncSearchResultLoader extends WrappedAsyncTaskLoader<List<Object>> {
        private final String query;
        private SearchActivity searchActivity;

        public AsyncSearchResultLoader(SearchActivity searchActivity, Context context, String query) {
            super(context);
            this.query = query;
            this.searchActivity = searchActivity;
        }

        @Override
        public List<Object> loadInBackground() {
            final List<Object> results = new ArrayList<>();
            if (!TextUtils.isEmpty(query)) {
                List songs = SongLoader.getSongs(getContext(), query);
                if (!songs.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.songs));
                    results.addAll(songs);
                }

                List artists = ArtistLoader.getArtists(getContext(), query);
                if (!artists.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.artists));
                    results.addAll(artists);
                }

                List albums = AlbumLoader.getAlbums(getContext(), query);
                if (!albums.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.albums));
                    results.addAll(albums);
                }
                searchActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchActivity.querySoundCloud(query, results);
                    }
                });
            }
            return results;
        }
    }
}
