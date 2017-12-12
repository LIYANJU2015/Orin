package com.alium.orin.youtube;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by liyanju on 2017/12/12.
 */

public interface YoutubeService {


    @GET("music/{region}")
    Call<YouTubeModel> getYoutubeMusic(@Path("region") String region);

}
