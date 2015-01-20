package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastException extends Exception {
    private static final long serialVersionUID = 1L;
    private int mMessageId;
    private String mMessage;
    
    public ToastException(int messageId) {
        mMessageId = messageId;
    }

    public ToastException(String error) {
        mMessage = error;
    }

    public void show(Context context) {
        if (mMessageId != 0) {
            Toast.makeText(context, mMessageId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, mMessage, Toast.LENGTH_LONG).show();
        }
    }
}
