package ru.neverdark.photohunt.dialogs;

import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService.User;

public class UserInfoDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "userInfoDialog";
    private User mUserInfo;
    private TextView mDisplayName;
    private TextView mBalance;
    
    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.user_info_dialog, null));
        mDisplayName = (TextView) getDialogView().findViewById(R.id.user_info_display_name);
        mBalance = (TextView) getDialogView().findViewById(R.id.user_info_balance);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }
    
    public static UserInfoDialog getInstance(Context context) {
        UserInfoDialog dialog = new UserInfoDialog();
        dialog.setContext(context);
        return dialog;
    }
    
    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.user_information);
        prepareDataForShow();
    }
    
    private void prepareDataForShow() {
        String user = String.format(Locale.US, "%s: %s", getString(R.string.displayname), mUserInfo.display_name);
        String balance = String.format(Locale.US, "%s: %d", getString(R.string.rating_count), mUserInfo.balance);
        mDisplayName.setText(user);
        mBalance.setText(balance);
    }

    public void setUserInfo(User userInfo) {
        mUserInfo = userInfo;
    }
    

}
