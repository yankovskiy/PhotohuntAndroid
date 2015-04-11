package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.User;
import ru.neverdark.photohunt.utils.Common;

public class RatingAdapter extends ArrayAdapter<User> {
    private final List<User> mObjects;
    private final int mResource;
    private final Context mContext;

    public RatingAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).id;
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
            holder.mAuthor = (TextView) row.findViewById(R.id.rating_list_item_author);
            holder.mBalance = (TextView) row.findViewById(R.id.rating_list_item_balance);
            holder.mPosition = (TextView) row.findViewById(R.id.rating_position);
            holder.mAvatar = (ImageView) row.findViewById(R.id.rating_avatar);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        User user = getItem(position);
        String userPosition = String.format(Locale.US, "%d", position + 1);
        String balance = String.format(Locale.US, "%s: %d", mContext.getString(R.string.rating_count), user.balance);
        holder.mAuthor.setText(user.display_name);
        holder.mBalance.setText(balance);
        holder.mPosition.setText(userPosition);

        if (user.avatar != null && user.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), user.avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }
        return row;
    }

    private static class RowHolder {
        private TextView mAuthor;
        private ImageView mAvatar;
        private TextView mBalance;
        private TextView mPosition;
    }

    private class Transform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = (int) mContext.getResources().getDimension(R.dimen.min_avatar_size);
            return Common.resizeBitmap(source, targetWidth, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }
}
