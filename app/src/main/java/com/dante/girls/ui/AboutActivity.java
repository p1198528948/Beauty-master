package com.dante.girls.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.dante.girls.BuildConfig;
import com.dante.girls.R;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.utils.AppUtil;
import com.dante.girls.utils.UiUtils;

import butterknife.BindView;

/**
 * about the author and so on.
 */
public class AboutActivity extends BaseActivity {
    private static final String TAG = "AboutActivity";
    @BindView(R.id.versionName)
    TextView versionName;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        versionName.setText(String.format(getString(R.string.version) + " %s(%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        if(BuildConfig.DEBUG){
            //Debug版本
            versionName.setText(versionName.getText()+"-Debug");
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (AppUtil.isIntentSafe(intent)) {
            super.startActivity(intent);
        } else {
            UiUtils.showSnack(versionName, R.string.email_not_install);
        }

    }

}
