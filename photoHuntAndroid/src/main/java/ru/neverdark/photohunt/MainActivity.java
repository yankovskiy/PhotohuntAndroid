package ru.neverdark.photohunt;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.dialogs.FeedbackDialog;
import ru.neverdark.photohunt.dialogs.RulesDialog;
import ru.neverdark.photohunt.fragments.BriefContestFragment;
import ru.neverdark.photohunt.fragments.ProfileFragment;
import ru.neverdark.photohunt.fragments.RatingFragment;
import ru.neverdark.photohunt.fragments.StatsFragment;
import ru.neverdark.photohunt.fragments.WelcomeFragment;
import ru.neverdark.photohunt.utils.UfoMenuItem;
import ru.neverdark.photohunt.utils.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Главная активность приложения
 */
public class MainActivity extends UfoFragmentActivity {
    private Context mContext;
    /**
     * Обработчик кликов по выдвигающимуся меню
     */
    private class LeftMenuItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            mLeftMenu.setItemChecked(position, true);
            UfoMenuItem item = (UfoMenuItem) mLeftMenu.getAdapter().getItem(position);
            UfoFragment fragment = null;

            switch (item.getId()) {
            case R.string.contest:
                fragment = new BriefContestFragment();
                break;
            case R.string.stats:
                fragment = new StatsFragment();
                break;
            case R.string.profile:
                fragment = new ProfileFragment();
                break;
            case R.string.rating:
                fragment = new RatingFragment();
                break;
            case R.string.rules:
                showRules();
                break;
            case R.string.rate:
                gotoMarket();
                break;
            case R.string.feedback:
                showFeedback();
                break;
            }

            mDrawerLayout.closeDrawer(mLeftMenu);

            if (fragment != null) {
                setTitle(item.getMenuLabel());
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
            } 
        }

        private void showFeedback() {
            FeedbackDialog dialog = FeedbackDialog.getInstance(mContext);
            dialog.show(getSupportFragmentManager(), FeedbackDialog.DIALOG_ID);
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

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mLeftMenu;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    @Override
    public void bindObjects() {
        mContext = this;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftMenu = (ListView) findViewById(R.id.left_menu);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.drawable.ic_drawer);/* {
            
        }
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            @Override
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };*/

    }

    /**
     * Создание элементов выдвигающегося меню
     */
    private void createLeftMenu() {
        MenuAdapter menuAdapter = new MenuAdapter(mContext, R.layout.menu_item);
        
        UfoMenuItem contestItem = new UfoMenuItem(mContext, R.drawable.ic_contest, R.string.contest);
        UfoMenuItem statsItem = new UfoMenuItem(mContext, R.drawable.ic_stats, R.string.stats);
        UfoMenuItem profileItem = new UfoMenuItem(mContext, R.drawable.ic_action_person, R.string.profile);
        UfoMenuItem ratingItem = new UfoMenuItem(mContext, R.drawable.ic_action_group, R.string.rating);
        UfoMenuItem aboutItem = new UfoMenuItem(mContext, R.drawable.ic_action_about, R.string.rules);
        UfoMenuItem rateItem = new UfoMenuItem(mContext, R.drawable.ic_action_good, R.string.rate);
        UfoMenuItem feedbackItem = new UfoMenuItem(mContext, R.drawable.ic_action_email, R.string.feedback);
        
        menuAdapter.add(contestItem);
        menuAdapter.add(statsItem);
        menuAdapter.add(profileItem);
        menuAdapter.add(ratingItem);
        menuAdapter.add(aboutItem);
        menuAdapter.add(rateItem);
        menuAdapter.add(feedbackItem);

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

        mTitle = mDrawerTitle = getTitle();
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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:

            if (mDrawerLayout.isDrawerOpen(mLeftMenu)) {
                mDrawerLayout.closeDrawer(mLeftMenu);
            } else {
                mDrawerLayout.openDrawer(mLeftMenu);
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setListeners() {
        mLeftMenu.setOnItemClickListener(new LeftMenuItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
}
