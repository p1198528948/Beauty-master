package com.dante.girls.net;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yons on 16/12/8.
 */
public interface DBApi {
    @GET("show.htm")
    Observable<ResponseBody> get(@Query("cid") String type, @Query("pager_offset") int page);
    @GET("rank.htm")
    Observable<ResponseBody> getRank(@Query("pager_offset") int page);


}
