package ru.neverdark.abs;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Интерфейс элемент списка
 */
public interface Item {
    public Object getObject();
    public long getItemId();
    public int getViewType();
    public View getView(LayoutInflater inflater, View convertView);
}
