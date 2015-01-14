package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class UfoMenuItem {
    private final Drawable mMenuIcon;
    private final String mMenuLabel;
    private final int mId;

    public UfoMenuItem(Context context, int iconResource, int stringResource) {
        mMenuIcon = context.getResources().getDrawable(iconResource);
        mMenuLabel = context.getString(stringResource);
        mId = stringResource;
    }

    public String getMenuLabel() {
        return mMenuLabel;
    }

    public Drawable getMenuIcon() {
        return mMenuIcon;
    }

    public int getId() {
        return mId;
    }
}
