package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Элемент меню
 */
public class UfoMenuItem {
    private final Drawable mMenuIcon;
    private final String mMenuLabel;
    private final int mId;

    /**
     * Конструктор
     * @param context context приложения
     * @param iconResource id ресурса иконки для пункта меню
     * @param stringResource id строкового ресура для пункта меню
     */
    public UfoMenuItem(Context context, int iconResource, int stringResource) {
        mMenuIcon = context.getResources().getDrawable(iconResource);
        mMenuLabel = context.getString(stringResource);
        mId = stringResource;
    }

    /**
     * Конструктор
     * @param context context приложения
     * @param iconResource id ресурса иконки для пункта меню
     * @param menuLabel строка для пункта меню
     * @param id id записи
     */
    public UfoMenuItem(Context context, int iconResource, String menuLabel, int id) {
        mMenuIcon = context.getResources().getDrawable(iconResource);
        mMenuLabel = menuLabel;
        mId = id;
    }

    /**
     * Получает строковое значение пункта меню
     * @return строковое значение пункта меню
     */
    public String getMenuLabel() {
        return mMenuLabel;
    }

    /**
     * Получает иконку пункта меню
     * @return drawable-ресурс пункта меню
     */
    public Drawable getMenuIcon() {
        return mMenuIcon;
    }

    /**
     * Получает id записи пункта меню
     * @return id записи пункта меню
     */
    public int getId() {
        return mId;
    }
}
