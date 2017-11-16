package com.dante.girls.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.girls.R;
import com.dante.girls.base.App;

/**
 * 典型的静态工厂方法，最简单 最实惠
 *
 * loading img encapsulation.
 */
public class Imager {


    public static void loadDefer(final Fragment context, String url, SimpleTarget<Bitmap> target) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.portrait_holder)
                .into(target);
    }

    public static void load(final Fragment context, String url, ImageView target, RequestListener<String, Bitmap> listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.error_holder)
                .animate(R.anim.fade_in)
                .listener(listener)
                .into(target);
    }
    public static void load(final Fragment context, String url, ImageView target) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .crossFade()
                .into(view);
    }


    public static void load(String url, int animationId, ImageView view) {
        Glide.with(App.context)
                .load(url)
                .animate(animationId)
                .into(view);
    }

}
