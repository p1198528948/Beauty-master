package com.dante.girls;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.utils.BarUtils;
import com.bugtags.library.Bugtags;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.helper.RevealHelper;
import com.dante.girls.helper.Updater;
import com.dante.girls.lib.PopupDialogActivity;
import com.dante.girls.picture.FavoriteFragment;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.ui.SettingsActivity;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Random;

import butterknife.BindView;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
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
import static com.dante.girls.net.API.TYPE_GANK;
import static com.dante.girls.utils.AppUtil.donate;

/**
 * 比如我希望加一个通知消息功能
 * 网络接收到某种消息，进过过滤加工，通知用户知道
 *
 * 注意，该项目使用了 butterknife
 * 对于 net API 我们不必关注
 * picture 包下的我们需要重点分析
 *
 * 使用 SparseArray 以提高效率
 */
public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    public static final String MAIN_FRAGMENT_TAG = "main";
    public static final int DRAWER_CLOSE_DELAY = 230;
    private static final String TAG = "MainActivity";
    @BindView(R.id.fab)
    public FloatingActionButton fab;
    public ActionBarDrawerToggle toggle;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.reveal)
    FrameLayout revealView;

    private boolean backPressed;
    private MenuItem currentMenu;
    private SparseArray<Fragment> fragmentSparseArray;
    private Updater updater;
    private boolean isFirst = true;
    private int placeHolderHeight;

    @Override
    protected int initLayoutId() {
        //找到需要的 XML 布局文件
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        setupDrawer();
        initToolbar();

        //更新
        updater = Updater.getInstance(this);
        //检查更新
        updater.check();

        initNavigationView();
        initFragments(savedInstanceState);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = BarUtils.getActionBarHeight(this);
            toolbar.setLayoutParams(layoutParams);
            initFab();
            return;
        }
        toolbar.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        toolbar.getViewTreeObserver().removeOnPreDrawListener(this);
                        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        toolbar.measure(widthSpec, heightSpec);
                        placeHolderHeight = toolbar.getHeight();
                        collapseToolbar();
                        return true;
                    }
                });
    }

    private void collapseToolbar() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(toolbar, "height", placeHolderHeight, BarUtils.getActionBarHeight(this));
        objectAnimator.setDuration(3000);
        objectAnimator.start();
