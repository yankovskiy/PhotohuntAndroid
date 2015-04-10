package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.UProgressDialog;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.User;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ToastException;

public class RegisterUserFragment extends UfoFragment {
    private boolean mIsLogin = false;

    private class AddUserHandler implements Callback<User> {
        private UProgressDialog mDialog;

        public AddUserHandler() {
            mDialog = UProgressDialog.getInstance(mContext);
            mDialog.show(R.string.registering, R.string.please_wait);
        }

        @Override
        public void failure(RetrofitError error) {
            mDialog.dismiss();
            Response response = error.getResponse();

            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 403) {
                    throw new ToastException(R.string.error_user_already_exists);
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
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getString(R.string.pref_isLogin), true);
            editor.putString(getString(R.string.pref_userId), mUserId.getText().toString());
            editor.putString(getString(R.string.pref_password), mPassword.getText().toString());
            editor.commit();

            getFragmentManager().popBackStack();
            BriefContestFragment fragment = new BriefContestFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
            ((UfoFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((UfoFragmentActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            mIsLogin = true;
        }
    }

    private EditText mDisplayName;
    private EditText mPassword;
    private EditText mUserId;
    private Context mContext;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.register_user_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.app_name);
        return mView;
    }

    @Override
    public void onDestroy() {
        if (!mIsLogin) {
            ((UfoFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((UfoFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((UfoFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((UfoFragmentActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public void bindObjects() {
        mDisplayName = (EditText) mView.findViewById(R.id.register_displayname);
        mPassword = (EditText) mView.findViewById(R.id.register_password);
        mUserId = (EditText) mView.findViewById(R.id.register_userid);
    }

    @Override
    public void setListeners() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.register_user, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.register_user_done:
            addUser();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addUser() {
        String userId = mUserId.getText().toString();
        String displayName = mDisplayName.getText().toString();
        String password = mPassword.getText().toString();
        try {
            if (Common.isValidEmail(userId) == false) {
                throw new ToastException(R.string.error_bad_email);
            }
            
            if (displayName.length() == 0) {
                throw new ToastException(R.string.error_empty_display_name);
            }
            
            if (password.length() == 0) {
                throw new ToastException(R.string.error_empty_password);
            }

            ((MainActivity) getActivity()).hideKeyboard();

            RestService restService = new RestService();
            User user = new User();
            user.display_name = displayName;
            user.user_id = userId;
            user.password = password;
            restService.getUserApi().addUser(user, new AddUserHandler());
        } catch (ToastException e) {
            e.show(mContext);
        }
    }

}
