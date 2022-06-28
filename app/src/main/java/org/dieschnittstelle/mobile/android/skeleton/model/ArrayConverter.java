package org.dieschnittstelle.mobile.android.skeleton.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.room.TypeConverter;

public class ArrayConverter {

    @TypeConverter
    public static ArrayList<Long> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Long> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
