package ru.neverdark.photohunt.fragments;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.BriefContestAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.Contest;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;
import ru.neverdark.abs.UfoFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public class BriefContestFragment extends UfoFragment {
    private Context mContext;
    private View mView;
    private boolean mIsDataLoaded;
    private ListView mContestList;

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
        mView = inflater.inflate(R.layout.brief_contest_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        return mView;
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
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);

            RestService service = new RestService(user, pass);
            service.getContestApi().getOpenContests(new GetOpenContestsHandler(mView));
        }
    }

    private class GetOpenContestsHandler extends CallbackHandler<List<Contest>> {

        public GetOpenContestsHandler(View view) {
            super(view);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                if (response.getStatus() == 404) {
                    throw new ToastException(R.string.error_contest_not_found);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(List<Contest> data, Response response) {
            if (data != null) {
                BriefContestAdapter adapter = new BriefContestAdapter(mContext, data);
                adapter.setCallback(new EnterToContestListener());
                mContestList.setAdapter(adapter);
                mIsDataLoaded = true;
            }

            super.success(data, response);
        }
    }

    private class EnterToContestListener implements BriefContestAdapter.OnEnterToContest {
        @Override
        public void enterToContest(long contestId) {
            DetailContestFragment fragment = new DetailContestFragment(contestId);
            ActionBarDrawerToggle toggle = ((UfoFragmentActivity) getActivity()).getDrawerToggle();
            fragment.setDrawerToggle(toggle);
            fragment.setChangeNavi(true);
            fragment.setBackHandle(true);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
