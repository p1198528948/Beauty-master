package com.dante.girls.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;
import com.dante.girls.BuildConfig;

import io.realm.Realm;
import timber.log.Timber;

/**
 * 自定义 应用程序 libs 初始化
 *
 * Custom application for libs init etc.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        //Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventNone);
        //BTGInvocationEventNone 静默模式，只收集 Crash 信息（如果允许，默认为允许）
        Bugtags.start("91fdc0a9606816a652a26618aa9915e4", this, Bugtags.BTGInvocationEventNone);
        //Bugtags.start("91fdc0a9606816a652a26618aa9915e4", this, Bugtags.BTGInvocationEventBubble);

        Realm.init(this);
        Utils.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        Colorful.defaults()
//                .primaryColor(Colorful.ThemeColor.RED)
//                .accentColor(Colorful.ThemeColor.BLUE)
//                .translucent(false)
//                .dark(true);
//        Colorful.init(this);
    }
}
