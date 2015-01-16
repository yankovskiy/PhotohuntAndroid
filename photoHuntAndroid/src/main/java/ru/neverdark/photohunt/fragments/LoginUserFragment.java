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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.photohunt.R;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.dialogs.ResetPasswordDialog;
import ru.neverdark.photohunt.dialogs.UProgressDialog;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.User;
import ru.neverdark.photohunt.utils.ButtonOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.ToastException;

public class LoginUserFragment extends UfoFragment {
    private boolean mIsLogin = false;

    private class GetUserHandler implements Callback<User> {
        private UProgressDialog mDialog;
        
        public GetUserHandler() {
            mDialog = UProgressDialog.getInstance(mContext);
            mDialog.show(R.string.entering, R.string.please_wait);
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
            Log.variable("user", user.display_name);
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

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.login_lost_password:
                ResetPasswordDialog dialog = ResetPasswordDialog.getInstance(mContext);
                dialog.show(getFragmentManager(), ResetPasswordDialog.DIALOG_ID);
                break;
            }
        }

    }

    private TextView mLostPassword;
    private EditText mUserId;
    private EditText mPassword;
    private Context mContext;
    private View mView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.login_user_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        setHasOptionsMenu(true);
        return mView;
    }

    @Override
    public void onDestroy() {
        if(!mIsLogin) {
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
        mLostPassword = (TextView) mView.findViewById(R.id.login_lost_password);
        mUserId = (EditText) mView.findViewById(R.id.login_userid);
        mPassword = (EditText) mView.findViewById(R.id.login_password);
    }

    @Override
    public void setListeners() {
        mLostPassword.setOnClickListener(new ClickListener());
        mLostPassword.setOnTouchListener(new ButtonOnTouchListener());
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.login_user, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.login_user_done:
            loginUser();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loginUser() {
        String userId = mUserId.getText().toString();
        String password = mPassword.getText().toString();
        try {
            if (Common.isValidEmail(userId) == false) {
                throw new ToastException(R.string.error_bad_email);
            }
            
            if (password.length() == 0) {
                throw new ToastException(R.string.error_empty_password);
            }
            
            RestService service = new RestService(userId, password);
            service.getUserApi().getUser(userId, new GetUserHandler());
        } catch (ToastException e) {
            e.show(mContext);
        }
    }
}
