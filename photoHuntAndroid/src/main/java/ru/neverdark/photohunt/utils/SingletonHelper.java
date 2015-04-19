package ru.neverdark.photohunt.utils;

import android.os.Build;

public class SingletonHelper {
    private static SingletonHelper mInstance;
    private int mVersion;
    private String mUserAgent;

    public static SingletonHelper getInstance() {
        if (mInstance == null) {
            mInstance = new SingletonHelper();
        }

        return mInstance;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
        mUserAgent = String.format("%s %s (%s); App: %d", Build.MANUFACTURER.toUpperCase(), Build.MODEL, Build.VERSION.RELEASE, mVersion);
    }

    public String getUserAgent() {
        return mUserAgent;
    }
}
