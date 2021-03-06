package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.utils.UfoMenuItem;

public class MenuAdapter extends ArrayAdapter<UfoMenuItem> {
    private final Context mContext;
    private final int mResource;
    private final List<UfoMenuItem> mObjects;

    private static class RowHolder {
        private ImageView mMenuIcon;
        private TextView mMenuLabel;
        private TextView mCount;
    }

    public MenuAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<UfoMenuItem>());
    }

    private MenuAdapter(Context context, int resource, List<UfoMenuItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;

        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.mMenuIcon = (ImageView) row.findViewById(R.id.menu_icon);
            holder.mMenuLabel = (TextView) row.findViewById(R.id.menu_label);
            holder.mCount = (TextView) row.findViewById(R.id.menu_count);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        UfoMenuItem item = mObjects.get(position);
        holder.mMenuIcon.setImageDrawable(item.getMenuIcon());
        holder.mMenuLabel.setText(item.getMenuLabel());

        if (item.getMenuCount() != -1 && holder.mCount != null) {
            holder.mCount.setText(String.valueOf(item.getMenuCount()));
            holder.mCount.setVisibility(View.VISIBLE);
            holder.mMenuLabel.setTypeface(null, Typeface.BOLD);
        }

        return row;
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).getId();
    }

}
