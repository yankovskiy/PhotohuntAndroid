package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import ru.neverdark.photohunt.rest.RestService;

public class MyItemsAdapter extends ArrayAdapter<RestService.Item>{
    public MyItemsAdapter(Context context, int resource, List<RestService.Item> items) {
        super(context, resource, items);
    }
}
