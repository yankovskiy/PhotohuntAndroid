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
import ru.neverdark.photohunt.rest.data.Comment;
import ru.neverdark.photohunt.utils.Common;

public class CommentsAdapter extends ArrayAdapter<Comment> {
    private final Context mContext;
    private final int mResource;

    public CommentsAdapter(Context context, List<Comment> objects) {
        super(context, R.layout.comments_list_item, objects);
        mContext = context;
        mResource = R.layout.comments_list_item;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
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
            holder.mComment = (TextView) row.findViewById(R.id.comment_list_item_comment);
            holder.mDate = (TextView) row.findViewById(R.id.comments_list_item_datetime);
            holder.mAuthor = (TextView) row.findViewById(R.id.comment_list_item_author);
            holder.mAvatar = (ImageView) row.findViewById(R.id.comment_list_item_avatar);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        Comment comment = getItem(position);
        if (comment.display_name != null) {
            holder.mAuthor.setText(comment.display_name);
        } else {
            holder.mAuthor.setText(R.string.photo_author);
        }

        holder.mComment.setText(comment.comment);
        String date = Common.parseDate(mContext, comment.datetime);
        String time = comment.datetime.split(" ")[1];

        holder.mDate.setText(String.format("%s\n%s", date, time));

        long userId = comment.user_id;
        String avatar = comment.avatar;

        if (userId == 1L) {// system
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
        } else if (avatar != null && avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(holder.mAvatar);
        } else {
            holder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }

        return row;
    }

    private static class RowHolder {
        TextView mAuthor;
        TextView mComment;
        TextView mDate;
        ImageView mAvatar;
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
