package ru.neverdark.photohunt.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.User;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ToastException;

public class ResetPasswordDialog extends UfoDialogFragment {
    private class GenerateHashHandler implements Callback<User> {
        private final UProgressDialog mDialog;
        
        public GenerateHashHandler() {
            mDialog = UProgressDialog.getInstance(getContext());
            mDialog.show(R.string.password_request, R.string.please_wait);
        }
        
        @Override
        public void failure(RetrofitError error) {
            mDialog.dismiss();
            Response response = error.getResponse();

            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 404) {
                    throw new ToastException(R.string.error_email_not_found);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(getContext());
            }
        }

        @Override
        public void success(User user, Response response) {
            mDialog.dismiss();
            Common.showMessage(getContext(), R.string.check_your_mail);
            dismiss();
        }

    }

    private class PositiveClickListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            String userId = mUserId.getText().toString();
            try {
                if (Common.isValidEmail(userId) == false) {
                    throw new ToastException(R.string.error_bad_email);
                }
                
                RestService service = new RestService();
                User user = new User();
                user.user_id = userId;
                service.getUserApi().generateHash(user, new GenerateHashHandler());
            } catch (ToastException e) {
                e.show(getContext());
            }
        }

    }

    private EditText mUserId;
    public static final String DIALOG_ID = "resetPasswordDialog";

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.reset_password_dialog, null));
        mUserId = (EditText) getDialogView().findViewById(R.id.reset_userid);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, null);
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new PositiveClickListener());
    }

    public static ResetPasswordDialog getInstance(Context context) {
        ResetPasswordDialog dialog = new ResetPasswordDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.reset_password);
        getAlertDialog().setMessage(R.string.email_for_send_password);
    }

}
