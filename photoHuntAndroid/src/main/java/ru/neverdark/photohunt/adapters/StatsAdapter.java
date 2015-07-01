package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.utils.Common;

public class StatsAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> mListDataHeader;
    private Map<String, List<ChildRecord>> mListDataChild;

    public StatsAdapter(Context context, List<String> listDataHeader, Map<String, List<ChildRecord>> listDataChild) {
        mContext = context;
        mListDataChild = listDataChild;
        mListDataHeader = listDataHeader;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String header = mListDataHeader.get(groupPosition);
        ChildRecord child = mListDataChild.get(header).get(childPosition);
        return child;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        ChildRecord child = (ChildRecord) getChild(groupPosition, childPosition);
        return child.getId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View row = convertView;
        ChildRowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.stats_list_item, parent, false);
            holder = new ChildRowHolder();
            holder.mAuthor = (TextView) row.findViewById(R.id.stats_list_item_user);
            holder.mSubject = (TextView) row.findViewById(R.id.stats_list_item_subject);
            holder.mCloseDate = (TextView) row.findViewById(R.id.stats_list_item_close_date);
            holder.mRewards = (TextView) row.findViewById(R.id.stats_list_item_reward);
            holder.mWorks = (TextView) row.findViewById(R.id.stats_list_item_works);
            holder.mStatus = (ImageView) row.findViewById(R.id.stats_list_item_status);
            row.setTag(holder);
        } else {
            holder = (ChildRowHolder) row.getTag();
        }

        ChildRecord record = (ChildRecord) getChild(groupPosition, childPosition);
        holder.mAuthor.setText(record.getAuthor());
        holder.mSubject.setText(record.getSubject());
        holder.mCloseDate.setText(Common.parseDate(mContext, record.getCloseDate()));
        holder.mRewards.setText(String.valueOf(record.getReward()));
        holder.mWorks.setText(String.valueOf(record.getWorks()));

        if (record.getStatus() == Contest.STATUS_CLOSE) {
            holder.mStatus.setImageResource(R.drawable.ic_lock_outline_grey600_24dp);
        } else {
            holder.mStatus.setImageResource(R.drawable.ic_lock_open_grey600_24dp);
        }

        return row;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String group = mListDataHeader.get(groupPosition);
        return mListDataChild.get(group).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View row = convertView;
        GroupRowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.stats_list_group, parent, false);
            holder = new GroupRowHolder();
            holder.mGroupHeader = (TextView) row.findViewById(R.id.stats_list_group_header);
            row.setTag(holder);
        } else {
            holder = (GroupRowHolder) row.getTag();
        }
        String header = String.format(Locale.US, "%s (%d)", (String) getGroup(groupPosition), getChildrenCount(groupPosition));
        holder.mGroupHeader.setText(header);

        return row;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static class ChildRecord {
        private long mId;
        private String mSubject;
        private String mAuthor;
        private String mCloseDate;
        private int mReward;
        private int mWorks;
        private int mStatus;

        public int getStatus() {
            return mStatus;
        }

        public void setStatus(int status) {
            mStatus = status;
        }

        public int getWorks() {
            return mWorks;
        }

        public void setWorks(int works) {
            mWorks = works;
        }

        public long getId() {
            return mId;
        }

        public void setId(long id) {
            mId = id;
        }

        public String getSubject() {
            return mSubject;
        }

        public void setSubject(String subject) {
            this.mSubject = subject;
        }

        public String getAuthor() {
            return mAuthor;
        }

        public void setAuthor(String author) {
            this.mAuthor = author;
        }

        public String getCloseDate() {
            return mCloseDate;
        }

        public void setCloseDate(String date) {
            mCloseDate = date;
        }

        public int getReward() {
            return mReward;
        }

        public void setReward(int reward) {
            this.mReward = reward;
        }
    }

    private static class ChildRowHolder {
        private TextView mAuthor;
        private TextView mSubject;
        private TextView mCloseDate;
        private TextView mRewards;
        private TextView mWorks;
        private ImageView mStatus;
    }

    private static class GroupRowHolder {
        private TextView mGroupHeader;
    }

}
