package com.dante.girls.net;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import rx.Observable;

/**
 * Created by yons on 16/12/16.
 */

public interface TestApi {
    @Headers("User-Agent: Mozilla/5.0")
    @GET("page/2")
    Observable<ResponseBody> get();

}
