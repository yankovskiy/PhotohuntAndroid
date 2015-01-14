package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.utils.UfoMenuItem;

public class FeedbackDialog extends UfoDialogFragment {
    private class MenuItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            UfoMenuItem item = (UfoMenuItem) mMenuList.getAdapter().getItem(position);
            switch (item.getId()) {
            case R.string.send_mail:
                sendMail();
                break;
            case R.string.google_plus:
                gPlus();
                break;
            case R.string.facebook_group:
                facebook();
                break;
            case R.string.vk_group:
                vk();
                break;
            default:
                break;
            }
        }

        private void vk() {
            gotoUrl("http://vk.com/timswpho");
        }

        private void facebook() {
            gotoUrl("https://www.facebook.com/groups/793213797418220");
            
        }

        private void gPlus() {
            gotoUrl("https://plus.google.com/communities/112281523096923926274");
        }

        private void sendMail() {
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.setType("plain/text");
            mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.author_email) });
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            startActivity(Intent.createChooser(mailIntent, getString(R.string.choose_email_app)));
        }
        
        private void gotoUrl(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }

    }

    public static final String DIALOG_ID = "feedbackDialog";
    
    private ListView mMenuList;
    
    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.feedback_dialog, null));
        mMenuList = (ListView) getDialogView().findViewById(R.id.feedback_menu);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_ok, new CancelClickListener());
        mMenuList.setOnItemClickListener(new MenuItemClickListener());
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.feedback);
        initList();
    }
    
    private void initList() {
        Context context = getContext();
        MenuAdapter adapter = new MenuAdapter(context, R.layout.menu_item);
        UfoMenuItem gplus = new UfoMenuItem(context, R.drawable.gplus, R.string.google_plus);
        UfoMenuItem fb = new UfoMenuItem(context, R.drawable.fb, R.string.facebook_group);
        UfoMenuItem vk = new UfoMenuItem(context, R.drawable.vk, R.string.vk_group);
        UfoMenuItem email = new UfoMenuItem(context, R.drawable.ic_action_email, R.string.send_mail);
        
        adapter.add(gplus);
        adapter.add(fb);
        adapter.add(vk);
        adapter.add(email);
        
        mMenuList.setAdapter(adapter);
    }
    
    public static FeedbackDialog getInstance(Context context) {
        FeedbackDialog dialog = new FeedbackDialog();
        dialog.setContext(context);
        return dialog;
    }
}
