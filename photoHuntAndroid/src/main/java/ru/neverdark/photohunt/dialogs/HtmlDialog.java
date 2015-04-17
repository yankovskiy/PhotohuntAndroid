package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;

public class HtmlDialog extends UfoDialogFragment {

    public static final String DIALOG_ID = "htmlDialog";
    private String mUrl;
    private int mTitleResId;
    private WebView mWeb;
    private boolean mIsSingleButton = false;

    public static HtmlDialog getInstance(Context context, String url, int titleResId) {
        HtmlDialog dialog = new HtmlDialog();
        dialog.setContext(context);
        dialog.mUrl = url;
        dialog.mTitleResId = titleResId;
        return dialog;
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.html_dialog, null));
        mWeb = (WebView) getDialogView().findViewById(R.id.html_webview);
        mWeb.setBackgroundColor(Color.TRANSPARENT);
        mWeb.getSettings().setDefaultFontSize(12);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
        if (mIsSingleButton == false) {
            getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        }
    }

    private void loadData() {
        mWeb.loadUrl("file:///android_asset/".concat(mUrl));
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(mTitleResId);
        loadData();
    }

    public void setSingleButtonMode(boolean isSingleButtonMode) {
        mIsSingleButton = isSingleButtonMode;
    }

    public interface OnPositiveClickListener {
        public void onPositiveClick();
    }

    private class PositiveClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClick();
            }
        }
    }
}
