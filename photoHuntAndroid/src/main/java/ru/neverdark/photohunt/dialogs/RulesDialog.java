package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.webkit.WebView;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;

public class RulesDialog extends UfoDialogFragment {

    private class PositiveClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            OnAcceptRulesListener callback = (OnAcceptRulesListener) getCallback();
            if (callback != null) {
                callback.onAcceptRulesHandler();
            }
        }
    }
    
    public static final String DIALOG_ID = "rulesDialog";
    
    public interface OnAcceptRulesListener {
        public void onAcceptRulesHandler();
    }

    private WebView mWeb;
    private boolean mIsSingleButton = false;

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.rules_dialog, null));
        mWeb = (WebView) getDialogView().findViewById(R.id.rules_webview);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        if(mIsSingleButton == false) {
            getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
        } 
    }
    

    private void loadData() {
        mWeb.loadUrl("file:///android_asset/rules.html");
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.rules);
        loadData();
    }
    
    public void setSingleButtonMode(boolean isSingleButtonMode) {
        mIsSingleButton = isSingleButtonMode;
    }
    
    public static RulesDialog getInstance(Context context) {
        RulesDialog dialog = new RulesDialog();
        dialog.setContext(context);
        return dialog;
    }
}
