package com.alium.orin;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.admodule.AdModule;
import com.alium.orin.appshortcuts.DynamicShortcutManager;


import com.alium.orin.soundcloud.ContentsBean2Deserializer;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.HomeSoundDeserializer;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.PreferenceUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;
import com.vincan.medialoader.DefaultConfigFactory;
import com.vincan.medialoader.MediaLoader;
import com.vincan.medialoader.MediaLoaderConfig;
import com.vincan.medialoader.data.file.naming.Md5FileNameCreator;

import org.eclipse.egit.github.core.client.GsonUtils;

import java.io.InputStream;
import java.util.Locale;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends MultiDexApplication implements AdModule.AdCallBack{

    public static final String GOOGLE_PLAY_LICENSE_KEY = "";

    public static Context sContext;

    public static HomeSound sHomeSound;

    public static boolean sIsColdLaunch = false;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.v("App", "onCreate");
        sIsColdLaunch = true;

        sContext = getApplicationContext();

        PreferenceUtil.getInstance(this).setFristTime();

        initLocalHomeSound();

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        ThemeStore.editTheme(this)
                .primaryColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .commit();
        ThemeStore.editTheme(this)
                .accentColor(ContextCompat.getColor(this, R.color.colorAccent))
                .commit();
        initMediaLoader();

        AdModule.init(this);

        CrashReport.initCrashReport(getApplicationContext());

        String appkey = "A77CEYD9IS8G";
        try {
            StatService.startStatService(this, appkey,
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            e.printStackTrace();
        }
    }

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
        return null;
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
        if (language.equals("us") || language.equals("en")) {
            return false;
        }

        if (Math.abs(System.currentTimeMillis() - fristTime) >= 1000 * 60 * 60 * 24 * 5) {
            PreferenceUtil.getInstance(App.sContext).setFristTime();
            return false;
        }

        return true;
    }

    private void initLocalHomeSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
