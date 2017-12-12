package com.alium.orin.youtube;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by liyanju on 2017/12/12.
 */

public class YoutubeClient {

    private static Retrofit sYoutubeClient;

    public static final String HOME_YOUTUBE_URL = "http://api.ahameet.com/api/";

    private static Cache createDefaultCache(Context context) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath(), "/okhttp/");
        if (cacheDir.mkdirs() || cacheDir.isDirectory()) {
            return new Cache(cacheDir, 1024 * 1024 * 10);
        }
        return null;
    }

    public static YoutubeService getYouTubeRetrofit(Context context) {
        if (sYoutubeClient == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(YouTubeModel.class, new YouTubeModelDeseializer());
            Gson gson = gsonBuilder.create();
            GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);


            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(createDefaultCache(context))
                    .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            sYoutubeClient = new Retrofit.Builder()
                    .baseUrl(HOME_YOUTUBE_URL)
                    .client(client)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
        }
        return sYoutubeClient.create(YoutubeService.class);
    }
}
