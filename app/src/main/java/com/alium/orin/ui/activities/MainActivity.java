package com.alium.orin.ui.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.admodule.AdModule;
import com.alium.orin.R;
import com.alium.orin.dialogs.ChangelogDialog;
import com.alium.orin.glide.SongGlideRequest;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.helper.SearchQueryHelper;
import com.alium.orin.loader.AlbumLoader;
import com.alium.orin.loader.ArtistSongLoader;
import com.alium.orin.loader.PlaylistSongLoader;
import com.alium.orin.model.Song;
import com.alium.orin.service.MusicService;
import com.alium.orin.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.alium.orin.ui.activities.intro.AppIntroActivity;
import com.alium.orin.ui.fragments.mainactivity.folders.FoldersFragment;
import com.alium.orin.ui.fragments.mainactivity.library.LibraryFragment;
import com.alium.orin.util.PreferenceUtil;
import com.alium.orin.util.Util;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.rating.RatingActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vincan.medialoader.DefaultConfigFactory;
import com.vincan.medialoader.MediaLoader;
import com.vincan.medialoader.MediaLoaderConfig;
import com.vincan.medialoader.data.file.naming.Md5FileNameCreator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AbsSlidingMusicPanelActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int APP_INTRO_REQUEST = 100;

    private static final int LIBRARY = 0;
    private static final int FOLDERS = 1;
    private static final int EQUALIZER = 2;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    @Nullable
    private View navigationDrawerHeader;

    private boolean blockRequestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
            drawerLayout.setFitsSystemWindows(false);
            navigationView.setFitsSystemWindows(false);
            //noinspection ConstantConditions
            findViewById(R.id.drawer_content_container).setFitsSystemWindows(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawerLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    navigationView.dispatchApplyWindowInsets(windowInsets);
                    return windowInsets.replaceSystemWindowInsets(0, 0, 0, 0);
                }
            });
        }

        setUpDrawerLayout();

        if (savedInstanceState == null) {
            setMusicChooser(PreferenceUtil.getInstance(this).getLastMusicChooser());
        } else {
            restoreCurrentFragment();
        }

        if (!checkShowIntro()) {
            checkShowChangelog();
        }

        AdModule.getInstance().getAdMob().initInterstitialAd();
        AdModule.getInstance().getAdMob().requestNewInterstitial();

        isShowRating = PreferenceUtil.getInstance(this).isShowRating();
    }

    private void setMusicChooser(int key) {
        if (key != EQUALIZER) {
            PreferenceUtil.getInstance(this).setLastMusicChooser(key);
        }
        switch (key) {
            case LIBRARY:
                navigationView.setCheckedItem(R.id.nav_library);
                setCurrentFragment(LibraryFragment.newInstance());
                break;
            case FOLDERS:
                navigationView.setCheckedItem(R.id.nav_folders);
                setCurrentFragment(FoldersFragment.newInstance(this));
                break;
            case EQUALIZER:
                int sessionId = MusicPlayerRemote.getAudioSessionId();
                if (sessionId != AudioEffect.ERROR_BAD_VALUE && sessionId != -1) {
                    navigationView.setCheckedItem(R.id.equalizer_item);
                    EqualizerActivity.launch(this);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_audio_ID),
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void setCurrentFragment(@SuppressWarnings("NullableProblems") Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    private void restoreCurrentFragment() {
        currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false;
            if (!hasPermissions()) {
                requestPermissions();
            }
        }
    }

    private boolean isShowRating = false;
    private boolean isShow = false;
    @Override
    public void onBackPressed() {
        if (isShow) {
            return;
        }

        if (isShowRating) {
            isShow = true;
            Util.runSingleThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isShow = false;
                    isShowRating = false;
                    PreferenceUtil.getInstance(getApplicationContext()).notShowRating();
                }
            });
            RatingActivity.launch(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions();
    }

    @Override
    protected View createContentView() {
        @SuppressLint("InflateParams")
        View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = ButterKnife.findById(contentView, R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));
        return contentView;
    }

    private void setUpNavigationView() {
        if (navigationDrawerHeader == null) {
            navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
            //noinspection ConstantConditions
            navigationDrawerHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawers();
                    if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        expandPanel();
                    }
                }
            });
            Drawable defaultHeader = getResources().getDrawable(R.drawable.icon_drawer_theme_bg);
            defaultHeader.setColorFilter(ThemeStore.primaryColor(getApplicationContext()),
                    PorterDuff.Mode.DARKEN);
            ((ImageView)navigationDrawerHeader.findViewById(R.id.image)).setImageDrawable(defaultHeader);
        }

        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(navigationView, ATHUtil.resolveColor(this, R.attr.iconColor, ThemeStore.textColorSecondary(this)), accentColor);
        NavigationViewUtil.setItemTextColors(navigationView, ThemeStore.textColorPrimary(this), accentColor);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.equalizer_item:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setMusicChooser(EQUALIZER);
                            }
                        }, 200);
                        break;
                    case R.id.nav_library:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setMusicChooser(LIBRARY);
                            }
                        }, 200);
                        break;
                    case R.id.nav_folders:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setMusicChooser(FOLDERS);
                            }
                        }, 200);
                        break;
//                    case R.id.support_development:
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                DonationsDialog.create().show(getSupportFragmentManager(), "DONATION_DIALOG");
//                            }
//                        }, 200);
//                        break;
                    case R.id.nav_settings:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            }
                        }, 200);
                        break;
                    case R.id.nav_exit:
                        finish();
                        break;
                }
                return true;
            }
        });
    }

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        if (navigationDrawerHeader == null) {
            return;
        }
        Drawable defaultHeader = getResources().getDrawable(R.drawable.icon_drawer_theme_bg);
        defaultHeader.setColorFilter(ThemeStore.primaryColor(getApplicationContext()), PorterDuff.Mode.DARKEN);
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            Song song = MusicPlayerRemote.getCurrentSong();
            ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
            ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(song.artistName);
            SongGlideRequest.Builder.from(Glide.with(this), song)
                    .checkIgnoreMediaStore(this).build()
                    .into(((ImageView) navigationDrawerHeader.findViewById(R.id.image)));
        } else {
            ((ImageView)navigationDrawerHeader.findViewById(R.id.image)).setImageDrawable(defaultHeader);
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateNavigationDrawerHeader();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateNavigationDrawerHeader();
        handlePlaybackIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final ArrayList<Song> songs = SearchQueryHelper.getSongs(this, intent.getExtras());
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true);
            }
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                ArrayList<Song> songs = new ArrayList<>();
                songs.addAll(PlaylistSongLoader.getPlaylistSongList(this, id));
                MusicPlayerRemote.openQueue(songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(this, id).songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistSongLoader.getArtistSongList(this, id), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private boolean checkShowIntro() {
//        if (!PreferenceUtil.getInstance(this).introShown()) {
//            PreferenceUtil.getInstance(this).setIntroShown();
//            ChangelogDialog.setChangelogRead(this);
//            blockRequestPermissions = true;
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), APP_INTRO_REQUEST);
//                }
//            }, 50);
//            return true;
//        }
        return false;
    }

    private boolean checkShowChangelog() {
//        try {
//            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            int currentVersion = pInfo.versionCode;
//            if (currentVersion != PreferenceUtil.getInstance(this).getLastChangelogVersion()) {
//                ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGE_LOG_DIALOG");
//                return true;
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        return false;
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }
}