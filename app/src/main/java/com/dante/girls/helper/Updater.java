package com.dante.girls.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import com.dante.girls.BuildConfig;
import com.dante.girls.R;
import com.dante.girls.model.AppInfo;
import com.dante.girls.net.API;
import com.dante.girls.net.NetService;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.utils.SpUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 为您的APP增加 检查更新    下载更新APK功能
 *
 * Update app helper.
 */

public class Updater {

    public static final String SHARE_APP = "share_app";
    private static Subscription subscription;
    private final Activity context;
    private DownloadHelper helper;

    private Updater(Activity context) {
        this.context = context;
    }

    public static Updater getInstance(Activity context) {
        return new Updater(context);

    }

    private void downloadAndInstall(final AppInfo appInfo) {
        subscription = new RxPermissions(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(granted -> granted)
                .subscribe(aBoolean -> {
                    String url = API.DOWNLOAD_BASE + "/" + appInfo.getVersion() +
                            "/" + appInfo.getApkName();
                    helper = new DownloadHelper(context, url);
                    helper.downWithDownloadManager(getApkName(appInfo.getVersion()), getApkName(appInfo.getFormerVersion()));
                });
    }
    /**
     * 检查版本是否有更新
     *
     * 这里我们简单了解一下，更新检测机制
     */
    public void check() {
        NetService.getInstance(API.GITHUB_RAW).getAppApi().getAppInfo()
                .filter(appInfo -> {
                    return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                })
                .doOnNext(appInfo -> SpUtil.save(Updater.SHARE_APP, appInfo.getShareApp()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showDialog, Throwable::printStackTrace);
    }

    /**
     * 这里会更新新的版本
     * @param appInfo
     */
    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate() || SpUtil.getBoolean(SettingFragment.CHECK_VERSION);
        new AlertDialog.Builder(context).setTitle(R.string.new_version)
                //.setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setPositiveButton(R.string.update, (dialog, which) -> downloadAndInstall(appInfo)).show();
    }

    private String getApkName(String version) {
        return "Beauty_" + version + ".apk";
    }

    public void release() {
        if (helper != null) {
            helper.release();
        }
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
