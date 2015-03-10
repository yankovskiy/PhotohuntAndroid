package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;

/**
 * Адаптер для статистики по очкам
 */
public class UserStatsAdapter extends BaseAdapter {
    private final List<UserStatsItem> mItems;
    private final Context mContext;

    public UserStatsAdapter(Context context, RestService.Stats data) {
        mItems = new ArrayList<>();
        mContext = context;

        UserStatsItem totalItem = new UserStatsItem(R.string.total_balance, data.total);
        UserStatsItem winsItem = new UserStatsItem(R.string.wins_balance, data.wins_rewards);
        UserStatsItem worksItem = new UserStatsItem(R.string.works_balance, data.works);
        UserStatsItem otherItem = new UserStatsItem(R.string.other_balance, data.other);

        mItems.add(totalItem);
        mItems.add(winsItem);
        mItems.add(worksItem);
        mItems.add(otherItem);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).mResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.user_stats_list_item, parent, false);
            holder = new RowHolder();
            holder.mText = (TextView) row.findViewById(R.id.user_stats_list_item_text);
            holder.mCount = (TextView) row.findViewById(R.id.user_stats_list_item_count);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        UserStatsItem item = mItems.get(position);
        holder.mText.setText(item.mResourceId);
        holder.mCount.setText(String.valueOf(item.mBalanceCount));

        return row;
    }

    private static class UserStatsItem {
        private int mResourceId;
        private int mBalanceCount;

        public UserStatsItem(int resourceId, int balance) {
            mResourceId = resourceId;
            mBalanceCount = balance;
        }
    }

    private static class RowHolder {
        private TextView mText;
        private TextView mCount;
    }
}
