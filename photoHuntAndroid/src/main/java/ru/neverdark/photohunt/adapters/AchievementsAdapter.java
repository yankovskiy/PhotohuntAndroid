package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Achievement;
import ru.neverdark.photohunt.utils.Common;

/**
 * Адаптер для системы достижений
 */
public class AchievementsAdapter extends ArrayAdapter<Achievement> {
    private final Context mContext;
    private final int mResource;

    public AchievementsAdapter(Context context, List<Achievement> objects) {
        super(context, R.layout.achievements_list_item, objects);
        mContext = context;
        mResource = R.layout.achievements_list_item;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;

        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.mName = (TextView) row.findViewById(R.id.achievements_name);
            holder.mDescription = (TextView) row.findViewById(R.id.achievements_description);
            holder.mCount = (TextView) row.findViewById(R.id.achievements_count);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }


        Achievement achievement = getItem(position);
        holder.mName.setText(achievement.name);
        if (achievement.status) {
            holder.mName.setPaintFlags(holder.mName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.mName.setPaintFlags(holder.mName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.mDescription.setText(achievement.description);

        String count = null;
        if (achievement.count > 0) {
            count = String.format(Locale.US, mContext.getString(R.string.achievement_on_user), achievement.count,
                    Common.declensionByNumber(achievement.count, mContext.getResources().getStringArray(R.array.user_count)));
        } else {
            count = mContext.getString(R.string.be_the_first);
        }
        holder.mCount.setText(count);
        return row;
    }

    private static class RowHolder {
        private TextView mName;
        private TextView mDescription;
        private TextView mCount;
    }
}
