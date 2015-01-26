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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailContestAdapter extends ArrayAdapter<Image> {

    private final List<Image> mObjects;
    private final Context mContext;
    private VoteListener mCallback;
    private Contest mContest;
    private int mResource;

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

    public void setCallback(VoteListener callback) {
        mCallback = callback;
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
            holder.mVoteButton = (ImageView) row.findViewById(R.id.detail_contest_list_item_vote);
            holder.mContestData = (RelativeLayout) row.findViewById(R.id.detail_contest_data);
            holder.mDataDelemiter = row.findViewById(R.id.detail_contest_data_delimiter);
            holder.mImageDelemiter = row.findViewById(R.id.detail_contest_image_delimiter);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        if (mContest.status != Contest.STATUS_VOTES) {
            holder.mVoteButton.setVisibility(View.GONE);
        }

        Image image = getItem(position);

        String voteCount = null;
        String author = null;
        String subject = image.subject;

        String hidden = mContext.getString(R.string.hidden);
        if (mContest.status == Contest.STATUS_CLOSE) {
            voteCount = String.format(Locale.US, "%d", image.vote_count);
            author = image.display_name;
            setDataBlockVisible(holder, true);
        } else {
            voteCount = hidden;
            author = hidden;
            setDataBlockVisible(holder, false);
        }

        if (subject == null) {
            subject = hidden;
        }

        if (mContest.status == Contest.STATUS_VOTES) {
            setVoteBlockVisible(holder, true);
        } else {
            setVoteBlockVisible(holder, false);
        }

        holder.mVoteCount.setText(voteCount);
        holder.mAuthor.setText(author);
        holder.mSubject.setText(subject);

        holder.mVoteButton.setOnTouchListener(new ImageOnTouchListener());
        holder.mVoteButton.setOnClickListener(new VoteClickListener(image));
        String url = String.format(Locale.US, "%s/images/%d.jpg", RestService.getRestUrl(), image.id);

        Picasso.with(mContext).load(url).transform(new Transform(holder)).tag(mContext)
                .into(holder.mImage);

        return row;
    }

    private void setDataBlockVisible(RowHolder holder, boolean isVisible) {
        if (isVisible) {
            holder.mContestData.setVisibility(View.VISIBLE);
            holder.mImageDelemiter.setVisibility(View.VISIBLE);
        } else {
            holder.mContestData.setVisibility(View.GONE);
            holder.mImageDelemiter.setVisibility(View.GONE);
        }
    }

    private void setVoteBlockVisible(RowHolder holder, boolean isVisible) {
        if (isVisible) {
            holder.mDataDelemiter.setVisibility(View.VISIBLE);
            holder.mVoteButton.setVisibility(View.VISIBLE);
        } else {
            holder.mDataDelemiter.setVisibility(View.GONE);
            holder.mVoteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).id;
    }

    public interface VoteListener {
        public void onVote();
    }

    private static class RowHolder {
        private ImageView mRemoveButton;
        private ImageView mEditButton;
        private ImageView mImage;
        private TextView mVoteCount;
        private TextView mAuthor;
        private TextView mSubject;
        private ImageView mVoteButton;
        private View mImageDelemiter;
        private View mDataDelemiter;
        private RelativeLayout mContestData;
    }

    private class VoteHandler implements Callback<Void> {

        @Override
        public void failure(RetrofitError error) {
            RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
            Common.showMessage(mContext, err.error);
        }

        @Override
        public void success(Void data, Response response) {
            Common.showMessage(mContext, R.string.vote_success);
            if (mCallback != null) {
                mCallback.onVote();
            }
        }

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

        public VoteClickListener(Image image) {
            mImage = image;
        }

        @Override
        public void onClick(View view) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            RestService service = new RestService(user, pass);
            service.getContestApi().voteForContest(mImage.contest_id, mImage, new VoteHandler());
        }

    }
}
