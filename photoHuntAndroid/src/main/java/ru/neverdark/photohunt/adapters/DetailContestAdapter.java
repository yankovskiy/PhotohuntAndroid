package ru.neverdark.photohunt.adapters;

import java.util.List;
import java.util.Locale;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.Contest;
import ru.neverdark.photohunt.rest.RestService.ContestDetail;
import ru.neverdark.photohunt.rest.RestService.Image;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailContestAdapter extends ArrayAdapter<Image> {

    private class VoteHandler implements Callback<Void> {

        @Override
        public void failure(RetrofitError error) {
            Common.showMessage(mContext, R.string.already_voted);
        }

        @Override
        public void success(Void data, Response response) {
            Common.showMessage(mContext, R.string.vote_success);
            if (mCallback != null) {
                mCallback.onVote();
            }
        }

    }
    
    public interface VoteListener{
        public void onVote();
    }
    
    private VoteListener mCallback;
    
    public void setCallback(VoteListener callback) {
        mCallback = callback;
    }

    private class Transform implements Transformation {

        private RowHolder mHolder;

        public Transform(RowHolder holder) {
            mHolder = holder;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = mHolder.mImage.getWidth();

            return Common.resizeBitmap(source, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    private class VoteClickListener implements OnClickListener {
        private Image mImage;

        @Override
        public void onClick(View view) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            
            RestService service = new RestService(user, pass);
            service.getContestApi().voteForContest(mImage.contest_id, mImage, new VoteHandler());
        }

        public VoteClickListener(Image image) {
            mImage = image;
        }

    }

    private final List<Image> mObjects;
    private final Context mContext;
    private Contest mContest;
    private int mResource;

    private static class RowHolder {
        private ImageView mImage;
        private TextView mVoteCount;
        private TextView mAuthor;
        private TextView mSubject;
        private ImageView mVote;
    }

    public DetailContestAdapter(Context context, ContestDetail contestDetail) {
        this(context, R.layout.detail_contest_list_item, contestDetail.images);
        mContest = contestDetail.contest;
    }

    private DetailContestAdapter(Context context, int resource, List<Image> objects) {
        super(context, resource, objects);
        mObjects = objects;
        mContext = context;
        mResource = resource;
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
            holder.mImage = (ImageView) row.findViewById(R.id.detail_contest_list_item_image);
            holder.mVoteCount = (TextView) row
                    .findViewById(R.id.detail_contest_list_item_vote_count);
            holder.mAuthor = (TextView) row.findViewById(R.id.detail_contest_list_item_author);
            holder.mSubject = (TextView) row.findViewById(R.id.detail_contest_list_item_subject);
            holder.mVote = (ImageView) row.findViewById(R.id.detail_contest_list_item_vote);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        if (mContest.status != Contest.STATUS_VOTES) {
            holder.mVote.setVisibility(View.GONE);
        }
        
        Image image = getItem(position);

        String voteCount = null;
        String author = null;
        String subject = null;
        
        String hidden = mContext.getString(R.string.hidden);
        if (mContest.status == Contest.STATUS_CLOSE) {
            voteCount = String.format(Locale.US, "%d", image.vote_count);
            author = image.display_name;
            subject = image.subject;
        } else {
            voteCount = hidden;
            author = hidden;
            subject = hidden;
        }
        
        holder.mVoteCount.setText(voteCount);
        holder.mAuthor.setText(author);
        holder.mSubject.setText(subject);
        
        holder.mVote.setOnTouchListener(new ImageOnTouchListener());
        holder.mVote.setOnClickListener(new VoteClickListener(image));
        String url = String.format(Locale.US, "%s/images/%d.jpg", RestService.REST_URL, image.id);

        Picasso.with(mContext).load(url).transform(new Transform(holder)).tag(mContext)
                .into(holder.mImage);

        return row;
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).id;
    }
}
