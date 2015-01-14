package ru.neverdark.photohunt.utils;

import ru.neverdark.photohunt.R;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    public static boolean isLogin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.pref_filename), Context.MODE_PRIVATE);
        return prefs.getBoolean(context.getString(R.string.pref_isLogin), false);
    }

    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.pref_filename), Context.MODE_PRIVATE);
        return prefs.getString(context.getString(R.string.pref_userId), "");
    }

    public static String getPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.pref_filename), Context.MODE_PRIVATE);
        return prefs.getString(context.getString(R.string.pref_password), "");
    }

}
