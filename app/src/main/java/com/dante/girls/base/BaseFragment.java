package com.dante.girls.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dante.girls.BuildConfig;

import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * BaseFragment 是抽象的，根据程序的需要，派生出各自独立的功能
 *
 * BaseFragment helps onCreateView, and initViews(when root is null), init data on Activity Created.
 */
public abstract class BaseFragment extends Fragment {

    protected View rootView;
    protected Realm realm;
    protected Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(initLayoutId(), container, false);
            ButterKnife.bind(this, rootView);
            initViews();
        }
        onCreateView();//在下面，并不是调自己
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isHidden", isHidden());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract int initLayoutId();

    protected void onCreateView() {
        ButterKnife.bind(this, rootView);
        realm = ((BaseActivity) getActivity()).realm;
        toolbar = ((BaseActivity) getActivity()).toolbar;
    }

    protected abstract void initViews();

    protected abstract void initData();

    public <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinct();
    }


    public void log(String key, String content) {
        if (BuildConfig.DEBUG && getUserVisibleHint()) {
            Log.d(getClass().getSimpleName(), key + "  " + content);
        }
    }

    public void log(String key, int content) {
        log(key, String.valueOf(content));
    }

    public void log(String key) {
        log(key, "");
    }

}
