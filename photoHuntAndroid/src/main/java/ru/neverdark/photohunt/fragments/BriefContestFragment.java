package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.BriefContestAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.NewContestDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.OpenContests;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class BriefContestFragment extends UfoFragment {
    private Context mContext;
    private View mView;
    private boolean mIsDataLoaded;
    private ListView mContestList;
    private Contest mSelectedContest;
    private Parcelable mContestListState = null;
    private OpenContests mData;

    /*
     * (non-Javadoc)
     *
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mIsDataLoaded = false;
        mView = inflater.inflate(R.layout.brief_contest_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.contests);
        registerForContextMenu(mContestList);
        return mView;
    }

    @Override
    public void onDestroyView() {
        mContestListState = mContestList.onSaveInstanceState();
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        Log.enter();
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.contest_card, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.enter();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Contest contest;

        // если выбрано по троеточию, то info будет Null
        if (info == null) {
            contest = mSelectedContest;
        } else {
            contest = (Contest) mContestList.getAdapter().getItem(info.position);
        }

        switch (item.getItemId()) {
            case R.id.view_profile:
                showUserProfile(contest.user_id);
                return true;
            case R.id.view_prev_contest:
                if (contest.prev_id != 0L) {
                    showContest(contest.prev_id);
                } else {
                    Common.showMessage(mContext, R.string.error_contest_not_found);
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showContest(long contestId) {
        DetailContestFragment fragment = DetailContestFragment.getInstance(contestId);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showUserProfile(long userId) {
        ProfileFragment fragment = ProfileFragment.getInstance(userId);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void bindObjects() {
        mContestList = (ListView) mView.findViewById(R.id.brief_contest_list);
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            loadData();
        }
    }

    private void loadData() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getContestApi().getOpenContests(new GetOpenContestsHandler(mView));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.brief_contest, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.brief_contest_new:
                createNewContest();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNewContest() {
        if (mData.is_can_create) {
            NewContestDialog dialog = NewContestDialog.getInstance(mContext);
            dialog.setCallback(new NewContestListener());
            dialog.show(getFragmentManager(), NewContestDialog.DIALOG_ID);
        } else {
            showAttentionDialog();
        }
    }

    private void showAttentionDialog() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessages(R.string.goto_shop_title, R.string.goto_shop_message_contest);
        dialog.setCallback(new GotoShopListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    private void openShop() {
        Common.openFragment(this, ShopFragment.getInstance(), true);
    }

    private class GetOpenContestsHandler extends CallbackHandler<OpenContests> {

        public GetOpenContestsHandler(View view) {
            super(view, R.id.brief_hide_when_loading, R.id.brief_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            Response response = error.getResponse();
            try {
                setHasOptionsMenu(false);

                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                throw new ToastException(err.error);

            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(OpenContests data, Response response) {
            if (data != null) {
                mData = data;
                BriefContestAdapter adapter = new BriefContestAdapter(mContext, data.contests);
                adapter.setCallback(new EnterToContestListener());
                mContestList.setAdapter(adapter);

                if (mContestListState != null) {
                    mContestList.onRestoreInstanceState(mContestListState);
                }
                mIsDataLoaded = true;
                setHasOptionsMenu(true);
            }

            super.success(data, response);
        }
    }

    private class EnterToContestListener implements BriefContestAdapter.OnBriefCardClickListener {
        @Override
        public void enterToContest(long contestId) {
            showContest(contestId);
        }

        @Override
        public void onMoreButton(Contest contest) {
            mSelectedContest = contest;
            getActivity().openContextMenu(mContestList);
        }
    }

    private class GotoShopListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler() {
            openShop();
        }
    }

    private class NewContestListener implements OnCallback, NewContestDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler(Contest contest) {
            RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
            setHasOptionsMenu(false);
            ((UfoFragmentActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);
            service.getContestApi().createNewContest(contest, new CreateNewContestListener());
        }

        private class CreateNewContestListener implements Callback<Void> {
            @Override
            public void success(Void data, Response response) {
                ((UfoFragmentActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);
                loadData();
            }

            @Override
            public void failure(RetrofitError error) {
                Response response = error.getResponse();
                try {
                    setHasOptionsMenu(false);
                    ((UfoFragmentActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);
                    if (response == null) {
                        throw new ToastException(R.string.error_network_problem);
                    }

                    if (response.getStatus() == 401) {
                        throw new ToastException(R.string.error_wrong_password);
                    }

                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    throw new ToastException(err.error);

                } catch (ToastException e) {
                    e.show(mContext);
                }
            }
        }
    }
}
