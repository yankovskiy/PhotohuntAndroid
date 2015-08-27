package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.StatsAdapter;
import ru.neverdark.photohunt.adapters.StatsAdapter.ChildRecord;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент содержащий статистику
 */
public class StatsFragment extends UfoFragment {
    private StatsAdapter mAdapter;
    private boolean mIsDataLoaded;
    private Context mContext;
    private ExpandableListView mContestList;
    private Parcelable mContestListState = null;
    private View mView;
    private long mUserId;
    private String mDisplayName;
    private int mListPosition;
    private int mItemPosition;
    private String mAvatar;

    public static StatsFragment getInstance() {
        StatsFragment fragment = new StatsFragment();
        fragment.mUserId = 0L;
        return fragment;
    }

    public static StatsFragment getInstance(long userId, String displayName, String avatar) {
        StatsFragment fragment = new StatsFragment();
        fragment.mUserId = userId;
        fragment.mDisplayName = displayName;
        fragment.mAvatar = avatar;
        return fragment;
    }

    @Override
    public void bindObjects() {
        mContestList = (ExpandableListView) mView.findViewById(R.id.stats_contestList);
    }

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
        mView = inflater.inflate(R.layout.stats_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        if (mUserId == 0L) {
            getActivity().setTitle(R.string.stats);
        } else {
            getActivity().setTitle(R.string.wins_list);
            ((MainActivity) getActivity()).setActionBarData(mDisplayName, R.string.wins_list, mAvatar, mUserId);
        }
        return mView;
    }

    @Override
    public void onResume() {
        Log.enter();
        super.onResume();
        if (!mIsDataLoaded) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            RestService service = new RestService(user, pass);
            if (mUserId == 0L) {
                service.getContestApi().getContests(
                        new GetContestsHandler(mView));
            } else {
                service.getUserApi().getWinsList(mUserId, new GetContestsHandler(mView));
            }
        }
    }

    @Override
    public void onDetach() {
        if (mUserId != 0L) {
            ((MainActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.enter();
        mContestListState = mContestList.onSaveInstanceState();
        mListPosition = mContestList.getFirstVisiblePosition();
        View itemView = mContestList.getChildAt(0);
        mItemPosition = itemView == null ? 0 : itemView.getTop();
        ((MainActivity) getActivity()).getActionBarLayout(false);
        super.onDestroyView();
    }

    @Override
    public void setListeners() {
        mContestList.setOnChildClickListener(new ContestClickHandler());
    }

    private class ContestClickHandler implements OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                    int childPosition, long id) {
            DetailContestFragment fragment = DetailContestFragment.getInstance(id);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return false;
        }

    }

    private class GetContestsHandler extends CallbackHandler<List<Contest>> {

        public GetContestsHandler(View view) {
            super(view, R.id.stats_hide_when_loading, R.id.stats_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.enter();
            super.failure(error);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        private String formatDate(String date) {
            String[] dateParts = date.split("-");
            String year = dateParts[0];
            String month = null;
            int monthIndex = Integer.valueOf(dateParts[1]);
            if (isAdded()) {
                month = getResources().getStringArray(R.array.months)[monthIndex - 1];
            }

            return String.format("%s %s", month, year);
        }

        @Override
        public void success(List<Contest> data, Response response) {
            Log.enter();
            if (data != null) {
                final List<String> headers = new ArrayList<>();
                final Map<String, List<ChildRecord>> childs = new HashMap<>();

                for (Contest contest : data) {
                    String date = formatDate(contest.close_date);
                    if (headers.indexOf(date) == -1) {
                        headers.add(date);
                    }

                    ChildRecord record = new ChildRecord();
                    record.setAuthor(contest.display_name);
                    record.setId(contest.id);
                    record.setSubject(contest.subject);
                    record.setCloseDate(contest.close_date);
                    record.setReward(contest.rewards);
                    record.setWorks(contest.works);
                    record.setStatus(contest.status);

                    List<ChildRecord> records = childs.get(date);
                    if (records == null) {
                        records = new ArrayList<ChildRecord>();
                        records.add(record);
                        childs.put(date, records);
                    } else {
                        records.add(record);
                    }
                }

                mAdapter = new StatsAdapter(mContext, headers, childs);
                mContestList.setAdapter(mAdapter);
                mIsDataLoaded = true;

                if (mContestListState != null) {
                    Log.message("not null");
                    mContestList.onRestoreInstanceState(mContestListState);
                    mContestList.setSelectionFromTop(mListPosition, mItemPosition);
                } else {
                    Log.message("null");
                    mContestList.expandGroup(0);    // expand top group
                }
            }
            super.success(data, response);
        }

    }
}
