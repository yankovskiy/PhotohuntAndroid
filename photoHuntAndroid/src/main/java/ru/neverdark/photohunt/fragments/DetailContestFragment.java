package ru.neverdark.photohunt.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.DetailContestAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.EditImageDialog;
import ru.neverdark.photohunt.dialogs.MessageDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.Contest;
import ru.neverdark.photohunt.rest.RestService.ContestDetail;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.PicassoScrollListener;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Фрагмент содержащий детальную информацию о проводимом конкурсе Должен
 * реализовывать отображение всех работ, голосование, загрузку своей работыs
 */
@SuppressLint("ValidFragment")
public class DetailContestFragment extends UfoFragment {
    private RelativeLayout mDetailContestBottom;
    private DetailContestAdapter mAdapter;

    private class VoteHandler implements DetailContestAdapter.CallbackListener {

        @Override
        public void onVote(boolean isVoted) {
            if (!isVoted && mRemainingVotes > 0) {
                mRemainingVotes--;
            } else if (isVoted && mRemainingVotes < 3) {
                mRemainingVotes++;
            }
            updateRemainingVotes(mRemainingVotes);
        }

        @Override
        public void onRemoveImage(RestService.Image image) {
            ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
            dialog.setMessages(R.string.delete_confirmation_title, R.string.image_delete_confirmation_message);
            dialog.setCallback(new RemoveImageListener(image));
            dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
        }

        @Override
        public void onEditImage(RestService.Image image) {
            Log.enter();
            EditImageDialog dialog = EditImageDialog.getInstance(mContext);
            dialog.setImage(image);
            dialog.setCallback(new EditImageListener());
            dialog.show(getFragmentManager(), EditImageDialog.DIALOG_ID);
        }

        @Override
        public void showError(String message) {
            MessageDialog dialog = MessageDialog.getInstance(mContext);
            dialog.setMessages(getString(R.string.send_this_to_developer), message);
            dialog.show(getFragmentManager(), MessageDialog.DIALOG_ID);
        }
    }

    private int mRemainingVotes = 0;
    private static final int PICTURE_REQUEST_CODE = 1;

    private class ChoosePictureHandler implements OnClickListener {

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
            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

            // Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

            startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
        }

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

                Log.variable("uri", selectedImageUri.getPath());
                UploadImageFragment fragment = new UploadImageFragment(selectedImageUri, mContestId);
                fragment.setFileName(outputFileUri.getPath());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri outputFileUri;

    private class GetContestDetailsHandler extends CallbackHandler<ContestDetail> {

        public GetContestDetailsHandler(View view) {
            super(view);
            // TODO Auto-generated constructor stub
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
            setHasOptionsMenu(true);
            if (data != null) {
                mSubject.setText(data.contest.subject);
                mAuthor.setText(data.contest.display_name);
                mCloseDate.setText(data.contest.close_date);

                if (data.images != null) {
                    initList(data);
                }

                if (data.contest.status != Contest.STATUS_OPEN) {
                    mDetailContestBottom.setVisibility(View.GONE);
                } else {
                    mDetailContestBottom.setVisibility(View.VISIBLE);
                }

                if (data.contest.status == Contest.STATUS_VOTES) {
                    updateRemainingVotes(data.votes);
                }
            }
            super.success(data, response);
        }

        private void initList(ContestDetail contestDetail) {
            mAdapter = new DetailContestAdapter(mContext, contestDetail);
            mAdapter.setCallback(new VoteHandler());
            mContestList.setAdapter(mAdapter);
        }
    }

    private void updateRemainingVotes(int newVotes) {
        mRemainingVotes = newVotes;
        String votes = String.format(Locale.US, "%s: %d", getString(R.string.remaining_votes), newVotes);
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(votes);
    }


    private final long mContestId;
    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;
    private ListView mContestList;
    private TextView mSubject;
    private TextView mAuthor;
    private TextView mCloseDate;
    private ImageView mCamera;

    public DetailContestFragment(long contestId) {
        mContestId = contestId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.detail_contest_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_contest_refresh:
                refresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_contest, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void bindObjects() {
        mContestList = (ListView) mView.findViewById(R.id.detail_contest_list);
        mSubject = (TextView) mView.findViewById(R.id.detail_contest_subject);
        mCamera = (ImageView) mView.findViewById(R.id.detail_contest_camera);
        mAuthor = (TextView) mView.findViewById(R.id.detail_contest_author);
        mCloseDate = (TextView) mView.findViewById(R.id.detail_contest_close_date);
        mDetailContestBottom = (RelativeLayout) mView.findViewById(R.id.detail_contest_bottom);
    }

    @Override
    public void setListeners() {
        mContestList.setOnScrollListener(new PicassoScrollListener(mContext));
        mCamera.setOnTouchListener(new ImageOnTouchListener());
        mCamera.setOnClickListener(new ChoosePictureHandler());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsDataLoaded == false) {
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        super.onDestroy();
    }

    private void refresh() {
        setHasOptionsMenu(false);
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getContestApi().getContestDetails(mContestId,
                new GetContestDetailsHandler(mView));
    }

    private class RemoveImageListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {
        private final RestService.Image mImage;

        public RemoveImageListener(RestService.Image image) {
            mImage = image;
        }

        @Override
        public void onPositiveClickHandler() {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService service = new RestService(user, pass);
            service.getContestApi().deleteImage(mImage.id, new RestRemoveImageListener());
        }

        private class RestRemoveImageListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                Common.showMessage(mContext, R.string.image_removed);
                mAdapter.remove(mImage);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                RestService.ErrorData err = (RestService.ErrorData) retrofitError.getBodyAs(RestService.ErrorData.class);
                Common.showMessage(mContext, err.error);
            }
        }
    }

    private class EditImageListener implements OnCallback, EditImageDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler(RestService.Image image) {
            Log.enter();
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            Log.variable("image", image.toString());
            RestService service = new RestService(user, pass);
            service.getContestApi().updateImage(image.id, image, new RestEditImageListener());
        }

        private class RestEditImageListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                Common.showMessage(mContext, R.string.subject_changed);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                //Log.message(retrofitError.getMessage());
                RestService.ErrorData err = (RestService.ErrorData) retrofitError.getBodyAs(RestService.ErrorData.class);
                Common.showMessage(mContext, err.error);
            }
        }
    }
}
