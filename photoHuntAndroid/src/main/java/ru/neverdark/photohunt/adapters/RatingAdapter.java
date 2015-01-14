package ru.neverdark.photohunt.adapters;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RatingAdapter extends ArrayAdapter<User> {
    private final List<User> mObjects;
    private final int mResource;
    private final Context mContext;
    
    private static class RowHolder {
        private TextView mAuthor;
        private TextView mBalance;
    }
    
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
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }
        
        User user = getItem(position);
        String author = String.format(Locale.US, "%d. %s", position + 1, user.display_name);
        String balance = String.format(Locale.US, "%d", user.balance);
        holder.mAuthor.setText(author);
        holder.mBalance.setText(balance);
        return row;
    }
    

}
