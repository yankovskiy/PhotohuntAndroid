package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;

public class ConfirmDialog extends UfoDialogFragment {
    private class PositiveClickListener implements OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClickHandler();
            }
        }
    }

    public static final String DIALOG_ID = "confirmDialog";
    private int mTitleId;
    private int mMessageId;
    
    @Override
    public void bindObjects() {
        // TODO Auto-generated method stub

    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler();
    }
    
    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    public static ConfirmDialog getInstance(Context context) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setContext(context);
        return dialog;
    }
    
    public void setMessages(int titleId, int messageId) {
        mTitleId = titleId;
        mMessageId = messageId;
    }
    
    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(mTitleId);
        getAlertDialog().setMessage(mMessageId);
    }
    
}
