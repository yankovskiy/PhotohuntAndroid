package ru.neverdark.photohunt.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.StatsAdapter;
import ru.neverdark.photohunt.adapters.StatsAdapter.ChildRecord;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.Contest;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;
import ru.neverdark.abs.UfoFragment;

/**
 * Фрагмент содержащий статистику
 */
public class StatsFragment extends UfoFragment {
    private class ContestClickHandler implements OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id) {
            DetailContestFragment fragment = new DetailContestFragment(id);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
            return false;
        }

    }

    private class GetContestsHandler extends CallbackHandler<List<RestService.Contest>> {

        public GetContestsHandler(View view) {
            super(view);
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
            int monthIndex = Integer.valueOf(dateParts[1]);
            String month = getResources().getStringArray(R.array.months)[monthIndex - 1];

            return String.format("%s %s", month, year);
        }

        @Override
        public void success(List<RestService.Contest> data, Response response) {
            Log.enter();
            final List<String> headers = new ArrayList<String>();
            final Map<String, List<ChildRecord>> childs = new HashMap<String, List<ChildRecord>>();

            for (Contest contest : data) {
                String date = formatDate(contest.close_date);
                Log.variable("date", date);
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

                Log.variable("Author", contest.display_name);
                Log.variable("Id", String.valueOf(contest.id));
                Log.variable("Subject", contest.subject);
                Log.variable("Close date", contest.close_date);
                Log.variable("Reward", String.valueOf(contest.rewards));
                Log.variable("Works", String.valueOf(contest.works));
                Log.variable("Status", String.valueOf(contest.status));

                List<ChildRecord> records = childs.get(date);
                if (records == null) {
                    Log.message("Create new");
                    records = new ArrayList<ChildRecord>();
                    records.add(record);
                    childs.put(date, records);
                } else {
                    Log.message("Use exists");
                    records.add(record);
                }
            }

            Log.variable("headers size", String.valueOf(headers.size()));
            Log.variable("child size", String.valueOf(childs.size()));
            mAdapter = new StatsAdapter(mContext, headers, childs);
            mContestList.setAdapter(mAdapter);

            super.success(data, response);
        }

    }

    private StatsAdapter mAdapter;

    private boolean mIsDataLoaded;
    private Context mContext;
    private ExpandableListView mContestList;
    private View mView;

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
        mIsDataLoaded = false;
        mView = inflater.inflate(R.layout.stats_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsDataLoaded == false) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            RestService service = new RestService(user, pass);
            service.getContestApi().getContests(
                    new GetContestsHandler(mView));
        }
    }

    @Override
    public void setListeners() {
        mContestList.setOnChildClickListener(new ContestClickHandler());
    }
}
