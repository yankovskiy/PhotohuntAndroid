package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Item;
import ru.neverdark.photohunt.utils.Log;

public class MyItemsAdapter extends ArrayAdapter<Item>{
    private final Context mContext;
    private final int mResource;
    private final List<Item> mObjects;

    public MyItemsAdapter(Context context, int resource, List<Item> items) {
        super(context, resource, items);
        mContext = context;
        mResource = resource;
        mObjects = items;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    private static class RowHolder {
        private TextView mMyItemsName;
        private TextView mMyItemsDescription;
        private TextView mMyItemsCount;
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
            holder.mMyItemsDescription = (TextView) row.findViewById(R.id.shop_myitems_description);
            holder.mMyItemsName = (TextView) row.findViewById(R.id.shop_myitems_name);
            holder.mMyItemsCount = (TextView) row.findViewById(R.id.shop_myitems_count);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }


        Item item = getItem(position);
        String count = String.format(Locale.US, "%d %s", item.count, mContext.getString(R.string.brief_count));

        holder.mMyItemsName.setText(item.name);
        holder.mMyItemsDescription.setText(item.description);
        holder.mMyItemsCount.setText(count);

        return row;
    }

    /**
     * Возвращает элемент по его service_name
     * @param serviceName значение поля service_name
     * @return возвращает объект типа RestService.Item, либо null если объект не найден
     */
    public Item getItemByServiceName(String serviceName) {
        Log.enter();
        Log.variable("size", String.valueOf(mObjects.size()));


        for (int i = 0; i < mObjects.size(); i++) {
            Log.variable("service_name", mObjects.get(i).service_name);
            if (mObjects.get(i).service_name.equals(serviceName)) {
                return mObjects.get(i);
            }
        }

        return null;
    }
}
