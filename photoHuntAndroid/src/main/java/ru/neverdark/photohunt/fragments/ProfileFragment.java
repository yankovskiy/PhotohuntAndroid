package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.User;
import ru.neverdark.photohunt.utils.ButtonOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class ProfileFragment extends UfoFragment {

    private Context mContext;
    private View mView;
    private boolean mIsDataLoaded = false;
    private TextView mDisplayName;
    private TextView mCardUserId;
    private TextView mCardBalance;
    private TextView mCardVotes;
    private TextView mButtonEdit;
    private TextView mButtonRemove;
    private User mUserData;

    private void updateProfileInfo(User user) {
        String balance = String.format(Locale.US, "%s: %d", getString(R.string.rating_count), user.balance);
        String email = Settings.getUserId(mContext);
        String votes = String.format(Locale.US, "%s: %d", getString(R.string.vote_count), user.vote_count);
        mCardUserId.setText(email);
        mCardBalance.setText(balance);
        mDisplayName.setText(user.display_name);
        mCardVotes.setText(votes);
    }

    @Override
    public void bindObjects() {
        mCardBalance = (TextView) mView.findViewById(R.id.profile_card_balance);
        mDisplayName = (TextView) mView.findViewById(R.id.profile_displayname);
        mCardUserId = (TextView) mView.findViewById(R.id.profile_card_userid);
        mCardVotes = (TextView) mView.findViewById(R.id.profile_card_votes);
        mButtonEdit = (TextView) mView.findViewById(R.id.profile_button_edit);
        mButtonRemove = (TextView) mView.findViewById(R.id.profile_button_remove);
    }

    @Override
    public void setListeners() {
        mButtonRemove.setOnTouchListener(new ButtonOnTouchListener());
        mButtonEdit.setOnTouchListener(new ButtonOnTouchListener());
        mButtonEdit.setOnClickListener(new ButtonOnClickListener());
        mButtonRemove.setOnClickListener(new ButtonOnClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.profile_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        return mView;
    }

    private void removeProfile() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setCallback(new OnConfirmHandler());
        dialog.setMessages(R.string.delete_confirmation_title, R.string.delete_confirmation_message);
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    private void showUpdateProfile() {
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
                service.getUserApi().getUser(user, new GetUserHandler());
            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        }

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

    private class UpdateUserHandler implements Callback<User> {
        private UProgressDialog mDialog;
        private User mUser;

        public UpdateUserHandler(User user) {
            mDialog = UProgressDialog.getInstance(mContext);
            mDialog.show(R.string.loading_info, R.string.please_wait);
            mUser = user;
        }

        @Override
        public void failure(RetrofitError error) {
            mDialog.dismiss();
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
        public void success(User user, Response response) {
            mDialog.dismiss();
            SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_password), mUser.password);
            editor.commit();
            mDisplayName.setText(mUser.display_name);
            mUserData = mUser;
            Common.showMessage(mContext, R.string.profile_updated);
        }

    }

    private class GetUserHandler implements Callback<User> {

        private UProgressDialog mDialog;

        public GetUserHandler() {
            mDialog = UProgressDialog.getInstance(mContext);
            mDialog.show(R.string.loading_info, R.string.please_wait);
        }

        @Override
        public void failure(RetrofitError error) {
            mDialog.dismiss();
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
            mDialog.dismiss();
            updateProfileInfo(user);
            mIsDataLoaded = true;
            mUserData = user;
        }
    }

    private class ButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.profile_button_edit:
                    showUpdateProfile();
                    break;
                case R.id.profile_button_remove:
                    removeProfile();
                    break;
            }
        }
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
                service.getUserApi().updateUser(userId, user, new UpdateUserHandler(user));

            } catch (ToastException e) {
                e.show(mContext);
            }
        }
    }
}
