package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.utils.UfoMenuItem;

public class SocialNetDialog extends UfoDialogFragment {
    private class MenuItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            UfoMenuItem item = (UfoMenuItem) mMenuList.getAdapter().getItem(position);
            switch (item.getId()) {
                case R.string.google_plus:
                    gPlus();
                    break;
                case R.string.facebook_group:
                    facebook();
                    break;
                case R.string.vk_group:
                    vk();
                    break;
                case R.string.insta:
                    instagram();
                    break;
            }
        }

        private void instagram() {
            Uri uri = Uri.parse("http://instagram.com/_u/photohunt.online");
            Intent insta = new Intent(Intent.ACTION_VIEW, uri);
            insta.setPackage("com.instagram.android");

            if (isIntentAvailable(getContext(), insta)) {
                startActivity(insta);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/photohunt.online")));
            }

        }

        private boolean isIntentAvailable(Context ctx, Intent intent) {
            final PackageManager packageManager = ctx.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
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

        private void gotoUrl(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }

    }

    public static final String DIALOG_ID = "socialNetDialog";

    private ListView mMenuList;

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.social_network_dialog, null));
        mMenuList = (ListView) getDialogView().findViewById(R.id.feedback_menu);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
        mMenuList.setOnItemClickListener(new MenuItemClickListener());
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.in_social);
        initList();
    }

    private void initList() {
        Context context = getContext();
        MenuAdapter adapter = new MenuAdapter(context, R.layout.menu_item);
        UfoMenuItem gplus = new UfoMenuItem(context, R.drawable.gplus_24dp, R.string.google_plus);
        UfoMenuItem fb = new UfoMenuItem(context, R.drawable.fb_24dp, R.string.facebook_group);
        UfoMenuItem vk = new UfoMenuItem(context, R.drawable.vk_24dp, R.string.vk_group);
        UfoMenuItem insta = new UfoMenuItem(context, R.drawable.insta_24dp, R.string.insta);

        adapter.add(gplus);
        adapter.add(fb);
        adapter.add(vk);
        adapter.add(insta);

        mMenuList.setAdapter(adapter);
    }

    public static SocialNetDialog getInstance(Context context) {
        SocialNetDialog dialog = new SocialNetDialog();
        dialog.setContext(context);
        return dialog;
    }
}
