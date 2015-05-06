package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;

public class ViewSingleImageFragment extends UfoFragment {
    private Image mImage;
    private View mView;
    private Context mContext;
    private ImageView mSingleImage;
    private int mContestStatus;

    private View mInfoButton;
    private View mCommentButton;
    private View mImageInfoBlock;
    private TextView mImageDescription;
    private TextView mCommentCount;
    private View mVoteButton;
    private TextView mVotesCount;
    private ImageView mVoteImg;
    private OnButtonsClickListener mCallback;
    private TextView mCameraModel;
    private TextView mFocalLength;
    private TextView mShutterSpeed;
    private TextView mIso;
    private TextView mShutterDatetime;
    private TextView mAperture;

    public static ViewSingleImageFragment getInstance(Image image, int contestStatus) {
        ViewSingleImageFragment fragment = new ViewSingleImageFragment();
        fragment.mImage = image;
        fragment.mContestStatus = contestStatus;
        return fragment;
    }

    private void pressButton(View view, boolean isPress) {
        if (isPress) {
            view.getBackground().setColorFilter(0x77000000, android.graphics.PorterDuff.Mode.SRC_ATOP);
        } else {
            view.getBackground().clearColorFilter();
        }
        view.invalidate();
    }

    @Override
    public void bindObjects() {
        mSingleImage = (ImageView) mView.findViewById(R.id.view_single_image);
        mInfoButton = mView.findViewById(R.id.view_single_image_info_button);
        mCommentButton = mView.findViewById(R.id.view_single_image_comment_button);
        mVoteButton = mView.findViewById(R.id.view_single_image_vote_button);
        mVoteImg = (ImageView) mView.findViewById(R.id.view_single_image_vote_img);
        mVotesCount = (TextView) mView.findViewById(R.id.view_single_image_vote_count);
        mImageInfoBlock = mView.findViewById(R.id.view_single_image_info_block);
        mImageDescription = (TextView) mView.findViewById(R.id.view_single_image_description);
        mCameraModel = (TextView) mView.findViewById(R.id.view_single_image_camera);
        mFocalLength = (TextView) mView.findViewById(R.id.view_single_image_focal);
        mShutterSpeed = (TextView) mView.findViewById(R.id.view_single_image_shutter);
        mIso = (TextView) mView.findViewById(R.id.view_single_image_iso);
        mShutterDatetime = (TextView) mView.findViewById(R.id.view_single_image_datetime);
        mAperture = (TextView) mView.findViewById(R.id.view_single_image_aperture);
    }

