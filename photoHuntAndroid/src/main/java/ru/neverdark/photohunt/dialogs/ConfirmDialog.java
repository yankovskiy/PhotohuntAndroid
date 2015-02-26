package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;

public class ConfirmDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "confirmDialog";
    private int mTitleId;
    private int mMessageId;
    private String mMessage;

    public static ConfirmDialog getInstance(Context context) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void bindObjects() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    public void setMessages(int titleId, int messageId) {
        mTitleId = titleId;
        mMessageId = messageId;
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        if (mTitleId != 0) {
            getAlertDialog().setTitle(mTitleId);
        }
        if (mMessageId != 0) {
            getAlertDialog().setMessage(mMessageId);
        } else if (mMessage != null) {
            getAlertDialog().setMessage(mMessage);
        }
    }

    public void setMessage(int messageId) {
        mMessageId = messageId;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public void setTitle(int titleId) {
        mTitleId = titleId;
    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler();
    }

    private class PositiveClickListener implements OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClickHandler();
            }
        }
    }
}
