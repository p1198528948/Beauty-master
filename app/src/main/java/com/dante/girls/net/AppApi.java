package com.dante.girls.net;

import com.dante.girls.model.AppInfo;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by yons on 17/1/3.
 */

public interface AppApi {
    @GET("4/start-image/1080*1920")
    Observable<ResponseBody> getSplash();

    @GET("app.json")
    Observable<AppInfo> getAppInfo();

    @GET
    @Streaming
    Observable<ResponseBody> download(@Url String apkUrl);

}
