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
import ru.neverdark.photohunt.rest.data.AchievementUser;
import ru.neverdark.photohunt.utils.Common;

public class AchievementsUsersAdapter extends ArrayAdapter<AchievementUser> {
    private final int mResource;
    private final Context mContext;

    public AchievementsUsersAdapter(Context context, List<AchievementUser> objects) {
        super(context, R.layout.achievements_users_list_item, objects);
        mResource = R.layout.achievements_users_list_item;
        mContext = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;

        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.mDisplayName = (TextView) row.findViewById(R.id.achievements_users_item_user);
            holder.mAvatar = (ImageView) row.findViewById(R.id.achievements_users_item_avatar);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        AchievementUser user = getItem(position);
        if (user.avatar != null && user.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), user.avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }

        holder.mDisplayName.setText(user.display_name);

        return row;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    private static class RowHolder {
        private TextView mDisplayName;
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
