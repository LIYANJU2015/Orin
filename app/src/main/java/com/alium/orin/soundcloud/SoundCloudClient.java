package com.alium.orin.soundcloud;

import android.content.Context;
import android.util.Base64;

import com.alium.orin.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by liyanju on 2017/11/24.
 */

public class SoundCloudClient {

    public static final String CLIENT_ID = "LegNTza81OuwVaDfYELQW1X71tY1sot8";//8a73a4efdb1f7a965c562f75716551ee //LegNTza81OuwVaDfYELQW1X71tY1sot8

    public static final String HOME_SOUND_URL = "http://navigation.api.hk.goforandroid.com/api/v1/website/navigations/";
    public static final String SOUND_CLOUD_API_URL = "https://api.soundcloud.com";

    private static Retrofit sHomeSoundRF;
    private static Retrofit sSoundCloudRF;

    private static Cache createDefaultCache(Context context) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath(), "/okhttp/");
        if (cacheDir.mkdirs() || cacheDir.isDirectory()) {
            return new Cache(cacheDir, 1024 * 1024 * 10);
        }
        return null;
    }

    public static String getClientId() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("aid", "301fa6b6c195e8a3");
            jSONObject.put("lang", Locale.getDefault().getLanguage());
            jSONObject.put("country", Locale.getDefault().getCountry());
            jSONObject.put("channel", 200);
            jSONObject.put("cversion_number", 83);
            jSONObject.put("cversion_name", "2.1.5");
            jSONObject.put("goid", "1510472516918301fa6b6c195e8a3");
            String id = Base64.encodeToString(jSONObject.toString().getBytes(), 2);
            LogUtil.v("xx", "getClientId" + id);
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static SoundCloudService getSoundCloudRetrofit(Context context) {
        if (sSoundCloudRF == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);


            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(createDefaultCache(context))
                    .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            sSoundCloudRF = new Retrofit.Builder()
                    .baseUrl(SOUND_CLOUD_API_URL)
                    .client(client)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
        }
        return sSoundCloudRF.create(SoundCloudService.class);
    }

    public static SoundCloudService getHomeSoundRetrofit(Context context) {
        if (sHomeSoundRF == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(HomeSound.ContentsBeanX.class, new HomeSoundDeserializer());
            gsonBuilder.registerTypeAdapter(HomeSound.ContentsBean2.class, new ContentsBean2Deserializer());
            Gson gson = gsonBuilder.create();
            GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);


            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(createDefaultCache(context))
                    .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            sHomeSoundRF = new Retrofit.Builder()
                    .baseUrl(HOME_SOUND_URL)
                    .client(client)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
        }
        return sHomeSoundRF.create(SoundCloudService.class);
    }

}
