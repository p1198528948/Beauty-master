package com.dante.girls.net;

import android.util.Log;

import com.dante.girls.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Net API services.
 */

public class NetService {

    public static final String TAG = "NetService";

    private static NetService instance;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//            .setExclusionStrategies(new ExclusionStrategy() {
//                @Override
//                public boolean shouldSkipField(FieldAttributes f) {
//                    return f.getDeclaringClass().equals(RealmObject.class);
//                }
//
//                @Override
//                public boolean shouldSkipClass(Class<?> clazz) {
//                    return false;
//                }
//            })
            .create();
    private Retrofit retrofit;
    private String baseUrl;
    private OkHttpClient client;
    private GankApi gankApi;
    private DBApi dbApi;
    private AApi aApi;
    private AppApi appApi;

    //单例模式
    public static NetService getInstance(String baseUrl) {
        if (instance == null) {
            instance = new NetService();
            /** 该拦截器用于记录应用中的网络请求的信息 **/
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            /** 设置日志是否打印，发布版不打印 NONE 不记录**/
            logging.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);

            instance.client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                                .method(original.method(), original.body())
                                .build();

                        Log.e(TAG,request.toString());

                        return chain.proceed(request);
                    })
                    .addInterceptor(logging).build();


        }
        instance.baseUrl = baseUrl;
        return instance;
    }

    /**  得到 干货集中营  API **/
    public GankApi getGankApi() {
        if (gankApi == null) {
            gankApi = createServiceWithGson(GankApi.class);
        }
        return gankApi;
    }

    /** 得到豆瓣API **/
    public DBApi getDbApi() {
        if (dbApi == null) {
            dbApi = createService(DBApi.class);
        }
        return dbApi;
    }

    /**  得到 动漫API ***/
    public AApi getaApi() {
        if (aApi == null) {
            aApi = createService(AApi.class);
        }
        return aApi;
    }

    /****
     * 这个我们重点关注 ，App 的 信息在这里
     * @return
     */
    public AppApi getAppApi() {
        return createServiceWithGson(AppApi.class);
    }

    public <T> T createService(Class<T> className) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(className);
    }

    public <T> T createServiceWithGson(Class<T> className) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(className);
    }
}
