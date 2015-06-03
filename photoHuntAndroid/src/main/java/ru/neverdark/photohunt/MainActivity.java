package ru.neverdark.photohunt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import retrofit.RetrofitError;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.fragments.BriefContestFragment;
import ru.neverdark.photohunt.fragments.InformationFragment;
import ru.neverdark.photohunt.fragments.ProfileFragment;
import ru.neverdark.photohunt.fragments.RatingFragment;
import ru.neverdark.photohunt.fragments.ShopFragment;
import ru.neverdark.photohunt.fragments.StatsFragment;
import ru.neverdark.photohunt.fragments.ViewSingleImageFragment;
import ru.neverdark.photohunt.fragments.WelcomeFragment;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.User;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.SingletonHelper;
import ru.neverdark.photohunt.utils.UfoMenuItem;

/**
 * Главная активность приложения
 */
public class MainActivity extends UfoFragmentActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "59526129623";
    private Context mContext;
    private boolean mIsBackToContest;
    private GoogleCloudMessaging mGcm;
    private String mRegid;
    private ListView mLeftMenu;
    private ViewSingleImageFragment mSingleImageFragment;

    public void bindSingleImageFragment(ViewSingleImageFragment fragment) {
        mSingleImageFragment = fragment;
    }

    private void exitApp() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setCallback(new ExitAppListener());
        dialog.setMessage(R.string.exit_confirmation_message);
        dialog.show(getSupportFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    @Override
    public void bindObjects() {
        mContext = this;
        setDrawerLayout((DrawerLayout) findViewById(R.id.drawer_layout));
        mLeftMenu = (ListView) findViewById(R.id.left_menu);

        setDrawerToggle(new ActionBarDrawerToggle(this, getDrawerLayout(), R.string.drawer_open, R.string.drawer_close));
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
        UfoMenuItem infoItem = new UfoMenuItem(mContext, R.drawable.ic_info_outline_grey600_24dp, R.string.information);
        UfoMenuItem exitItem = new UfoMenuItem(mContext, R.drawable.ic_exit_to_app_grey600_24dp, R.string.exit);

        menuAdapter.add(contestItem);
        menuAdapter.add(statsItem);
        menuAdapter.add(shopItem);
        menuAdapter.add(ratingItem);
        menuAdapter.add(profileItem);
        menuAdapter.add(infoItem);
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
        Log.enter();
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
            if (checkPlayServices()) {
                mGcm = GoogleCloudMessaging.getInstance(this);
                mRegid = Settings.getRegistrationId(mContext);

                if (mRegid.isEmpty()) {
                    registerInBackground();
                }
            } else {
                Log.message("No valid Google Play Services APK found.");
            }

            Intent intent = getIntent();
            if (intent != null) {
                if (intent.getAction().equals(GcmIntentService.OPEN_PROFILE_ACTION)) {
                    fragment = ProfileFragment.getInstance(0L);
                    mIsBackToContest = true;
                }
            }

            if (fragment == null) {
                fragment = new BriefContestFragment();
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            fragment = new WelcomeFragment();
        }

        /*
         * workaround: при включении девайса и автостарте приложения иногда происходит наложение фрагментов
         * вызвано двойным вызовом OnCreate для activity & множественным добавлением фрагмента который уже есть.
         * Код ниже выполняет проверку, есть ли уже созданный фрагмент,
         */
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (frag == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_container, fragment).commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(new BackStackChangedListener());
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    public View getActionBarLayout(boolean enabled) {
        View view = null;
        getSupportActionBar().setCustomView(null);
        if (enabled) {
            getSupportActionBar().setCustomView(R.layout.custom_actionbar);
            view = getSupportActionBar().getCustomView();
        }

        return view;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                    }

                    mRegid = mGcm.register(SENDER_ID);

                    try {
                        sendRegistrationIdToServer();

                        // Persist the regID - no need to register again.
                        Settings.storeRegistrationId(mContext, mRegid);
                    } catch (RetrofitError e) {
                        Log.message(e.getMessage());
                    }
                } catch (IOException ex) {
                    Log.message(ex.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void data) {

            }
        }.execute(null, null, null);

    }

    /**
     * Отправка id зарегистрированного пользователя на сервер
     */
    private void sendRegistrationIdToServer() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);
        RestService service = new RestService(user, pass);
        User data = new User();
        data.regid = mRegid;
        data.client_version = SingletonHelper.getInstance().getVersion();
        service.getUserApi().updateUser(user, data);
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (visible) {
            getSupportActionBar().setCustomView(R.layout.progress);
            ProgressBar progressBar = (ProgressBar) getSupportActionBar().getCustomView();
            progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        } else {
            getSupportActionBar().setCustomView(null);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.message("This device is not supported");
            }

            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
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
        } else if (mSingleImageFragment != null) {
            mSingleImageFragment.backPressed();
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
                } else if (mSingleImageFragment != null) {
                    mSingleImageFragment.backPressed();
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
                case R.string.information:
                    mIsBackToContest = true;
                    fragment = InformationFragment.getInstance();
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
