package com.dante.girls.net;

import com.dante.girls.model.Image;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yons on 16/12/8.
 */

public interface GankApi {
    @GET("data/%E7%A6%8F%E5%88%A9/{count}/{page}")
    Observable<Result<List<Image>>> get(@Path("count")int count, @Path("page")int page);

    class Result<T>{
        public boolean error;
        public T results;
    }
}
