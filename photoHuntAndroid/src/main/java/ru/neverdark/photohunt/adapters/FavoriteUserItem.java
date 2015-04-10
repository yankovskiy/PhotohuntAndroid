package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

import ru.neverdark.abs.Item;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.FavoriteUser;
import ru.neverdark.photohunt.utils.Common;

/**
 * Элемент списка "избранные пользователи"
 */
public class FavoriteUserItem implements Item {
    private final FavoriteUser mUser;
    private final Context mContext;

    @Override
    public Object getObject() {
        return mUser;
    }

    public FavoriteUserItem (Context context, FavoriteUser user) {
        mUser = user;
        mContext = context;
    }

    @Override
    public long getItemId() {
        return mUser.fid;
    }

    @Override
    public int getViewType() {
        return HeadersArrayAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View row = convertView;
        RowHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.favorites_users_list_item, null);
            holder  = new RowHolder();
            holder.mUser = (TextView) row.findViewById(R.id.favorites_users_item_user);
            holder.mAvatar = (ImageView) row.findViewById(R.id.favorites_users_item_avatar);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        if (mUser.fid == 1L) {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
        } else if (mUser.avatar != null && mUser.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg", RestService.getRestUrl(), mUser.avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }

        holder.mUser.setText(mUser.display_name);
        return row;
    }

    private static class RowHolder {
        private TextView mUser;
        private ImageView mAvatar;
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
