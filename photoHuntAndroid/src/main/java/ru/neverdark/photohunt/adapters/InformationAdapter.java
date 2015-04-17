package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.neverdark.photohunt.R;

public class InformationAdapter extends ArrayAdapter<InformationAdapter.Record> {
    private final int mResource;
    private final Context mContext;

    public InformationAdapter(Context context) {
        this(context, R.layout.information_list_item, new ArrayList<Record>());
    }

    private InformationAdapter(Context context, int resource, List<Record> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder;

        if (row == null) {
            holder = new RowHolder();

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);

            holder.title = (TextView) row.findViewById(R.id.information_list_item_title);
            holder.message = (TextView) row.findViewById(R.id.information_list_item_message);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        Record item = getItem(position);
        holder.title.setText(item.getTitle());
        if (item.isHaveMessage()) {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(item.getMessage());
        } else {
            holder.message.setVisibility(View.GONE);
        }
        return row;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    private static class RowHolder {
        private TextView title;
        private TextView message;
    }

    public static class Record {
        private final int mId;
        private String mTitle;
        private String mMessage;

        public Record(String title, String message, int id) {
            this.mTitle = title;
            this.mMessage = message;
            this.mId = id;
        }

        public boolean isHaveMessage() {
            return mMessage != null;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            this.mMessage = message;
        }

        public int getId() {
            return mId;
        }
    }
}
