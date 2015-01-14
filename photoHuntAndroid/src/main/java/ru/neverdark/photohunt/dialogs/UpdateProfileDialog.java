package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService.User;
import ru.neverdark.photohunt.utils.Log;

public class UpdateProfileDialog extends UfoDialogFragment {
    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.update_profile_dialog, null));
        mDispayName = (EditText) getDialogView().findViewById(R.id.update_profile_displayname);
        mPassword = (EditText) getDialogView().findViewById(R.id.update_profile_password);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.update_profile);
        loadDataToView();
    }

    private void loadDataToView() {
        if (mUser != null) {
            mDispayName.setText(mUser.display_name);
            mPassword.setText(mUser.password);
        }
    }

    public static final String DIALOG_ID = "updateProfileDialog";

    public static UpdateProfileDialog getInstance(Context context) {
        UpdateProfileDialog dialog = new UpdateProfileDialog();
        dialog.setContext(context);
        return dialog;
    }

    private class PositiveClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mCallback = (OnUpdateProfileListener) getCallback();
            if (mCallback != null) {
                User user = new User();
                user.display_name = mDispayName.getText().toString();
                user.password = mPassword.getText().toString();
                mCallback.updateProfileListener(user);
            }
        }
    }

    private OnUpdateProfileListener mCallback;
    private User mUser;
    private EditText mDispayName;
    private EditText mPassword;

    public void setUser(User user) {
        mUser = user;
    }

    public interface OnUpdateProfileListener {
        public void updateProfileListener(User user);
    }
}
