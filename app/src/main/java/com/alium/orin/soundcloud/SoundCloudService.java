package com.alium.orin.soundcloud;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liyanju on 2017/11/24.
 */

public interface SoundCloudService {

    @GET("module?product_id=1112&module_id=1474")
    Call<HomeSound> getHomeSound(@Query("client") String client);

    @GET("/tracks?client_id=" + SoundCloudClient.CLIENT_ID)
    Call<List<Track>> getTracks(@Query("q") String query, @Query("limit") int limit);
}
