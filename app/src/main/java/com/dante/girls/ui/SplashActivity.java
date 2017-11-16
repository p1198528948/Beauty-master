package com.dante.girls.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.net.API;
import com.dante.girls.net.NetService;
import com.dante.girls.utils.DateUtil;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.SpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import rx.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1800;
    private static final int SPLASH_DURATION_SHORT = 1200;
    private static final String SPLASH = "splash";

    public static int FULL_SCREEN_UI = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_UI);

        if (SpUtil.getBoolean(SettingFragment.ORIGINAL_SPLASH)) {
            getWindow().setBackgroundDrawableResource(R.drawable.window_background);
            startAppDelay(SPLASH_DURATION_SHORT);
            return;
        }
        updateSplash();
        loadImage();
    }

    //Update url of splash, no load
    private void updateSplash() {
        today = DateUtil.parseStandardDate(new Date());
        if (today.equals(SpUtil.getString(Constants.DATE))
                && !SpUtil.get(SPLASH, "").isEmpty()) {
            return;
        }
        NetService.getInstance(API.SPLASH).getAppApi().getSplash()
                .map(responseBody -> {
                    try {
                        return new JSONObject(responseBody.string()).getString("img");
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    return "";
                })
                .subscribeOn(Schedulers.io())
                .subscribe(url -> {
                    SpUtil.save(SPLASH, url);
                    SpUtil.save(Constants.DATE, today);
                });


    }

    private void loadImage() {
        ImageView splash = new ImageView(this);
        splash.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setContentView(splash);

        String url = SpUtil.get(SPLASH, "");
        if (url.isEmpty()) {
            Imager.load(this, R.drawable.splash, splash);
            startAppDelay(SPLASH_DURATION_SHORT);
        } else {
            Imager.load(url, R.anim.splash_anim, splash);
            startAppDelay(SPLASH_DURATION);
        }
    }

    private void startAppDelay(int delay) {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, delay);
    }

    @Override
    public void onBackPressed() {
        //disable back button when showing splash
    }

}
