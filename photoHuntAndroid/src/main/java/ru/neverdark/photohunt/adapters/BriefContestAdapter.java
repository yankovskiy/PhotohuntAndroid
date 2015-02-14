package ru.neverdark.photohunt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.ButtonOnTouchListener;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;

public class BriefContestAdapter extends ArrayAdapter<RestService.Contest>{

    private final Context mContext;
    private final List<RestService.Contest> mObjects;
    private final int mResource;
    private OnBriefCardClickListener mCallback;

    private static class RowHolder {
        private TextView mEnterButton;
        private TextView mSubject;
        private TextView mAuthor;
        private TextView mRewards;
        private TextView mOpenDate;
        private TextView mCloseDate;
        private TextView mWorks;
        private ImageView mContextButton;
    }

    public interface OnBriefCardClickListener {
        public void enterToContest(long contestId);
        public void onMoreButton(RestService.Contest contest);
    }

    public void setCallback(OnBriefCardClickListener callback) {
        mCallback = callback;
    }

    public BriefContestAdapter(Context context, List<RestService.Contest> contests) {
        this(context, R.layout.brief_contest_list_item, contests);
    }

    private BriefContestAdapter(Context context, int resource, List<RestService.Contest> contests) {
        super(context, resource, contests);
        mContext = context;
        mObjects = contests;
        mResource = resource;
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
            holder.mEnterButton = (TextView) row.findViewById(R.id.brief_contest_enter);
            holder.mSubject = (TextView) row.findViewById(R.id.brief_contest_subject);
            holder.mAuthor = (TextView) row.findViewById(R.id.brief_contest_author);
            holder.mRewards = (TextView) row.findViewById(R.id.brief_contest_reward);
            holder.mOpenDate = (TextView) row.findViewById(R.id.brief_contest_open);
            holder.mCloseDate = (TextView) row.findViewById(R.id.brief_contest_close);
            holder.mWorks = (TextView) row.findViewById(R.id.brief_contest_works);
            holder.mContextButton = (ImageView) row.findViewById(R.id.brief_context_button);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        RestService.Contest contest = getItem(position);
        String subject = contest.subject;
        String author = String.format("%s: %s", mContext.getString(R.string.author), contest.display_name);
        String openDate = String.format("%s: %s", mContext.getString(R.string.open_date), contest.open_date);
        String closeDate = String.format("%s: %s", mContext.getString(R.string.close_date), contest.close_date);
        String rewards = String.format(Locale.US, "%s: %d", mContext.getString(R.string.reward), contest.rewards);
        String works = String.format("%s: %s", mContext.getString(R.string.works_count), contest.works);

        holder.mAuthor.setText(author);
        holder.mSubject.setText(subject);
        holder.mOpenDate.setText(openDate);
        holder.mCloseDate.setText(closeDate);
        holder.mRewards.setText(rewards);
        holder.mWorks.setText(works);

        String enterText = null;
        switch (contest.status) {
            case RestService.Contest.STATUS_OPEN:
                enterText = mContext.getString(R.string.take_part);
                break;
            case RestService.Contest.STATUS_VOTES:
                enterText = mContext.getString(R.string.vote);
                break;
            case RestService.Contest.STATUS_CLOSE:
                enterText = mContext.getString(R.string.view);
        }

        holder.mEnterButton.setText(enterText.toUpperCase(Locale.getDefault()));
        holder.mEnterButton.setOnClickListener(new ButtonClickListener(contest.id));
        holder.mEnterButton.setOnTouchListener(new ButtonOnTouchListener());

        holder.mContextButton.setOnTouchListener(new ImageOnTouchListener(false));
        holder.mContextButton.setOnClickListener(new ButtonClickListener(contest));

        return row;
    }

    private class ButtonClickListener implements View.OnClickListener {

        private final long mId;
        private RestService.Contest mContest;

        public ButtonClickListener(long id) {
            mId = id;
        }

        public ButtonClickListener(RestService.Contest contest) {mId = 0L; mContest = contest;}

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                switch (v.getId()) {
                    case R.id.brief_contest_enter:
                        mCallback.enterToContest(mId);
                        break;
                    case R.id.brief_context_button:
                        mCallback.onMoreButton(mContest);
                        break;
                }
            }
        }
    }
}
