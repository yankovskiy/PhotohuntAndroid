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
import ru.neverdark.photohunt.rest.data.Goods;
import ru.neverdark.photohunt.utils.Common;

public class GoodsAdapter extends ArrayAdapter<Goods> {
    private final Context mContext;
    private final int mResource;

    public GoodsAdapter(Context context, int resource, List<Goods> goods) {
        super(context, resource, goods);
        mContext = context;
        mResource = resource;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
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
            holder.mGoodsDescription = (TextView) row.findViewById(R.id.shop_goods_description);
            holder.mGoodsName = (TextView) row.findViewById(R.id.shop_goods_name);
            holder.mGoodsPrice = (TextView) row.findViewById(R.id.shop_goods_price);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }


        Goods goods = getItem(position);
        String price = String.format(Locale.US, "%d %s", goods.price_money, Common.declensionByNumber(goods.price_money, mContext.getResources().getStringArray(R.array.money)));

        holder.mGoodsName.setText(goods.name);
        holder.mGoodsDescription.setText(goods.description);
        holder.mGoodsPrice.setText(price);

        return row;
    }

    private static class RowHolder {
        private TextView mGoodsName;
        private TextView mGoodsDescription;
        private TextView mGoodsPrice;
    }
}
