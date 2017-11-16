package com.dante.girls.model;


import android.support.annotation.Nullable;

import com.dante.girls.base.Constants;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * 使用 Realm来存储数据，值得一探究竟
 *
 * 这里要分析一下，数据
 *
 * Deals with cache, data
 */
public class DataBase {

    //初始化
    private static Realm initRealm(Realm realm) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public static <T extends RealmObject> void save(Realm realm, List<T> realmObjects) {
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> T getById(Realm realm, int id, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).equalTo(Constants.ID, id).findFirst();
    }

    private static <T extends RealmObject> RealmResults<T> findAll(Realm realm, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public static void save(Realm realm, RealmObject realmObject) {
        if (realmObject == null) {
            return;
        }
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
    }


    public static Image getById(Realm realm, int id) {
        return getById(realm, id, Image.class);
    }

    public static boolean hasImage(@Nullable Realm realm, String url) {
        return getByUrl(realm, url) != null;
    }


    public static Image getByUrl(Realm realm, String url) {
        realm = initRealm(realm);
        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst();
    }

    public static RealmResults<Image> findImages(Realm realm, String type) {
        realm = initRealm(realm);
        if (Constants.FAVORITE.equals(type)) {
            return findFavoriteImages(realm);
        }
        return realm.where(Image.class)
                .equalTo(Constants.TYPE, type)
                .findAll();
    }

    /**
     * 这里找到喜欢的图片
     * @param realm
     * @return
     */
    public static RealmResults<Image> findFavoriteImages(Realm realm) {
        realm = initRealm(realm);
        return realm.where(Image.class)
                .equalTo("isLiked", true)
                .findAll();
    }

    /**
     * 清除所有的图片
     */
    public static void clearAllImages() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Image.class);
        realm.commitTransaction();
    }
}
