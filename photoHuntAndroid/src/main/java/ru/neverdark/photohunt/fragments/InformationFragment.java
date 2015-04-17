package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Locale;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.InformationAdapter;
import ru.neverdark.photohunt.dialogs.HtmlDialog;
import ru.neverdark.photohunt.dialogs.SocialNetDialog;

public class InformationFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private ListView mList;

    public static InformationFragment getInstance() {
        return new InformationFragment();
    }

    @Override
    public void bindObjects() {
        mList = (ListView) mView.findViewById(R.id.information_list);
    }

    @Override
    public void setListeners() {
        mList.setOnItemClickListener(new ItemClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.information_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.information);
        initList();
        return mView;
    }

    private void initList() {
        InformationAdapter adapter = new InformationAdapter(mContext);

        try {
            String appName = getString(R.string.app_name);
            String version = String.format(Locale.US, "%s: %s",
                    getString(R.string.version),
                    mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
            adapter.add(new InformationAdapter.Record(appName, version, R.string.app_name));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        adapter.add(new InformationAdapter.Record(getString(R.string.in_social), null, R.string.in_social));
        adapter.add(new InformationAdapter.Record(getString(R.string.rules), null, R.string.rules));
        adapter.add(new InformationAdapter.Record(getString(R.string.last_changes), null, R.string.last_changes));
        adapter.add(new InformationAdapter.Record(getString(R.string.rate), getString(R.string.rate_on_market), R.string.rate));
        adapter.add(new InformationAdapter.Record(getString(R.string.feedback), getString(R.string.send_mail), R.string.feedback));
        adapter.add(new InformationAdapter.Record(getString(R.string.licenses), null, R.string.licenses));
        mList.setAdapter(adapter);
    }

    private class ItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch ((int) id) {
                case R.string.app_name:
                    showHtmlDialog("about.html", R.string.app_name);
                    break;
                case R.string.in_social:
                    showSocialDialog();
                    break;
                case R.string.rules:
                    showHtmlDialog("rules.html", R.string.rules);
                    break;
                case R.string.feedback:
                    sendMail();
                    break;
                case R.string.rate:
                    gotoMarket();
                    break;
                case R.string.licenses:
                    showHtmlDialog("licenses.html", R.string.licenses);
                    break;
                case R.string.last_changes:
                    showHtmlDialog("last_changes.html", R.string.last_changes);
                    break;
            }
        }

        private void sendMail() {
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.setType("plain/text");
            mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.author_email)});
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            startActivity(Intent.createChooser(mailIntent, getString(R.string.choose_email_app)));
        }

        private void showSocialDialog() {
            SocialNetDialog dialog = SocialNetDialog.getInstance(mContext);
            dialog.show(getFragmentManager(), SocialNetDialog.DIALOG_ID);
        }

        private void showHtmlDialog(String assetsName, int titleResId) {
            HtmlDialog dialog = HtmlDialog.getInstance(mContext, assetsName, titleResId);
            dialog.setSingleButtonMode(true);
            dialog.show(getFragmentManager(), HtmlDialog.DIALOG_ID);
        }

        private void gotoMarket() {
            String url = "market://details?id=ru.neverdark.photohunt";
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse(url));
            startActivity(marketIntent);
        }
    }
}
