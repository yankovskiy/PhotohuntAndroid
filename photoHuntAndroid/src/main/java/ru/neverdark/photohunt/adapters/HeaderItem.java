package ru.neverdark.photohunt.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ru.neverdark.abs.Item;
import ru.neverdark.photohunt.R;

/**
 * Элемент списка - заголовок
 */
public class HeaderItem implements Item {
    private final String mHeader;

    private static class RowHolder {
        private TextView mItemHeader;
    }

    public HeaderItem(String header) {
        mHeader = header;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public int getViewType() {
        return HeadersArrayAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View row = convertView;
        RowHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.list_item_header, null);
            holder  = new RowHolder();
            holder.mItemHeader = (TextView) row.findViewById(R.id.list_item_header);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        holder.mItemHeader.setText(mHeader);
        return row;
    }
}
