package com.dante.girls.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 图片对象
 *
 * 思考为什么要继承于 RealmObject，使用Realm有哪些优势
 *
 * 这里应为图片会被需求保存在本地，如何持久化存储对象，这里给出了答案
 *
 * Image object for save in Realm database.
 */
public class Image extends RealmObject {
    public int id;
    public String type;//Gank or DB
    public Date publishedAt;
    public String info;
    public String title;
    @PrimaryKey
    public String url;
    public int width;
    public int height;
    public boolean isLiked;

    public Image(String url) {
        this.url = url;
    }

    public Image() {
    }

    public Image(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public Image(String url, String type, int id) {
        this.url = url;
        this.type = type;
        this.id = id;
    }

    public static Image getFixedImage(Fragment context, Image image, String type) throws ExecutionException, InterruptedException {
        image.setType(type);
        Bitmap bitmap;
        bitmap = getBitmap(context, image.url);
        image.setWidth(bitmap.getWidth());
        image.setHeight(bitmap.getHeight());
        return image;
    }

    public static void prefetch(Context context, final Image image, String type) {
        image.setType(type);
        Glide.with(context).load(image.url)
                .preload().getSize((width1, height1) -> {
            image.setWidth(width1);
            image.setHeight(height1);
        });
    }

    public static Bitmap getBitmap(Fragment context, String url) throws InterruptedException, ExecutionException {
        return Glide.with(context).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
