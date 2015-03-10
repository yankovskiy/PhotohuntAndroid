package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент для редактирования профиля
 */
public class EditProfileFragment extends UfoFragment{

    private RestService.User mUserData;
    private View mView;
    private Context mContext;

    private EditText mDisplayName;
    private EditText mInsta;
    private EditText mEmail;
    private EditText mPassword;
    private TextView mRemoveButton;

    @Override
    public void bindObjects() {
        mDisplayName = (EditText) mView.findViewById(R.id.edit_profile_displayname);
        mInsta = (EditText) mView.findViewById(R.id.edit_profile_insta);
        mEmail = (EditText) mView.findViewById(R.id.edit_profile_email);
        mPassword = (EditText) mView.findViewById(R.id.edit_profile_password);
        mRemoveButton = (TextView) mView.findViewById(R.id.edit_profile_remove);
    }

    @Override
    public void setListeners() {
        mRemoveButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mRemoveButton.setOnClickListener(new RemoveProfileListener());
    }

    public static EditProfileFragment getInstance(RestService.User userData) {
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.mUserData = userData;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {

        mView = inflater.inflate(R.layout.edit_profile_fragment, container, false);
        mContext = mView.getContext();

        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.profile);

        setHasOptionsMenu(true);
        bindData();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile_action:
                sendProfileData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Отправляет запрос на редактирование на сервер
     */
    private void sendProfileData() {
        try {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            String displayName = mDisplayName.getText().toString().trim();
            String insta = mInsta.getText().toString().trim();
            String password = mPassword.getText().toString();

            if (user.length() == 0 || pass.length() == 0) {
                throw new ToastException(R.string.error_not_authorized);
            }

            if (displayName.length() == 0) {
                throw new ToastException(R.string.error_empty_display_name);
            }

            if (password.length() == 0) {
                password = null;
            }

            RestService.User sendData = new RestService.User();
            sendData.display_name = displayName;
            sendData.insta = insta;
            sendData.password = password;

            setHasOptionsMenu(false);
            ((UfoFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);

            RestService service = new RestService(user, pass);
            service.getUserApi().updateUser(user, sendData, new UpdateUserListener(password));

        } catch (ToastException e) {
            e.show(mContext);
        }
    }

    public void onDetach() {
        ((UfoFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        super.onDetach();
    }

    /**
     * Маппит данные из UserData в элементы управленя
     */
    private void bindData() {
        mDisplayName.setText(mUserData.display_name);
        mEmail.setText(mUserData.user_id);
        mInsta.setText(mUserData.insta);
    }

    /**
     * Обработчик нажатия на кнопку "удалить профиль"
     */
    private class RemoveProfileListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
            dialog.setCallback(new RemoveProfileDialogListener());
            dialog.setMessages(R.string.delete_account_title, R.string.delete_account_message);
            dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
        }

        /**
         * Обработчик нажатия на кнопку "Да" в диалоге удаления аккаунта
         */
        private class RemoveProfileDialogListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
            @Override
            public void onPositiveClickHandler() {
                setHasOptionsMenu(false);
                ((UfoFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
                String userId = Settings.getUserId(mContext);
                String password = Settings.getPassword(mContext);

                RestService service = new RestService(userId, password);
                service.getUserApi().deleteUser(userId, new DeleteUserListener());
            }

            /**
             * Обработчик удаления пользователя
             */
            private class DeleteUserListener implements Callback<RestService.User> {
                @Override
                public void success(RestService.User user, Response response) {
                    SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.commit();

                    ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
                    ((MainActivity) getActivity()).resetBackButtonToDefault();

                    WelcomeFragment fragment = new WelcomeFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    transaction.replace(R.id.main_container, fragment);
                    transaction.commit();
                    fragmentManager.executePendingTransactions();

                    Common.showMessage(mContext, R.string.accaunt_removed);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Response response = retrofitError.getResponse();
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
            }
        }
    }

    /**
     * Обработчик удаления пользователя
     */
    private class UpdateUserListener implements Callback<Void> {
        private final String mPassword;

        public UpdateUserListener(String newPassword) {
            this.mPassword = newPassword;
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

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }

            ((UfoFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
            setHasOptionsMenu(true);
        }

        @Override
        public void success(Void data, Response response) {
            if (mPassword != null) {
                SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.pref_password), mPassword);
                editor.commit();
            }

            getActivity().getSupportFragmentManager().popBackStack();
            Common.showMessage(mContext, R.string.profile_updated);
        }
    }
}
