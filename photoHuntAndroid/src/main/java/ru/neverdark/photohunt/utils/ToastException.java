package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int mMessageId;
    
    public ToastException(int messageId) {
        mMessageId = messageId;
    }
    
    public void show(Context context) {
        Toast.makeText(context, mMessageId, Toast.LENGTH_LONG).show();
    }
}
