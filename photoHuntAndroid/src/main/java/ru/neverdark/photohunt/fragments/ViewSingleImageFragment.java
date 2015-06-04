package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.CommentsAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Comment;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class ViewSingleImageFragment extends UfoFragment {
    private Image mImage;
    private View mView;
    private Context mContext;
    private ImageView mSingleImage;

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
    private View mCommentsBlock;
    private CommentsAdapter mCommentsAdapter;
    private ListView mCommentsList;
    private EditText mCommentEditText;
    private View mSendCommentButton;
    private Comment mComment;

    public static ViewSingleImageFragment getInstance(Image image) {
        ViewSingleImageFragment fragment = new ViewSingleImageFragment();
        fragment.mImage = image;
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
        mCommentEditText = (EditText) mView.findViewById(R.id.view_single_image_comment);
        mCommentCount = (TextView) mView.findViewById(R.id.view_single_image_comments_tv);
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
        mCommentsBlock = mView.findViewById(R.id.view_single_image_comments_block);
        mCommentsList = (ListView) mView.findViewById(R.id.view_single_image_comments_list);
        mSendCommentButton = mView.findViewById(R.id.view_single_image_send_comment_button);
    }

    @Override
    public void setListeners() {
        mInfoButton.setOnClickListener(new ButtonClickListener());
        mInfoButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mCommentButton.setOnClickListener(new ButtonClickListener());
        mCommentButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mSendCommentButton.setOnClickListener(new ButtonClickListener());
        mSendCommentButton.setOnTouchListener(new ImageOnTouchListener(false));

        if (mImage.contest_status == Contest.STATUS_VOTES) {
            mVoteButton.setOnClickListener(new ButtonClickListener());
            mVoteButton.setOnTouchListener(new ButtonBGOnTouchListener());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        Log.enter();
        Log.variable("imageId", String.valueOf(mImage.id));
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mComment = mCommentsAdapter.getItem(acmi.position);
        if (mComment.is_can_deleted) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.comments, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.enter();
        Log.variable("imageId", String.valueOf(mImage.id));
        Log.variable("mCommentsAdapter", String.valueOf(mCommentsAdapter == null));

        if (mComment != null) {
            switch (item.getItemId()) {
                case R.id.remove_comment:
                    showRemoveCommentConfirmation(mComment);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void showRemoveCommentConfirmation(Comment comment) {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessage(R.string.remove_message);
        dialog.setCallback(new RemoveCommentConfirmationListener(comment));
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
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
        Picasso.with(mContext).load(url).placeholder(R.drawable.placeholder).transform(new Transform(mSingleImage)).tag(mContext).into(mSingleImage);

        if (mImage.description != null) {
            mImageDescription.setVisibility(View.VISIBLE);
            mImageDescription.setText(mImage.description);
        }

        if (mImage.comments_count > 0) {
            mCommentCount.setText(String.valueOf(mImage.comments_count));
            mCommentCount.setVisibility(View.VISIBLE);
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
        boolean isCanVote = (mImage.contest_status == Contest.STATUS_VOTES && mImage.user_id == 0L);
        if (mImage.contest_status != Contest.STATUS_OPEN) {
            if (isCanVote || mImage.contest_status == Contest.STATUS_CLOSE) {
                mVoteButton.setVisibility(View.VISIBLE);
            }

            if (mImage.contest_status == Contest.STATUS_CLOSE) {
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
        ((MainActivity) getActivity()).hideKeyboard();
        pressButton(mInfoButton, !isPressed);
        pressButton(mCommentButton, false);
        if (!isPressed) {
            mSingleImage.setVisibility(View.GONE);
            mCommentsBlock.setVisibility(View.GONE);
            mImageInfoBlock.setVisibility(View.VISIBLE);
            updateLabels(mImage.description);
            ((MainActivity) getActivity()).bindSingleImageFragment(this);
        } else {
            mImageInfoBlock.setVisibility(View.GONE);
            mSingleImage.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).bindSingleImageFragment(null);
        }

        if (mCallback != null) {
            mCallback.setPagingEnabled(isPressed);
        }
    }

    @Override
    public void onDestroy() {
        ((MainActivity) getActivity()).bindSingleImageFragment(null);
        super.onDestroy();
    }

    private void loadComments() {
        loadComments(false);
    }

    private void loadComments(boolean isAddComment) {
        RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
        service.getCommentApi().getImageComments(mImage.id, new GetImageCommentsListener(isAddComment));
    }

    private void showComments() {
        boolean isPressed = mCommentsBlock.getVisibility() == View.VISIBLE;
        ((MainActivity) getActivity()).hideKeyboard();
        pressButton(mCommentButton, !isPressed);
        pressButton(mInfoButton, false);
        if (!isPressed) {
            loadComments();
            mCommentEditText.requestFocus();
            mSingleImage.setVisibility(View.GONE);
            mImageInfoBlock.setVisibility(View.GONE);
            mCommentsBlock.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).bindSingleImageFragment(this);
        } else {
            mCommentsBlock.setVisibility(View.GONE);
            mSingleImage.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).bindSingleImageFragment(null);
        }

        if (mCallback != null) {
            mCallback.setPagingEnabled(isPressed);
        }
    }

    public void backPressed() {
        pressButton(mCommentButton, false);
        pressButton(mInfoButton, false);
        mCommentsBlock.setVisibility(View.GONE);
        mImageInfoBlock.setVisibility(View.GONE);
        mSingleImage.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).bindSingleImageFragment(null);
        if (mCallback != null) {
            mCallback.setPagingEnabled(true);
        }
    }

    public void updateLabels(String description) {
        mImageDescription.setText(description);
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
                case R.id.view_single_image_comment_button:
                    showComments();
                    break;
                case R.id.view_single_image_send_comment_button:
                    sendComment();
                    break;
            }
        }

        private void sendComment() {
            String comment = mCommentEditText.getText().toString().trim();
            try {
                if (comment.length() == 0) {
                    throw new ToastException(R.string.enter_comment);
                }

                RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
                service.getCommentApi().addImageComments(mImage.id, comment, new AddImageCommentsListener());
            } catch (ToastException e) {
                mCommentEditText.requestFocus();
                e.show(mContext);
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

        private class AddImageCommentsListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                loadComments(true);
            }

            @Override
            public void failure(RetrofitError error) {
                Response response = error.getResponse();
                try {
                    if (response == null) {
                        throw new ToastException(R.string.error_network_problem);
                    }

                    if (response.getStatus() == 401) {
                        throw new ToastException(R.string.error_wrong_password);
                    }

                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    throw new ToastException(err.error);
                } catch (ToastException e) {
                    e.show(mContext);
                }
            }
        }
    }


    private class GetImageCommentsListener implements Callback<List<Comment>> {
        private final boolean mIsAddComment;

        public GetImageCommentsListener(boolean isAddComment) {
            mIsAddComment = isAddComment;
        }

        @Override
        public void success(List<Comment> comments, Response response) {
            if (comments != null) {
                mCommentsAdapter = new CommentsAdapter(mContext, comments);
                mCommentsList.setAdapter(mCommentsAdapter);

                mImage.comments_count = comments.size();

                if (mImage.comments_count > 0) {
                    mCommentCount.setText(String.valueOf(mImage.comments_count));
                    if (mCommentCount.getVisibility() == View.GONE) {
                        mCommentCount.setVisibility(View.VISIBLE);
                    }
                }

                if (mIsAddComment) {
                    mCommentEditText.setText("");
                    ((MainActivity) getActivity()).hideKeyboard();
                    mCommentsList.setSelection(mCommentsAdapter.getCount() - 1);
                }

                registerForContextMenu(mCommentsList);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                throw new ToastException(err.error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    private class RemoveCommentConfirmationListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
        private final Comment mComment;

        public RemoveCommentConfirmationListener(Comment comment) {
            this.mComment = comment;
        }

        @Override
        public void onPositiveClickHandler() {
            RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
            service.getCommentApi().removeComment(this.mComment.id, new RemoveCommentListener(this.mComment));
        }

        private class RemoveCommentListener implements Callback<Void> {
            private final Comment mComment;

            public RemoveCommentListener(Comment comment) {
                this.mComment = comment;
            }

            @Override
            public void success(Void aVoid, Response response) {
                Common.showMessage(mContext, R.string.comment_removed);
                mCommentsAdapter.remove(this.mComment);
                mCommentsAdapter.notifyDataSetChanged();
                mImage.comments_count--;
                if (mImage.comments_count == 0) {
                    mCommentCount.setVisibility(View.GONE);
                } else {
                    mCommentCount.setText(String.valueOf(mImage.comments_count));
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Response response = error.getResponse();
                try {
                    if (response == null) {
                        throw new ToastException(R.string.error_network_problem);
                    }

                    if (response.getStatus() == 401) {
                        throw new ToastException(R.string.error_wrong_password);
                    }

                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    throw new ToastException(err.error);
                } catch (ToastException e) {
                    e.show(mContext);
                }
            }
        }
    }
}
