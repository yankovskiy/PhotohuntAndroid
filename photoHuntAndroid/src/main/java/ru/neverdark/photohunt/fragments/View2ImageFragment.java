package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.EditImageDialog;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.CustomViewPager;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;

public class View2ImageFragment extends UfoFragment {
    private Image mImage;
    private View mView;
    private Context mContext;
    private CustomViewPager mViewPager;
    private Data mData;
    private boolean mIsHideActionBar = false;

    public static View2ImageFragment getInstance(Data data) {
        View2ImageFragment fragment = new View2ImageFragment();
        fragment.mData = data;

        return fragment;
    }

    public static View2ImageFragment getInstance(Data data, boolean isHideActionBar) {
        View2ImageFragment fragment = getInstance(data);
        fragment.mIsHideActionBar = isHideActionBar;
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_image:
                showEditImageDialog();
                break;
            case R.id.remove_image:
                showRemoveDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditImageDialog() {
        EditImageDialog dialog = EditImageDialog.getInstance(mContext);
        dialog.setImage(mImage);
        dialog.setCallback(new EditImageDialogListener());
        dialog.show(getFragmentManager(), EditImageDialog.DIALOG_ID);
    }

    private void showRemoveDialog() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessages(R.string.delete_confirmation_title, R.string.image_delete_confirmation_message);
        dialog.setCallback(new RemoveConfirmationListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.enter();
        if (!mIsHideActionBar) {
            inflater.inflate(R.menu.view_single_image, menu);
            if (mImage.is_editable) {
                menu.findItem(R.id.edit_image).setVisible(true);
                menu.findItem(R.id.remove_image).setVisible(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void bindObjects() {
        mViewPager = (CustomViewPager) mView.findViewById(R.id.pager);
        mViewPager.setAdapter(new PhotoPagerAdapter(getChildFragmentManager()));
    }

    @Override
    public void setListeners() {
        mViewPager.setOnPageChangeListener(new PhotoPageChangeListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.view2_image_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        mViewPager.setCurrentItem(mData.getPosition());
        if (!mIsHideActionBar) {
            updateActionBar(mData.getPosition());
        }
        setHasOptionsMenu(true);
        return mView;
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).getActionBarLayout(false);
        super.onDestroyView();
    }

    private void updateActionBar(int imagePosition) {
        View view = ((MainActivity) getActivity()).getActionBarLayout(true);
        TextView author = (TextView) view.findViewById(R.id.custom_actionbar_title);
        TextView subject = (TextView) view.findViewById(R.id.custom_actionbar_subtitle);
        ImageView avatar = (ImageView) view.findViewById(R.id.custom_actionbar_image);

        mImage = mData.getImage(imagePosition);
        if (mImage.display_name != null) {
            author.setText(mImage.display_name);
            author.setOnClickListener(new UserClickListener());
            avatar.setOnClickListener(new UserClickListener());
        } else {
            author.setText(R.string.hidden);
        }

        if (mImage.subject != null) {
            subject.setText(mImage.subject);
        } else if (mImage.contest_status == Contest.STATUS_VOTES) {
            String voteCount = String.format(Locale.US, "%s: %d", getString(R.string.remaining_votes), mData.getVoteCount());
            subject.setText(voteCount);
        } else {
            subject.setText(R.string.hidden);
        }

        if (mImage.user_id == 1L) { // System
            avatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
        } else if (mImage.avatar != null && mImage.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), mImage.avatar);
            Picasso.with(mContext).load(url).transform(new TransformAvatar()).placeholder(R.drawable.no_avatar).tag(mContext).into(avatar);
        } else {
            avatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
        }
    }

    private void openProfileFragment(long userId) {
        ProfileFragment fragment = ProfileFragment.getInstance(userId);
        Common.openFragment(this, fragment, true);
    }

    public static class Data {
        private final List<Image> mImages;
        private final int mPosition;
        private int mVoteCount;

        public Data(List<Image> images, int voteCount, int position) {
            mImages = images;
            mVoteCount = voteCount;
            mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }

        public List<Image> getImages() {
            return mImages;
        }

        public Image getImage(int position) {
            return mImages.get(position);
        }

        public int getVoteCount() {
            return mVoteCount;
        }

        public void incVoteCount() {
            if (mVoteCount < Contest.MAX_VOTE_COUNT) {
                mVoteCount++;
            }
        }

        public void decVoteCount() {
            if (mVoteCount > 0) {
                mVoteCount--;
            }
        }
    }

    public class PhotoPagerAdapter extends FragmentPagerAdapter {
        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ViewSingleImageFragment fragment = ViewSingleImageFragment.getInstance(mData.getImage(position));
            fragment.setCallback(new ButtonsClickListener());
            return fragment;
        }

        @Override
        public int getCount() {
            return mData.getImages().size();
        }

        private class ButtonsClickListener implements ViewSingleImageFragment.OnButtonsClickListener {
            @Override
            public void incVote() {
                mData.incVoteCount();
                updateVoteCount();
            }

            @Override
            public void decVote() {
                mData.decVoteCount();
                updateVoteCount();
            }

            @Override
            public void setPagingEnabled(boolean enabled) {
                mViewPager.setPagingEnabled(enabled);
            }

            private void updateVoteCount() {
                View view = ((MainActivity) getActivity()).getSupportActionBar().getCustomView();
                TextView subtitle = (TextView) view.findViewById(R.id.custom_actionbar_subtitle);
                String voteCount = String.format(Locale.US, "%s: %d", getString(R.string.remaining_votes), mData.getVoteCount());
                subtitle.setText(voteCount);
            }
        }
    }


    private class TransformAvatar implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = (int) mContext.getResources().getDimension(R.dimen.min_avatar_size);
            return Common.resizeBitmap(source, targetWidth, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    private class PhotoPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (!mIsHideActionBar) {
                updateActionBar(position);
                getActivity().supportInvalidateOptionsMenu();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class UserClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openProfileFragment(mImage.user_id);
        }
    }

    private class EditImageDialogListener implements EditImageDialog.OnPositiveClickListener, OnCallback {
        private Image mTempImage;

        @Override
        public void onPositiveClickHandler(Image image) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            Log.variable("image", image.toString());
            mTempImage = image;
            RestService service = new RestService(user, pass);
            service.getContestApi().updateImage(image.id, image, new RestEditImageListener());
        }

        private class RestEditImageListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                Common.showMessage(mContext, R.string.information_chanded);
                View view = ((MainActivity) getActivity()).getSupportActionBar().getCustomView();
                TextView subtitle = (TextView) view.findViewById(R.id.custom_actionbar_subtitle);
                subtitle.setText(mTempImage.subject);
                mImage.subject = mTempImage.subject;
                mImage.description = mTempImage.description;
            }

            @Override
            public void failure(RetrofitError error) {
                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                Common.showMessage(mContext, err.error);
            }
        }
    }

    private class RemoveConfirmationListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
        @Override
        public void onPositiveClickHandler() {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService service = new RestService(user, pass);
            service.getContestApi().deleteImage(mImage.id, new RemoveImageListener());
        }

        private class RemoveImageListener implements Callback<Void> {
            @Override
            public void success(Void aVoid, Response response) {
                getFragmentManager().popBackStack();
                Common.showMessage(mContext, R.string.image_removed);
            }

            @Override
            public void failure(RetrofitError error) {
                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                Common.showMessage(mContext, err.error);
            }
        }
    }
}
