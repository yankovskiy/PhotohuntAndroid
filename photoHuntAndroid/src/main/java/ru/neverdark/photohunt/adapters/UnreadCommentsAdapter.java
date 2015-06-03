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
import ru.neverdark.photohunt.rest.data.UnreadComment;
import ru.neverdark.photohunt.utils.Common;

public class UnreadCommentsAdapter extends ArrayAdapter<UnreadComment> {
    private final Context mContext;
    private final int mResource;

    public UnreadCommentsAdapter(Context context, List<UnreadComment> objects) {
        super(context, R.layout.unread_comments_list_item, objects);
        mContext = context;
        mResource = R.layout.unread_comments_list_item;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).image_id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);

            holder = new RowHolder();
            holder.mAuthor = (TextView) row.findViewById(R.id.unread_comments_list_author);
            holder.mComment = (TextView) row.findViewById(R.id.unread_comments_list_comment);
            holder.mImage = (ImageView) row.findViewById(R.id.unread_comments_list_item_image);
            holder.mAvatar = (ImageView) row.findViewById(R.id.unread_comments_list_item_avatar);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        UnreadComment comment = getItem(position);
        holder.mAuthor.setText(comment.display_name);
        holder.mComment.setText(comment.comment);

        if (comment.avatar != null && comment.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), comment.avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }

        String url = String.format(Locale.US, "%s/images/%d.jpg?size=48dp", RestService.getRestUrl(), comment.image_id);
        Picasso.with(mContext).load(url).placeholder(R.drawable.placeholder).transform(new Transform()).tag(mContext).into(holder.mImage);

        return row;
    }

    private static class RowHolder {
        TextView mAuthor;
        TextView mComment;
        ImageView mAvatar;
        ImageView mImage;
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
