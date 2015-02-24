package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import ru.neverdark.photohunt.rest.RestService;

public class GoodsAdapter extends ArrayAdapter<RestService.Goods> {
    public GoodsAdapter(Context context, int resource, List<RestService.Goods> goods) {
        super(context, resource, goods);
    }
}
