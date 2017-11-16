package com.dante.girls.net;

import android.util.Log;

import com.dante.girls.model.DataBase;
import com.dante.girls.model.Image;
import com.dante.girls.picture.CustomPictureFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.dante.girls.net.API.TYPE_DB_RANK;

/**
 * Help to get&fetch image resources from api
 */

public class DataFetcher {
    private final NetService netService;
    public String url; //source url
    public String type; //picture type
    private int page;

    public DataFetcher(String url, String type, int page) {
        this.url = url;
        this.type = type;
        this.page = page;
        netService = NetService.getInstance(url);

    }

    public Observable<List<Image>> getGank() {
        return netService.getGankApi().get(CustomPictureFragment.LOAD_COUNT, page)
                .subscribeOn(Schedulers.computation())
                .filter(listResult -> !listResult.error)
                .map(listResult -> {
                    List<Image> data = new ArrayList<>();
                    for (Image image :
                            listResult.results) {
                        if (!DataBase.hasImage(null, image.url)) {
                            data.add(image);
                        } else {

                        }
                    }
                    return data;
                });
    }

    public Observable<List<Image>> getDouban() {
        Observable<ResponseBody> data;
        if (TYPE_DB_RANK.equals(type)) {
            data = netService.getDbApi().getRank(page);
        } else {
            data = netService.getDbApi().get(type, page);
        }
        return data
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
//                            Elements elements = document.select("div[class=thumbnail] > div[class=img_single] > a > img");
                        Elements elements = document.select("div[class=thumbnail] div[class=img_single] img");
                        final int size = elements.size();
                        for (int i = 0; i < size; i++) {
                            String src = elements.get(i).attr("src").trim();
                            if (DataBase.hasImage(null, src)) {
                                continue;
                            }
                            images.add(new Image(src, type));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });
    }


    public Observable<List<Image>> getAPosts() {
        //get all posts' pictures
        return netService.getaApi().getPosts(type, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        Elements elements = document.select("div[class=content]  a");
//                            final int size = elements.size();
                        Log.i(TAG, "getAPosts: " + type + elements.size());
//                            String url = elements.last().select("img").first().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getAPosts: find saved image!");
//                                return null;//最后一个元素在数据库里已经保存了，那么不需要继续解析。
//                            }

                        for (int i = 0; i < elements.size(); i++) {
                            Element link = elements.get(i);
                            String postUrl = link.attr("href").replace(API.A_BASE, "");
                            String title = link.attr("title");
                            String src = link.select("img").first().attr("src").trim();
//                                if (src.endsWith("!thumb")) {
//                                    Log.i(TAG, "load image: thumb url " + src);
//                                    src = src.replace("!thumb", "");
//                                }
                            if (DataBase.hasImage(null, src)) {
                                continue;
                            }
                            Image image = new Image(src, type);
                            image.setInfo(postUrl);
                            image.setTitle(title);
                            images.add(image);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });


    }

    public Observable<List<Image>> getPicturesOfPost(String info) {
        //get all images in this post
        Log.d(TAG, "getPicturesOfPost : " + info);
        return netService.getaApi().getPictures(info, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        Elements elements = document.select("div[class=post] p img");
                        Elements test = document.select("div[class=post] > p > a > img");
                        final int size = elements.size();
                        Log.d(TAG, "call: test " + test.size());
//                            String url = elements.last().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getPicturesOfPost: find saved image!");
//                                return null;
//                            }
                        for (int i = 0; i < size; i++) {
                            String src = elements.get(i).attr("src").trim();
                            if (DataBase.hasImage(null, src)) {
                                continue;
                            }
                            Image image = new Image(src, type);
                            images.add(image);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });

    }

}
