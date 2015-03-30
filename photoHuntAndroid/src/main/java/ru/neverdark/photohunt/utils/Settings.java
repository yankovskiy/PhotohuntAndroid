package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import ru.neverdark.photohunt.R;

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

    public static String getRegistrationId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_filename), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(context.getString(R.string.pref_reg_id), "");
        if (registrationId.isEmpty()) {
            Log.message("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(context.getString(R.string.pref_app_version), Integer.MIN_VALUE);
        int currentVersion = SingletonHelper.getInstance().getVersion();
        if (registeredVersion != currentVersion) {
            Log.message("App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    public static void storeRegistrationId(Context context, String regId) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_filename), Context.MODE_PRIVATE);
        int appVersion = SingletonHelper.getInstance().getVersion();

        Log.message(String.format(Locale.US, "Saving regId (%s) on app version (%d)", regId, appVersion));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_reg_id), regId);
        editor.putInt(context.getString(R.string.pref_app_version), appVersion);
        editor.commit();
    }
}
