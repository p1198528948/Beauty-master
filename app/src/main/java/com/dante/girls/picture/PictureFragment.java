package com.dante.girls.picture;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DataBase;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;

import io.realm.RealmResults;

/**
 * 注意这是一个抽象类
 * 瀑布流图片显示界面
 *
 * Gank and DB beauty fragment.
 */
public abstract class PictureFragment extends RecyclerFragment {
    public static final int LOAD_COUNT_LARGE = 12;
    public static final int REQUEST_VIEW = 1;
    private static final String TAG = "PictureFragment";
    public static int LOAD_COUNT = 8;
    private static int PRELOAD_COUNT = 10;

    String url;
    boolean isFetching;
    String title;
    int page = 1;
    String info;//附加信息，这里暂时用于type为A区时的帖子地址
    BaseActivity context;
    String baseType;
    StaggeredGridLayoutManager layoutManager;
    PictureAdapter adapter;
    RealmResults<Image> images;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        firstPosition = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()])[0];
        SpUtil.save(imageType + Constants.POSITION, firstPosition);
        super.onPause();
    }


    @Override
    protected void initViews() {
        super.initViews();
//        setRetainInstance(true);
        initFab();
        //baseType is for base url
        baseType = getArguments() == null ? "" : getArguments().getString(Constants.TYPE);
        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PictureAdapter(initAdapterLayout(), this);
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        adapter.disableLoadMoreIfNotFullPage(recyclerView);
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onImageClicked(view, i);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 30) {
                    return;
                }
                int[] spans = new int[layoutManager.getSpanCount()];
                firstPosition = layoutManager.findFirstVisibleItemPositions(spans)[0];
                lastPosition = layoutManager.findLastVisibleItemPositions(spans)[1];
            }
        });
        imageType = baseType;
    }

    protected void onImageClicked(View view, int position) {
        startViewer(view, position);
    }

    protected int initAdapterLayout() {
        return R.layout.picture_item;
    }

    public void startViewer(View view, int position) {
        Intent intent = new Intent(context.getApplicationContext(), ViewerActivity.class);
        intent.putExtra(Constants.TYPE, imageType);
        intent.putExtra(Constants.POSITION, position);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context, view, adapter.getData().get(position).url);
        ActivityCompat.startActivityForResult(context, intent, REQUEST_VIEW, options.toBundle());
    }

    public abstract void fetch();//确定type，base url和解析数据

    //改变是否在加载数据的状态
    public void changeState(boolean fetching) {
        isFetching = fetching;
        changeRefresh(isFetching);
    }

    @Override
    public void onStop() {
        super.onStop();
        changeState(false);
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        log("onCreateView: ", imageType);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected void initData() {
        images = DataBase.findImages(realm, imageType);
        //优化启动动画
        new Handler().postDelayed(() -> adapter.setNewData(images), baseType.equals(API.TYPE_GANK) ? 300 : 0);
    }

    private void initFab() {
        if (SpUtil.getBoolean(SettingFragment.SECRET_MODE)) {
            return;
        }
        final FloatingActionButton button = ((MainActivity) getActivity()).fab;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (button.isShown()) {
                    if (dy > 20) {
                        button.hide();
                    }
                } else {
                    if (dy < -20) {
                        button.show();
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        page = 1;
        fetch();
        if (adapter.isLoading()) {
            changeRefresh(false);
            UiUtils.showSnack(rootView, R.string.is_loading);
        }
    }

    public Image getImage(int position) {
        return adapter.getData().get(position);
    }


    public class WrapContentLinearLayoutManager extends StaggeredGridLayoutManager {
        WrapContentLinearLayoutManager(int spanCount, int orientation) {
            super(spanCount, orientation);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("probe", "meet a IOOBE in RecyclerView");
            }
        }
    }


}
