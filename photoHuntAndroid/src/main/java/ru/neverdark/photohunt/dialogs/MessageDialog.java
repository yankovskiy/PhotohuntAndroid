package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;

public class MessageDialog extends UfoDialogFragment {

    public static final String DIALOG_ID = "messageDialog";

    private int mTitleId = 0;
    private int mMessageId = 0;
    private String mTitle = null;
    private String mMessage = null;
    
    @Override
    public void bindObjects() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new CancelClickListener());
    }
    
    public static MessageDialog getInstance(Context context) {
        MessageDialog dialog = new MessageDialog();
        dialog.setContext(context);
        return dialog;
    }
    
    @Override
    public void createDialog() {
        super.createDialog();
        if (mTitleId != 0) {
            getAlertDialog().setTitle(mTitleId);
            getAlertDialog().setMessage(mMessageId);
        } else {
            getAlertDialog().setTitle(mTitle);
            getAlertDialog().setMessage(mMessage);
        }
    }
    
    public void setMessages(int titleId, int messageId) {
        mTitleId = titleId;
        mMessageId = messageId;
    }
    
    public void setMessages(String title, String message) {
        mTitle = title;
        mMessage = message;
    }

}