//        toolbar.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(placeHolderHeight, BarUtils.getActionBarHeight(this));
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            toolbar.setLayoutParams(layoutParams);

        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                initFab();
            }
        });
        animator.start();
    }

    /**
     * 点击左下角桃心后
     */
    private void initFab() {
        if (SpUtil.getBoolean(SettingFragment.SECRET_MODE)) {
            return;
        }
        fab.animate().setStartDelay(0)
                .setDuration(400).scaleY(1).scaleX(1).start();



        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            fab.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setTitle(R.string.hint)
                        .setMessage(R.string.thanks_for_donation)
                        .setPositiveButton(R.string.donate, (dialogInterface, i) -> donate(MainActivity.this))
                        .create();
//                alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlideDialog;
                alertDialog.show();
            });
            return;
        }
        if (new Random().nextBoolean()) {
            //Morph transition
            fab.setOnClickListener(v -> {
                Intent login = PopupDialogActivity.getStartIntent(MainActivity.this, PopupDialogActivity.MORPH_TYPE_FAB);
                ActivityOptionsCompat options = null;
                options = ActivityOptionsCompat.makeSceneTransitionAnimation
                        (MainActivity.this, fab, getString(R.string.transition_morph_view));
                startActivity(login, options.toBundle());
            });
        } else {
            //Reveal animation 显示动画
            final RevealHelper helper = RevealHelper.with(this)
                    .reveal(revealView)
                    .hide(container)
                    .button(fab)
                    .onRevealEnd(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toggleToolbarFlag(false);
                            toggle.setDrawerIndicatorEnabled(false);
                            donate(MainActivity.this);
                        }
                    }).onUnrevealEnd(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            changeNavigator(true);
                        }
                    }).build();
            toggle.setToolbarNavigationClickListener(v -> helper.unreveal());
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (currentMenu != null) {
//            outState.putInt("itemId", currentMenu.getItemId());
//        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int itemId = savedInstanceState.getInt("itemId", R.id.nav_beauty);
//        MenuItem currentMenu = navView.getMenu().findItem(itemId);
//        navView.setCheckedItem(itemId);
//        if (currentMenu != null) {
//            onNavigationItemSelected(currentMenu);
//        }
//    }

    /**
     * 初始化Fragments，这个也是主界面独有的，重点是 [妹子] [二次元] [我的收藏]
     * @param savedInstanceState
     */
    private void initFragments(Bundle savedInstanceState) {
        if (fragmentSparseArray == null) {
            String[] titles, types;
            fragmentSparseArray = new SparseArray<>();

            /**  该数组 热门 等选项文字**/
            String[] all = getResources().getStringArray(R.array.db_titles);
            boolean secretMode = SpUtil.getBoolean(SettingFragment.SECRET_MODE);
            secretMode = true;
            if (secretMode) {
                //Gank & Douban
                titles = all;
                types = new String[]{TYPE_GANK, TYPE_DB_RANK, TYPE_DB_BREAST, TYPE_DB_BUTT, TYPE_DB_LEG, TYPE_DB_SILK};
            } else {
                titles = new String[]{all[0]};
                types = new String[]{TYPE_GANK};
            }
            fragmentSparseArray.put(R.id.nav_beauty, MainTabsFragment.newInstance(titles, types));

            //二次元，同上
            titles = getResources().getStringArray(R.array.a_titles);
            types = new String[]{TYPE_A_ANIME, TYPE_A_FULI, TYPE_A_HENTAI, TYPE_A_UNIFORM, TYPE_A_ZATU};
            fragmentSparseArray.put(R.id.nav_a, MainTabsFragment.newInstance(titles, types));

            //favorite 我的收藏
            fragmentSparseArray.put(R.id.nav_favorite, new FavoriteFragment());
        }
        setMainFragment(R.id.nav_beauty, fragmentSparseArray, savedInstanceState == null);
    }

    private void setupDrawer() {
        /** 侧滑菜单 **/
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG,"onBackPressed() : 用户点击了退出键");
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        doublePressBackToQuit();
    }

    /**
     * 双击退出应用
     * 这个是主界面独有的
     */
    private void doublePressBackToQuit() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UiUtils.showSnack(fab, R.string.leave_app);
        new Handler().postDelayed(() -> backPressed = false, 2000);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initNavigationView() {
//        Colorful.config(this)
//                .translucent(true)
//                .apply();

        //load headerView's image
        Imager.load(this, R.drawable.head, (ImageView) navView.getHeaderView(0).findViewById(R.id.headImage));
        navView.setNavigationItemSelectedListener(this);
        boolean isSecretOn = SpUtil.getBoolean(SettingFragment.SECRET_MODE);
        navView.inflateMenu(R.menu.menu_main);
//        navView.setCheckedItem(R.id.nav_beauty);
        Menu menu = navView.getMenu();
        menu.getItem(0).setChecked(true);
        menu.getItem(0).setIcon(new IconicsDrawable(this).
                icon(GoogleMaterial.Icon.gmd_face)
                .color(ContextCompat.getColor(this, R.color.pink)));

        menu.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_collections));

        menu.getItem(2).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_favorite)
                .color(Color.RED));

        Menu sub = menu.getItem(3).getSubMenu();
        sub.getItem(0).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Color.DKGRAY));
        sub.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(Color.GRAY));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        /** 如果要些特殊功能，可以加个case项。
         * 重点关注default，如何得到不同的Fragment **/
        switch (id) {
            case R.id.nav_setting:
                Log.e(TAG,"侧滑菜单 ：设置");
                new Handler().postDelayed(() -> {
                    changeDrawer(false);
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }, DRAWER_CLOSE_DELAY);
                break;
            case R.id.nav_share:
                Log.e(TAG,"侧滑菜单 ：分享");
                String text = SpUtil.get(Updater.SHARE_APP, getString(R.string.share_app_description));
                Share.shareText(this, text);
                break;
            default:
                Log.e(TAG,"侧滑菜单 ：其他");
                currentMenu = item;
                setToolbarTitle(getCurrentMenuTitle());//对于工具栏要 重新加载
                switchMenu(id, fragmentSparseArray);//Fragment 重新加载
                break;
        }
        drawerLayout.closeDrawers();//选择完毕后关闭侧滑菜单
        return true;
    }


    public void changeNavigator(boolean enable) {
        if (toggle == null) return;
        if (enable) {
            toggle.setDrawerIndicatorEnabled(true);
        } else {
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(v -> onBackPressed());
        }
    }

    public void toggleToolbarFlag(boolean scroll) {
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (scroll) {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
        } else {
            p.setScrollFlags(0);
        }
        toolbar.setLayoutParams(p);
    }

    public String getCurrentMenuTitle() {
        if (currentMenu == null) {
            currentMenu = navView.getMenu().getItem(0);
        }
        return currentMenu.getTitle().toString();
    }

    @Override
    protected void onDestroy() {
        updater.release();
        super.onDestroy();
    }

    Intent get(Class clz) {
        return new Intent(getApplicationContext(), clz);
    }

    public void changeDrawer(boolean enable) {
        drawerLayout.setDrawerLockMode(enable ?
                DrawerLayout.LOCK_MODE_UNLOCKED : LOCK_MODE_LOCKED_CLOSED);

    }
}
