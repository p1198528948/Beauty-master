package com.dante.girls.net;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;

/**
 * Json parser util.
 */
public class Json {

    public static Gson gson = new GsonBuilder().
            setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class) || f.getName().equals("type");
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    public static <T> T parse(String response, Class<T> clz) {
        return gson.fromJson(response, clz);
    }

}
