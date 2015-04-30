package ru.neverdark.photohunt.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.UserImagesAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.ContestDetail;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class Detail2ContestFragment extends UfoFragment {
    private static final int PICTURE_REQUEST_CODE = 1;
    private long mContestId;
    private int mContestStatus;
    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;
    private GridView mGridView;
    private List<Image> mImages;
    private Parcelable mGridState = null;
    private Contest mContest;
    private ImageView mCameraButton;
    private Uri outputFileUri;
    private int mVoteCount;

    public static Detail2ContestFragment getInstance(long contestId) {
        Detail2ContestFragment fragment = new Detail2ContestFragment();
        fragment.mContestId = contestId;
        return fragment;
    }

    @Override
    public void bindObjects() {
        mGridView = (GridView) mView.findViewById(R.id.detail2_contest_images_grid);
        mCameraButton = (ImageView) mView.findViewById(R.id.detail2_contest_camera);
    }

    @Override
    public void setListeners() {
        mGridView.setOnItemClickListener(new PhotoClickListener());
        mCameraButton.setOnClickListener(new ChooseImageClickListener());
        mCameraButton.setOnTouchListener(new ImageOnTouchListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.detail2_contest_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(null);
        return mView;
    }

    private void openViewImageFragment(int position) {
        View2ImageFragment fragment = View2ImageFragment.getInstance(new View2ImageFragment.Data(mImages, mContestStatus, mVoteCount, position));
        Common.openFragment(this, fragment, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            loadData();
        }
    }

    private void loadData() {
        setHasOptionsMenu(false);
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getContestApi().getContestDetails(mContestId,
                new GetContestDetailsHandler(mView));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_prev_contest:
                if (mContest.prev_id != 0L) {
                    openContest(mContest.prev_id);
                } else {
                    Common.showMessage(mContext, R.string.error_contest_not_found);
                }
                break;
        }
        return true;
    }

    private void openContest(long contestId) {
        Detail2ContestFragment fragment = Detail2ContestFragment.getInstance(contestId);
        Common.openFragment(this, fragment, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_contest, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void openProfileFragment(long userId) {
        ProfileFragment fragment = ProfileFragment.getInstance(userId);
        Common.openFragment(this, fragment, true);
    }

    public void onDestroyView() {
        Log.enter();
        ((MainActivity) getActivity()).getActionBarLayout(false);
        mGridState = mGridView.onSaveInstanceState();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }

                UploadImageFragment fragment = UploadImageFragment.getInstance(selectedImageUri, mContest);
                fragment.setFileName(outputFileUri.getPath());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class PhotoClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openViewImageFragment(position);
        }
    }

    private class GetContestDetailsHandler extends CallbackHandler<ContestDetail> {
        public GetContestDetailsHandler(View view) {
            super(view, R.id.detail2_contest_hide_when_loading, R.id.detail2_contest_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            setHasOptionsMenu(true);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                if (response.getStatus() == 404) {
                    throw new ToastException(R.string.error_contest_not_found);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(ContestDetail data, Response response) {
            super.success(data, response);
            setHasOptionsMenu(true);
            if (data != null) {
                mContest = data.contest;
                mContestStatus = data.contest.status;
                mVoteCount = data.votes;
                updateActionBar(data.contest);
                if (data.images != null) {
                    mImages = data.images;
                    UserImagesAdapter adapter = new UserImagesAdapter(mContext, data.images);
                    mGridView.setAdapter(adapter);
                    if (mGridState != null) {
                        mGridView.onRestoreInstanceState(mGridState);
                    }
                }

                if (data.contest.status == Contest.STATUS_OPEN) {
                    mView.findViewById(R.id.detail2_contest_bottom).setVisibility(View.VISIBLE);
                }
            }
        }

        private void updateActionBar(Contest contest) {
            View view = ((MainActivity) getActivity()).getActionBarLayout(true);
            TextView author = (TextView) view.findViewById(R.id.custom_actionbar_title);
            TextView subject = (TextView) view.findViewById(R.id.custom_actionbar_subtitle);
            ImageView avatar = (ImageView) view.findViewById(R.id.custom_actionbar_image);
            author.setText(contest.display_name);
            subject.setText(contest.subject);

            if (contest.user_id == 1L) { // System
                avatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
            } else if (contest.avatar != null && contest.avatar.trim().length() > 0) {
                String url = String.format(Locale.US, "%s/avatars/%s.jpg?size=48dp", RestService.getRestUrl(), contest.avatar);
                Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(avatar);
            } else {
                avatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_avatar));
            }

            avatar.setOnClickListener(new UserClickListener(contest.user_id));
            author.setOnClickListener(new UserClickListener(contest.user_id));
        }

        private class Transform implements Transformation {
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

        private class UserClickListener implements View.OnClickListener {
            private final long mUserId;

            public UserClickListener(long userId) {
                this.mUserId = userId;
            }

            @Override
            public void onClick(View view) {
                openProfileFragment(mUserId);
            }
        }
    }

    private class ChooseImageClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "Photohunt" + File.separator);
            root.mkdirs();
            final String fname = Common.getUniqueImageFilename();
            final File sdImageMainDirectory = new File(root, fname);
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            // Camera.
            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = mContext.getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                cameraIntents.add(intent);
            }

            // Filesystem.
            final Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            // Chooser of filesystem options.
            final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.choose_source));

            // Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

            startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
        }
    }
}
