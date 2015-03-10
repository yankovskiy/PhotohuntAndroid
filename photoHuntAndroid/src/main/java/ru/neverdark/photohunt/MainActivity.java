package ru.neverdark.photohunt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.RulesDialog;
import ru.neverdark.photohunt.dialogs.SocialNetDialog;
import ru.neverdark.photohunt.fragments.BriefContestFragment;
import ru.neverdark.photohunt.fragments.ProfileFragment;
import ru.neverdark.photohunt.fragments.RatingFragment;
import ru.neverdark.photohunt.fragments.ShopFragment;
import ru.neverdark.photohunt.fragments.StatsFragment;
import ru.neverdark.photohunt.fragments.WelcomeFragment;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.SingletonHelper;
import ru.neverdark.photohunt.utils.UfoMenuItem;

/**
 * Главная активность приложения
 */
public class MainActivity extends UfoFragmentActivity {
    private Context mContext;
    private boolean mIsBackToContest;

    /**
     * Обработчик кликов по выдвигающимуся меню
     */
    private class LeftMenuItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            UfoFragment fragment = null;

            mIsBackToContest = false;

            int itemId = (int) id;
            switch (itemId) {
                case R.string.contests:
                    fragment = new BriefContestFragment();
                    break;
                case R.string.stats:
                    mIsBackToContest = true;
                    fragment = StatsFragment.getInstance(0L, null);
                    break;
                case R.string.shop:
                    mIsBackToContest = true;
                    fragment = new ShopFragment();
                    break;
                case R.string.profile:
                    mIsBackToContest = true;
                    fragment = ProfileFragment.getInstance(0L);
                    break;
                case R.string.rating:
                    mIsBackToContest = true;
                    fragment = new RatingFragment();
                    break;
                case R.string.rules:
                    showRules();
                    break;
                case R.string.rate:
                    gotoMarket();
                    break;
                case R.string.in_social:
                    showSocialDialog();
                    break;
                case R.string.feedback:
                    sendMail();
                    break;
                case R.string.exit:
                    exitApp();
                    break;
            }

            getDrawerLayout().closeDrawer(mLeftMenu);

            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction trans = fragmentManager.beginTransaction();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                }
                trans.replace(R.id.main_container, fragment);
                trans.commit();
                fragmentManager.executePendingTransactions();
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
            dialog.show(getSupportFragmentManager(), SocialNetDialog.DIALOG_ID);
        }

        private void showRules() {
            RulesDialog dialog = RulesDialog.getInstance(mContext);
            dialog.setSingleButtonMode(true);
            dialog.show(getSupportFragmentManager(), RulesDialog.DIALOG_ID);
        }

        private void gotoMarket() {
            String url = "market://details?id=ru.neverdark.photohunt";
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse(url));
            startActivity(marketIntent);
        }
    }

    private void exitApp() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setCallback(new ExitAppListener());
        dialog.setMessage(R.string.exit_confirmation_message);
        dialog.show(getSupportFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    private ListView mLeftMenu;

    @Override
    public void bindObjects() {
        mContext = this;
        setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout));
        mLeftMenu = (ListView) findViewById(R.id.left_menu);

        setDrawerToggle(new ActionBarDrawerToggle(this, getDrawerLayout(), R.drawable.ic_drawer, R.drawable.ic_drawer));
    }

    /**
     * Создание элементов выдвигающегося меню
     */
    private void createLeftMenu() {
        MenuAdapter menuAdapter = new MenuAdapter(mContext, R.layout.menu_item);

        UfoMenuItem contestItem = new UfoMenuItem(mContext, R.drawable.ic_whatshot_grey600_24dp, R.string.contests);
        UfoMenuItem statsItem = new UfoMenuItem(mContext, R.drawable.ic_poll_grey600_24dp, R.string.stats);
        UfoMenuItem shopItem = new UfoMenuItem(mContext, R.drawable.ic_shopping_cart_grey600_24dp, R.string.shop);
        UfoMenuItem profileItem = new UfoMenuItem(mContext, R.drawable.ic_assignment_ind_grey600_24dp, R.string.profile);
        UfoMenuItem ratingItem = new UfoMenuItem(mContext, R.drawable.ic_group_grey600_24dp, R.string.rating);
        UfoMenuItem aboutItem = new UfoMenuItem(mContext, R.drawable.ic_info_outline_grey600_24dp, R.string.rules);
        UfoMenuItem rateItem = new UfoMenuItem(mContext, R.drawable.ic_thumb_up_grey600_24dp, R.string.rate);
        UfoMenuItem feedbackItem = new UfoMenuItem(mContext, R.drawable.ic_email_grey600_24dp, R.string.feedback);
        UfoMenuItem socialItem = new UfoMenuItem(mContext, R.drawable.ic_group_work_grey600_24dp, R.string.in_social);
        UfoMenuItem exitItem = new UfoMenuItem(mContext, R.drawable.ic_exit_to_app_grey600_24dp, R.string.exit);

        menuAdapter.add(contestItem);
        menuAdapter.add(statsItem);
        menuAdapter.add(shopItem);
        menuAdapter.add(ratingItem);
        menuAdapter.add(profileItem);
        menuAdapter.add(socialItem);
        menuAdapter.add(aboutItem);
        menuAdapter.add(rateItem);
        menuAdapter.add(feedbackItem);
        menuAdapter.add(exitItem);

        mLeftMenu.setAdapter(menuAdapter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            SingletonHelper.getInstance().setVersion(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.message("Could not determine version use");
        }

        bindObjects();
        setListeners();
        createLeftMenu();

        UfoFragment fragment = null;

        if (Settings.isLogin(mContext)) {
            fragment = new BriefContestFragment();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            fragment = new WelcomeFragment();
        }

        getSupportFragmentManager().beginTransaction().add(R.id.main_container, fragment).commit();
        getSupportFragmentManager().addOnBackStackChangedListener(new BackStackChangedListener());
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.progress);
        ProgressBar progressBar = (ProgressBar) getSupportActionBar().getCustomView();
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        getSupportActionBar().getCustomView().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDrawerToggle().onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (getDrawerLayout().isDrawerOpen(mLeftMenu)) {
            getDrawerLayout().closeDrawer(mLeftMenu);
        } else if (mIsBackToContest) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() == 0) {
                BriefContestFragment fragment = new BriefContestFragment();
                setTitle(R.string.contests);
                fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
                mIsBackToContest = false;
            } else {
                fragmentManager.popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getDrawerToggle().isDrawerIndicatorEnabled()) {
                    if (getDrawerLayout().isDrawerOpen(mLeftMenu)) {
                        getDrawerLayout().closeDrawer(mLeftMenu);
                    } else {
                        getDrawerLayout().openDrawer(mLeftMenu);
                    }
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setListeners() {
        mLeftMenu.setOnItemClickListener(new LeftMenuItemClickListener());
        getDrawerLayout().setDrawerListener(getDrawerToggle());
    }

    public void resetBackButtonToDefault() {
        mIsBackToContest = false;
    }

    public void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            Log.enter();
            hideKeyboard();

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getDrawerToggle().setDrawerIndicatorEnabled(false);
            } else {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                if ((fragment instanceof WelcomeFragment) == false) {
                    getDrawerToggle().setDrawerIndicatorEnabled(true);
                }
            }
        }
    }

    private class ExitAppListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler() {
            finish();
        }
    }
}
