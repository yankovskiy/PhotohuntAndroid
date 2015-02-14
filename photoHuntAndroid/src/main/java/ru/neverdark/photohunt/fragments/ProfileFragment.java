package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.UProgressDialog;
import ru.neverdark.photohunt.dialogs.UpdateProfileDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.User;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class ProfileFragment extends UfoFragment {

    private Context mContext;
    private View mView;
    private boolean mIsDataLoaded = false;
    private TextView mDisplayName;
    private TextView mRank;
    private TextView mWins;
    private TextView mImagesCount;
    private TextView mCardInsta;
    private TextView mCardUserId;
    private TextView mCardBalance;
    private ImageView mButtonAlbum;
    private ImageView mButtonEdit;
    private ImageView mButtonRemove;
    private ImageView mButtonInsta;
    private User mUserData;
    private boolean mIsSelf;

    private void updateProfileInfo(User user) {
        String balance = String.format(Locale.US, "%s: %d", getString(R.string.rating_count), user.balance);
        String email = Settings.getUserId(mContext);
        if (mIsSelf) {
            mCardUserId.setText(email);
        }

        String worksCount = String.format(Locale.US, "%s: %d", getString(R.string.works_count), user.images_count);
        String winsCount = String.format(Locale.US, "%s: %d", getString(R.string.wins_count), user.wins_count);
        String rank = String.format(Locale.US, "%s: %d", getString(R.string.rating_position), user.rank);

        mWins.setText(winsCount);
        mRank.setText(rank);
        mImagesCount.setText(worksCount);
        mCardBalance.setText(balance);
        mDisplayName.setText(user.display_name);

        visibilityControl(user);
    }

    private void visibilityControl(User user) {
        if (user.insta == null || user.insta.trim().length() == 0) {
            mCardInsta.setVisibility(View.GONE);
            mButtonInsta.setVisibility(View.GONE);
        } else {
            mButtonInsta.setVisibility(View.VISIBLE);
            mCardInsta.setVisibility(View.VISIBLE);
            mCardInsta.setText(user.insta);
        }
    }

    @Override
    public void bindObjects() {
        mCardBalance = (TextView) mView.findViewById(R.id.profile_card_balance);
        mDisplayName = (TextView) mView.findViewById(R.id.profile_displayname);
        mButtonAlbum = (ImageView) mView.findViewById(R.id.profile_button_album);
        mButtonInsta = (ImageView) mView.findViewById(R.id.profile_button_insta);
        mCardInsta = (TextView) mView.findViewById(R.id.profile_insta);
        mImagesCount = (TextView) mView.findViewById(R.id.profile_images_count);
        mRank = (TextView) mView.findViewById(R.id.profile_card_rank);
        mWins = (TextView) mView.findViewById(R.id.profile_card_wins);

        if (mIsSelf) {
            mButtonEdit = (ImageView) mView.findViewById(R.id.profile_button_edit);
            mButtonRemove = (ImageView) mView.findViewById(R.id.profile_button_remove);
            mCardUserId = (TextView) mView.findViewById(R.id.profile_card_userid);
        }
    }

    private void showInstaProfile(String user) {
        String url = String.format(Locale.US, "http://instagram.com/_u/%s", user);
        Uri uri = Uri.parse(url);
        Intent insta = new Intent(Intent.ACTION_VIEW, uri);
        insta.setPackage("com.instagram.android");

        if (isIntentAvailable(mContext, insta)) {
            startActivity(insta);
        } else {
            url = String.format(Locale.US, "http://instagram.com/%s", user);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }

    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    public void setListeners() {
        if (mIsSelf) {
            mButtonRemove.setOnTouchListener(new ImageOnTouchListener());
            mButtonEdit.setOnTouchListener(new ImageOnTouchListener());
            mButtonInsta.setOnTouchListener(new ImageOnTouchListener());
            mButtonEdit.setOnClickListener(new ButtonOnClickListener());
            mButtonRemove.setOnClickListener(new ButtonOnClickListener());
            mButtonInsta.setOnClickListener(new ButtonOnClickListener());
        }

        mButtonAlbum.setOnTouchListener(new ImageOnTouchListener());
        mButtonAlbum.setOnClickListener(new ButtonOnClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        int layoutId;
        if (mIsSelf) {
            layoutId = R.layout.self_profile_fragment;
        } else {
            layoutId = R.layout.profile_fragment;
        }

        mView = inflater.inflate(layoutId, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.profile);

        return mView;
    }

    private final static String USER_ID = "userId";
    private long mUserId;

    public static ProfileFragment getInstance(long userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putLong(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getLong(USER_ID);
        }

        mIsSelf = (mUserId == 0L);
    }

    private void removeProfile() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setCallback(new OnConfirmHandler());
        dialog.setMessages(R.string.delete_confirmation_title, R.string.profile_delete_confirmation_message);
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    private void showUpdateProfileDialog() {
        if (mUserData != null) {
            mUserData.password = Settings.getPassword(mContext);

            UpdateProfileDialog dialog = UpdateProfileDialog.getInstance(mContext);
            dialog.setUser(mUserData);
            dialog.setCallback(new OnUpdateProfileHandler());
            dialog.show(getFragmentManager(), UpdateProfileDialog.DIALOG_ID);
        }
    }

    private void getProfile() {
        if (!mIsDataLoaded) {
            String user = Settings.getUserId(mContext);
            String password = Settings.getPassword(mContext);
            try {
                if (user.length() == 0 || password.length() == 0) {
                    throw new ToastException(R.string.error_not_authorized);
                }

                RestService service = new RestService(user, password);
                if (mIsSelf) {
                    service.getUserApi().getUser(user, new GetUserHandler(mView));
                } else {
                    service.getUserApi().getUser(mUserId, new GetUserHandler(mView));
                }
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.enter();
        Log.variable("isLoaded", String.valueOf(mIsDataLoaded));
        getProfile();
    }

    private class DeleteUserHandler implements Callback<User> {
        private UProgressDialog mDialog;

        public DeleteUserHandler() {
            mDialog = UProgressDialog.getInstance(mContext);
            mDialog.show(R.string.deleting_profile, R.string.please_wait);
        }

        @Override
        public void failure(RetrofitError error) {
            mDialog.dismiss();
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401 || response.getStatus() == 403) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(User user, Response response) {
            mDialog.dismiss();
            SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.clear();
            editor.commit();

            WelcomeFragment fragment = new WelcomeFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
            ((UfoFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((UfoFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);

            Common.showMessage(mContext, R.string.accaunt_removed);
            if (mCallback != null) {
                mCallback.deleteSuccess();
            }
        }

    }

    public interface OnDeleteUser {
        public void deleteSuccess();
    }

    private OnDeleteUser mCallback;

    public void setCallback(OnDeleteUser callback) {
        mCallback = callback;
    }

    private class OnConfirmHandler implements OnCallback, ConfirmDialog.OnPositiveClickListener {

        @Override
        public void onPositiveClickHandler() {
            String userId = Settings.getUserId(mContext);
            String password = Settings.getPassword(mContext);

            RestService service = new RestService(userId, password);
            service.getUserApi().deleteUser(userId, new DeleteUserHandler());
        }
    }

    private class UpdateUserHandler extends CallbackHandler<Void> {
        private User mUser;

        public UpdateUserHandler(View view, User user) {
            super(view, R.id.profile_hide_when_loading, R.id.profile_loading_progress);
            mUser = user;
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(Void data, Response response) {
            SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_password), mUser.password);
            editor.commit();
            mDisplayName.setText(mUser.display_name);
            mCardInsta.setText(mUser.insta);
            mUserData.display_name = mUser.display_name;
            mUserData.insta = mUser.insta;
            visibilityControl(mUser);
            super.success(data, response);
            Common.showMessage(mContext, R.string.profile_updated);
        }

    }

    private class GetUserHandler extends CallbackHandler<User> {
        public GetUserHandler(View view) {
            super(view, R.id.profile_hide_when_loading, R.id.profile_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }

            mIsDataLoaded = false;
        }

        @Override
        public void success(User user, Response response) {
            updateProfileInfo(user);
            mIsDataLoaded = true;
            mUserData = user;
            super.success(user, response);
        }
    }

    private class ButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.profile_button_edit:
                    showUpdateProfileDialog();
                    break;
                case R.id.profile_button_remove:
                    removeProfile();
                    break;
                case R.id.profile_button_album:
                    showUserAlbum();
                    break;
                case R.id.profile_button_insta:
                    showInstaProfile(mUserData.insta);
                    break;
            }
        }
    }

    private void showUserAlbum() {
        long userId = mUserData.id;
        String displayName = mUserData.display_name;

        Log.variable("userId", String.valueOf(userId));

        UserImagesFragment fragment = UserImagesFragment.getInstance(userId, displayName);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private class OnUpdateProfileHandler implements OnCallback, UpdateProfileDialog.OnUpdateProfileListener {
        @Override
        public void updateProfileListener(User user) {
            try {
                String userId = Settings.getUserId(mContext);
                String password = Settings.getPassword(mContext);

                if (!mIsDataLoaded || userId.length() == 0 || password.length() == 0) {
                    throw new ToastException(R.string.error_not_authorized);
                }

                if (user.display_name.length() == 0) {
                    throw new ToastException(R.string.error_empty_display_name);
                }

                if (user.password.length() == 0) {
                    throw new ToastException(R.string.error_empty_password);
                }

                RestService service = new RestService(userId, password);
                service.getUserApi().updateUser(userId, user, new UpdateUserHandler(mView, user));

            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }
}
