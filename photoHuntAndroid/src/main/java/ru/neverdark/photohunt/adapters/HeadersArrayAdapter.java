package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ru.neverdark.abs.Item;

/**
 * Адаптер с заголовками
 */
public class HeadersArrayAdapter extends ArrayAdapter<Item> {
    private final LayoutInflater mInflater;

    public HeadersArrayAdapter(Context context, List<Item> objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return RowType.values().length;

    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(mInflater, convertView);
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItem(position).getViewType() == RowType.LIST_ITEM.ordinal());
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    public enum RowType {
        LIST_ITEM,
        HEADER_ITEM
    }
}
