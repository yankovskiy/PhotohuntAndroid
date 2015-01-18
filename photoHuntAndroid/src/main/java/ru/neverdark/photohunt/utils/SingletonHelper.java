package ru.neverdark.photohunt.utils;

public class SingletonHelper {
    private static SingletonHelper mInstance;
    private int mVersion;

    public static SingletonHelper getInstance() {
        if (mInstance == null) {
            mInstance = new SingletonHelper();
        }

        return mInstance;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public int getVersion() {
        return mVersion;
    }
}
