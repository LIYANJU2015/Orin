package com.alium.orin;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.admodule.AdModule;
import com.alium.orin.appshortcuts.DynamicShortcutManager;


import com.alium.orin.soundcloud.ContentsBean2Deserializer;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.HomeSoundDeserializer;
import com.alium.orin.util.FacebookReport;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.PreferenceUtil;
import com.alium.orin.util.Util;
import com.alium.orin.youtube.YouTubeModel;
import com.alium.orin.youtube.YouTubeModelDeseializer;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.tencent.bugly.crashreport.CrashReport;

import com.vincan.medialoader.DefaultConfigFactory;
import com.vincan.medialoader.MediaLoader;
import com.vincan.medialoader.MediaLoaderConfig;
import com.vincan.medialoader.data.file.naming.Md5FileNameCreator;

import org.eclipse.egit.github.core.client.GsonUtils;

import java.io.InputStream;
import java.util.Locale;

import ren.yale.android.cachewebviewlib.utils.NetworkUtils;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends MultiDexApplication implements AdModule.AdCallBack{

    public static final String GOOGLE_PLAY_LICENSE_KEY = "";

    public static Context sContext;

    public static HomeSound sHomeSound;

    public static YouTubeModel sYoutubeModel;

    public static boolean sIsColdLaunch = false;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.v("App", "onCreate");
        sIsColdLaunch = true;

        sContext = getApplicationContext();

        PreferenceUtil.getInstance(this).setFristTime();

        initLocalHomeYoutube();

        initLocalHomeSound();

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        Util.initRecommend();

        ThemeStore.editTheme(this)
                .primaryColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .commit();
        ThemeStore.editTheme(this)
                .accentColor(ContextCompat.getColor(this, R.color.colorAccent))
                .commit();
        initMediaLoader();

        AdModule.init(this);

        CrashReport.initCrashReport(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        fetchCount = PreferenceUtil.getInstance(sContext).getReportDeepLinkCount();
        if (fetchCount < 3 && NetworkUtils.isConnected(sContext)) {
            LogUtil.v("xx", " start onDeferredAppLinkDataFetched>>>>");
            AppLinkData.fetchDeferredAppLinkData(sContext, getString(R.string.facebook_app_id),
                    new AppLinkData.CompletionHandler() {
                        @Override
                        public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                            PreferenceUtil.getInstance(sContext).setReportDeepLinkCount(fetchCount + 1);
                            LogUtil.v("xx", " onDeferredAppLinkDataFetched>>>>");
                            //musicplus://mymusic/6666
                            if (appLinkData != null && appLinkData.getTargetUri() != null) {
                                LogUtil.v("xx", " onDeferredAppLinkDataFetched111>>>>");
                                String deepLinkStr = appLinkData.getTargetUri().toString();
                                FacebookReport.logAppLinkDataFetched(deepLinkStr);
                            }
                        }
                    });
        }

        if (PreferenceUtil.getInstance(this).getAdProtactTime() == 0) {
            PreferenceUtil.getInstance(this).saveAdProtactTime();
        }
    }

    private int fetchCount;

    @Override
    public Application getApplication() {
        return this;
    }

    @Override
    public String getAppId() {
        return "ca-app-pub-9880857526519562~4095916127";
    }

    @Override
    public boolean isAdDebug() {
        return false;
    }

    @Override
    public boolean isLogDebug() {
        return false;
    }

    @Override
    public String getAdMobNativeAdId() {
        return null;
    }

    @Override
    public String getBannerAdId() {
        return "ca-app-pub-9880857526519562/6984893216";
    }

    @Override
    public String getInterstitialAdId() {
        return "ca-app-pub-9880857526519562/8833237480";
    }

    @Override
    public String getTestDevice() {
        return "0d4f2b7d0465433d40ba2114ac067612";
    }

    @Override
    public String getRewardedVideoAdId() {
        return null;
    }

    @Override
    public String getFBNativeAdId() {
        return "1305172892959949_1308865969257308";
    }

    private void initMediaLoader() {
        MediaLoaderConfig mediaLoaderConfig = new MediaLoaderConfig.Builder(this)
                .cacheRootDir(DefaultConfigFactory.createCacheRootDir(this))
                .cacheFileNameGenerator(new Md5FileNameCreator())
                .maxCacheFilesCount(100)
                .maxCacheFilesSize(100 * 1024 * 1024)
                .maxCacheFileTimeLimit(5 * 24 * 60 * 60)
                .downloadThreadPoolSize(3)
                .downloadThreadPriority(Thread.NORM_PRIORITY)
                .build();
        MediaLoader.getInstance(this).init(mediaLoaderConfig);
    }

    public static boolean isLoadLocalHomeSound() {
        String language = Locale.getDefault().getLanguage().toLowerCase();
        long fristTime = PreferenceUtil.getInstance(App.sContext).getFristTime();
        boolean result = true;
        if ((!language.equals("us") || !language.equals("en")) && fristTime == 0) {
            result = false;
        }

        if (fristTime != 0 && Math.abs(System.currentTimeMillis() - fristTime) >= 1000 * 60 * 60 * 24 * 5) {
            result = false;
        }

        PreferenceUtil.getInstance(App.sContext).setFristTime();

        return result;
    }

    public static boolean isLoadLocalHomeYouTube() {
        long fristTime = PreferenceUtil.getInstance(App.sContext).getFristTime();
        boolean result = true;
        if (fristTime != 0 && Math.abs(System.currentTimeMillis() - fristTime) >= 1000 * 60 * 60 * 24 * 10) {
            PreferenceUtil.getInstance(App.sContext).setFristTime();
            result = false;
        }

        return result;
    }

    private void initLocalHomeYoutube() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    InputStream is = getAssets().open("ahameet.txt");
                    int lenght = is.available();
                    byte[]  buffer = new byte[lenght];
                    is.read(buffer);
                    String result = new String(buffer, "utf8");
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(YouTubeModel.class, new YouTubeModelDeseializer());
                    Gson gson = gsonBuilder.create();
                    sYoutubeModel = gson.fromJson(result, YouTubeModel.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initLocalHomeSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    InputStream is = getAssets().open("homesound.txt");
                    int lenght = is.available();
                    byte[]  buffer = new byte[lenght];
                    is.read(buffer);
                    String result = new String(buffer, "utf8");
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(HomeSound.ContentsBeanX.class, new HomeSoundDeserializer());
                    gsonBuilder.registerTypeAdapter(HomeSound.ContentsBean2.class, new ContentsBean2Deserializer());
                    Gson gson = gsonBuilder.create();
                    sHomeSound = gson.fromJson(result, HomeSound.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
