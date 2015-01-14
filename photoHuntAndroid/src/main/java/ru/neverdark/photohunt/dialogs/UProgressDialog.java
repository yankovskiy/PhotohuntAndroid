package ru.neverdark.photohunt.dialogs;

import android.app.ProgressDialog;
import android.content.Context;

public class UProgressDialog {
    private ProgressDialog mDialog;
    private Context mContext;
    
    public static UProgressDialog getInstance(Context context) {
        UProgressDialog dialog = new UProgressDialog();
        dialog.mDialog = new ProgressDialog(context);
        dialog.mContext = context;
        dialog.mDialog.setCancelable(false);
        return dialog;
    }
    
    public void show(int titleId, int messageId) {
        mDialog.setTitle(titleId);
        mDialog.setMessage(mContext.getString(messageId));
        mDialog.show();
    }
    
    public void dismiss() {
        mDialog.dismiss();
    }
}
