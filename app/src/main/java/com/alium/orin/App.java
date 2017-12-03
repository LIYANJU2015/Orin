package com.alium.orin;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.alium.orin.appshortcuts.DynamicShortcutManager;


import com.alium.orin.soundcloud.ContentsBean2Deserializer;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.HomeSoundDeserializer;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.PreferenceUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kabouzeid.appthemehelper.ThemeStore;

import org.eclipse.egit.github.core.client.GsonUtils;

import java.io.InputStream;
import java.util.Locale;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends Application {

    public static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjMeADN5Ffnt/ml5SYxNPCn8kGcOYGpHEfNSCts99vVxqmCn6C01E94c17j7rUK2aeHur5uxphZylzopPlQ8P8l1fqty0GPUNRSo18FCJzfGH8HZAwZYOcnRFPaXdaq3InyFJhBiODh2oeAcVK/idH6QraQ4r9HIlzigAg6lgwzxl2wJKDh7X/GMdDntCyzDh8xDQ0wIawFgvgojHwqh2Ci8Gnq6EYRwPA9yHiIIksT8Q30QyM5ewl5QcnWepsls7enNqeHarhpmSibRUDgCsxHoOpny7SyuvZvUI3wuLckDR0ds9hrt614scHHqDOBp/qWCZiAgOPVAEQcURbV09qQIDAQAB";

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
    }

    public static boolean isLoadLocalHomeSound() {
        String language = Locale.getDefault().getLanguage().toLowerCase();
        long fristTime = PreferenceUtil.getInstance(App.sContext).getFristTime();
        if (language.equals("us") && language.equals("en")) {
            return false;
        }

        if (Math.abs(System.currentTimeMillis() - fristTime) >= 1000 * 60 * 60 * 24 * 5) {
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
