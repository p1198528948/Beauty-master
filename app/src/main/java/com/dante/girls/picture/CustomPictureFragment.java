package com.dante.girls.picture;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DataBase;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.net.DataFetcher;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static com.dante.girls.net.API.TYPE_A_ANIME;
import static com.dante.girls.net.API.TYPE_A_FULI;
import static com.dante.girls.net.API.TYPE_A_HENTAI;
import static com.dante.girls.net.API.TYPE_A_UNIFORM;
import static com.dante.girls.net.API.TYPE_A_ZATU;
import static com.dante.girls.net.API.TYPE_DB_BREAST;
import static com.dante.girls.net.API.TYPE_DB_BUTT;
import static com.dante.girls.net.API.TYPE_DB_LEG;
import static com.dante.girls.net.API.TYPE_DB_RANK;
import static com.dante.girls.net.API.TYPE_DB_SILK;

/**
 * Custom appearance of picture list fragment
 */

public class CustomPictureFragment extends PictureFragment implements
        OrderedRealmCollectionChangeListener<RealmResults<Image>> {

    private static final String TAG = "CustomPictureFragment";
    private static final int BUFFER_SIZE = 2;
    boolean inAPost;
    boolean isA;
    private boolean firstPage;
    private int size;
    private int old;
    private int error;
    private boolean isGank;

    /**
     * 见书本 P173
     * @param type
     * @return
     */
    public static CustomPictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        CustomPictureFragment fragment = new CustomPictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void addInfo(String info) {
        this.info = info;
        inAPost = true;
    }

    @Override
    protected void initViews() {
        super.initViews();
        imageType = TextUtils.isEmpty(info) ? baseType : info;
    }

    @Override
    protected void onImageClicked(View view, int position) {
        if (isA && !inAPost) {
            startPost(getImage(position));
            return;
        }
        super.onImageClicked(view, position);
    }

    @Override
    protected int initAdapterLayout() {
        int layout = R.layout.picture_item;
        switch (baseType) {
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                isA = true;
                layout = R.layout.post_item;
                if (inAPost) {
                    layout = R.layout.picture_item;
                }
        }
        return layout;
    }

    @Override
    public void fetch() {
        if (isFetching) {
            Log.e(TAG, "fetch: isFetching. return");
            return;
        }
        firstPage = page <= 1;

        DataFetcher fetcher;
        Observable<List<Image>> source;
        switch (baseType) {
            case TYPE_DB_BREAST:
            case TYPE_DB_BUTT:
            case TYPE_DB_LEG:
            case TYPE_DB_SILK:
            case TYPE_DB_RANK:
                url = API.DB_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getDouban();
                break;

            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                url = API.A_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = inAPost ? fetcher.getPicturesOfPost(info) : fetcher.getAPosts();
                break;

            default://imageType = 0, 代表GANK
                url = API.GANK;
                isGank = true;
                if (firstPage) {
                    LOAD_COUNT = LOAD_COUNT_LARGE;
                }
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getGank();
                break;
        }
        fetchImages(source);
    }

    //预加载Image，然后刷新列表
    protected void fetchImages(final Observable<List<Image>> source) {
        subscription = source
                .observeOn(Schedulers.io())
                .filter(list -> list.size() > 0)
                .flatMap(new Func1<List<Image>, Observable<Image>>() {
                    @Override
                    public Observable<Image> call(List<Image> images) {
                        return Observable.from(images);
                    }
                })
                .map(image -> {
                    if (!isA || inAPost) {
                        //不是A区，需要预加载
                        try {
                            image = Image.getFixedImage(this, image, imageType);
                            if (!isGank && page <= 1) {
                                if (Realm.getDefaultInstance().
                                        where(Image.class).equalTo(Constants.URL, url).findFirst() == null) {
                                    image.setPublishedAt(new Date());
                                }
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return image;
                })
                .compose(applySchedulers())
                .subscribe(new Subscriber<Image>() {
                    int oldSize;

                    @Override
                    public void onStart() {
                        oldSize = images.size();
                    }

                    @Override
                    public void onCompleted() {
                        changeState(false);
                        if (adapter.isLoading()) {
                            adapter.loadMoreComplete();
                        }
                        if (!firstPage) {
                            SpUtil.save(imageType + Constants.PAGE, page);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        adapter.loadMoreFail();
                        error++;
                        if (error > 3) {
                            UiUtils.showSnackLong(rootView, R.string.net_error);
                        } else {
                            UiUtils.showSnack(rootView, R.string.load_fail);
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Image list) {
                        DataBase.save(realm, list);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        if (isA) recyclerView.setBackgroundColor(getColor(R.color.cardview_dark_background));
    }

    @Override
    public void onDestroyView() {
        if (adapter.isLoading()) {
            adapter.loadMoreComplete();
        }
        super.onDestroyView();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);//fragment被show或者hide时调用
        if (!hidden && context != null) {
            setupToolbar();
            ((MainActivity) context).changeDrawer(!inAPost);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) context).changeDrawer(!inAPost);
    }

    private void setupToolbar() {
        //在A区帖子中，改变toolbar的样式
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (inAPost) {
            p.setScrollFlags(0);
            ((MainActivity) context).changeNavigator(false);
            context.setToolbarTitle(title);
        } else {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            ((MainActivity) context).changeNavigator(true);
            context.setToolbarTitle(((MainActivity) context).getCurrentMenuTitle());
        }
        toolbar.setLayoutParams(p);
    }

    private void startPost(Image image) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        CustomPictureFragment fragment = CustomPictureFragment.newInstance(imageType);
        fragment.addInfo(image.info);//帖子地址，也是imageType
        fragment.setTitle(image.title);//用于toolbar的标题
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.hide(this);
        transaction.add(R.id.container, fragment, "aPost");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void initData() {
        super.initData();
        setupToolbar();
//        recyclerView.animate().alpha(1)
//                .setStartDelay(200).start();
        images.addChangeListener(this);

        adapter.setOnLoadMoreListener(() -> {
            page = SpUtil.getInt(imageType + Constants.PAGE, 1);
            page++;
            log("load more ", page);
            fetch();
        }, recyclerView);
        adapter.disableLoadMoreIfNotFullPage();

        if (images.isEmpty()) {
            firstFetch = true;
            fetch();
            changeState(true);
        }
    }

    @Override
    public void onChange(RealmResults<Image> collection, OrderedCollectionChangeSet changeSet) {
        // `null`  means the async query returns the first time.
        if (changeSet == null) {
            log("no change");
            adapter.notifyDataSetChanged();
            return;
        }
        // For deletions, the adapter has to be notified in reverse order.
        OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
        for (int i = deletions.length - 1; i >= 0; i--) {
            OrderedCollectionChangeSet.Range range = deletions[i];
            log("notifyItemRangeRemoved from: " + range.startIndex + " length: " + range.length);
            adapter.notifyItemRangeRemoved(range.startIndex, range.length);
        }

        OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
        for (OrderedCollectionChangeSet.Range range : insertions) {
            log("notifyItemRangeInserted from: " + range.startIndex + " length: " + range.length);
            adapter.notifyItemRangeInserted(range.startIndex, range.length);
            if (page == 1) {
                recyclerView.scrollToPosition(0);
            }
        }

//        OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
//        for (OrderedCollectionChangeSet.Range range : modifications) {
//            log("no notifyItemRangeChanged " + range.startIndex + " to " + range.length);
//            adapter.notifyItemRangeChanged(range.startIndex, range.length);
//        }
    }
}