    @Override
    public void setListeners() {
        mInfoButton.setOnClickListener(new ButtonClickListener());
        mInfoButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mCommentButton.setOnClickListener(new ButtonClickListener());
        mCommentButton.setOnTouchListener(new ButtonBGOnTouchListener());

        if (mContestStatus == Contest.STATUS_VOTES) {
            mVoteButton.setOnClickListener(new ButtonClickListener());
            mVoteButton.setOnTouchListener(new ButtonBGOnTouchListener());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.view_single_image_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        loadData();
        return mView;
    }

    private void loadData() {
        String url = String.format(Locale.US, "%s/images/%d.jpg?size=full", RestService.getRestUrl(), mImage.id);
        Log.variable("url", url);
        Picasso.with(mContext).load(url).transform(new Transform(mSingleImage)).tag(mContext).into(mSingleImage);

        if (mImage.description != null) {
            mImageDescription.setVisibility(View.VISIBLE);
            mImageDescription.setText(mImage.description);
        }

        if (mImage.exif != null) {
            mView.findViewById(R.id.view_single_image_info_exif).setVisibility(View.VISIBLE);
            if (mImage.exif.model != null) {
                mView.findViewById(R.id.view_single_image_camera_row).setVisibility(View.VISIBLE);
                mCameraModel.setText(mImage.exif.model);
            }

            if (mImage.exif.focal_length != null) {
                mView.findViewById(R.id.view_single_image_focal_row).setVisibility(View.VISIBLE);
                mFocalLength.setText(mImage.exif.focal_length);
            }

            if (mImage.exif.exposure_time != null) {
                mView.findViewById(R.id.view_single_image_shutter_row).setVisibility(View.VISIBLE);
                mShutterSpeed.setText(mImage.exif.exposure_time);
            }

            if (mImage.exif.iso != null) {
                mView.findViewById(R.id.view_single_image_iso_row).setVisibility(View.VISIBLE);
                mIso.setText(mImage.exif.iso);
            }

            if (mImage.exif.datetime != null) {
                mView.findViewById(R.id.view_single_image_datetime_row).setVisibility(View.VISIBLE);
                mShutterDatetime.setText(mImage.exif.datetime);
            }

            if (mImage.exif.aperture != null) {
                mView.findViewById(R.id.view_single_image_aperture_row).setVisibility(View.VISIBLE);
                mAperture.setText(mImage.exif.aperture);
            }
        }
        updateActionBar();
    }

    private void updateActionBar() {
        boolean isCanVote = (mContestStatus == Contest.STATUS_VOTES && mImage.user_id == 0L);
        if (mContestStatus != Contest.STATUS_OPEN) {
            if (isCanVote || mContestStatus == Contest.STATUS_CLOSE) {
                mVoteButton.setVisibility(View.VISIBLE);
            }

            if (mContestStatus == Contest.STATUS_CLOSE) {
                mVoteImg.setImageResource(R.drawable.ic_favorite_white_24dp);
                mVotesCount.setVisibility(View.VISIBLE);
                mVotesCount.setText(String.valueOf(mImage.vote_count));
            }

            if (isCanVote) {
                if (mImage.is_voted) {
                    mVoteImg.setImageResource(R.drawable.ic_favorite_white_24dp);
                } else {
                    mVoteImg.setImageResource(R.drawable.ic_favorite_outline_white_24dp);
                }
            }
        }

        if (mVoteButton.getVisibility() == View.VISIBLE) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCommentButton.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, R.id.view_single_image_info_button);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCommentButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    public void setCallback(OnButtonsClickListener callback) {
        mCallback = callback;
    }

    private void showInfo() {
        boolean isPressed = mImageInfoBlock.getVisibility() == View.VISIBLE;
        pressButton(mInfoButton, !isPressed);
        if (!isPressed) {
            mSingleImage.setVisibility(View.GONE);
            mImageInfoBlock.setVisibility(View.VISIBLE);
        } else {
            mImageInfoBlock.setVisibility(View.GONE);
            mSingleImage.setVisibility(View.VISIBLE);
        }

        if (mCallback != null) {
            mCallback.setPagingEnabled(isPressed);
        }
    }

    public interface OnButtonsClickListener {
        public void incVote();
        public void decVote();

        public void setPagingEnabled(boolean enabled);
    }

    private class Transform implements Transformation {
        private ImageView mImage;

        public Transform(ImageView image) {
            this.mImage = image;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = this.mImage.getWidth();

            return Common.resizeBitmap(source, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.view_single_image_vote_button:
                    vote();
                    break;
                case R.id.view_single_image_info_button:
                    showInfo();
                    break;
            }
        }

        private void vote() {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            RestService service = new RestService(user, pass);
            service.getContestApi().voteForContest(mImage.contest_id, mImage, new VoteForContestListener());
        }

        private class VoteForContestListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                mImage.is_voted = !mImage.is_voted;
                if (mImage.is_voted) {
                    mVoteImg.setImageResource(R.drawable.ic_favorite_white_24dp);
                    if (mCallback != null) {
                        mCallback.decVote();
                    }
                } else {
                    mVoteImg.setImageResource(R.drawable.ic_favorite_outline_white_24dp);
                    if (mCallback != null) {
                        mCallback.incVote();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    Common.showMessage(mContext, err.error);
                } catch (Exception e) {

                }
            }
        }
    }


}
