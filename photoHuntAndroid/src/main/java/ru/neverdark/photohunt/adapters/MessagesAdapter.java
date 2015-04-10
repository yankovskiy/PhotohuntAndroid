package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
import ru.neverdark.photohunt.rest.data.Message;
import ru.neverdark.photohunt.utils.Common;

/**
 * Адаптер для списка сообщений
 */
public class MessagesAdapter extends ArrayAdapter<Message>{
    private final Context mContext;
    private final int mResource;
    private final boolean mIsInbox;

    private static class RowHolder {
        private ImageView mAvatar;
        private TextView mFrom;
        private TextView mTitle;
        private TextView mDate;
    }

    public MessagesAdapter(Context context, int resource, List<Message> objects, boolean isInbox) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mIsInbox = isInbox;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder;

        String avatar;
        long userId;
        String displayName;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);

            holder = new RowHolder();
            holder.mAvatar = (ImageView) row.findViewById(R.id.message_avatar);
            holder.mFrom = (TextView) row.findViewById(R.id.message_from);
            holder.mDate = (TextView) row.findViewById(R.id.message_date);
            holder.mTitle = (TextView) row.findViewById(R.id.message_title);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        Message message = getItem(position);


        if (mIsInbox) {
            avatar = message.from_avatar;
            userId = message.from_user_id;
            displayName = message.from;
        } else {
            avatar = message.to_avatar;
            userId = message.to_user_id;
            displayName = message.to;
        }


        holder.mFrom.setText(displayName);
        holder.mTitle.setText(message.title);
        holder.mDate.setText(Common.parseDate(mContext, message.date));

        if (mIsInbox && message.status != Message.READ) {
            holder.mFrom.setTypeface(null, Typeface.BOLD);
        } else {
            holder.mFrom.setTypeface(null, Typeface.NORMAL);
        }

        if (userId == 1L) {// system
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
        } else if (avatar != null && avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg", RestService.getRestUrl(), avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }

        return row;
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

    public boolean isReadInbox() {
        for (int i = 0; i < getCount(); i++) {
            Message message = getItem(i);
            if (message.status == Message.READ) {
                return true;
            }
        }

        return false;
    }
}
