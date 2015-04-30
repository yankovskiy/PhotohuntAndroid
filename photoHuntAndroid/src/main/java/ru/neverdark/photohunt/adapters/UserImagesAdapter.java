package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.SquaredImageView;

public class UserImagesAdapter extends BaseAdapter {
    private final List<Image> mObjects;
    private final Context mContext;

    public UserImagesAdapter(Context context, List<Image> images) {
        mContext = context;
        mObjects = images;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(mContext);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        Image image = (Image) getItem(position);

        String url = String.format(Locale.US, "%s/images/%d.jpg", RestService.getRestUrl(), image.id);
        Picasso.with(mContext).load(url).fit().tag(mContext).into(view);

        return view;
    }
}
