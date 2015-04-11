package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.RatingAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Rating;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class RatingFragment extends UfoFragment {

    private View mView;
    private Context mContext;
    private ListView mRatingTop10List;
    private ListView mRatingQuartList;
    private Parcelable mRatingTop10ListState = null;
    private Parcelable mRatingQuartListState = null;
    private boolean mIsLoaded = false;
    private TabHost mTabHost;
    private int mTabPosition;

    public static RatingFragment getInstance() {
        RatingFragment fragment = new RatingFragment();
        return fragment;
    }

    @Override
    public void onDestroyView() {
        mTabPosition = mTabHost.getCurrentTab();
        mRatingTop10ListState = mRatingTop10List.onSaveInstanceState();
        mRatingQuartListState = mRatingQuartList.onSaveInstanceState();
        super.onDestroyView();
    }

    @Override
    public void bindObjects() {
        mRatingTop10List = (ListView) mView.findViewById(R.id.rating_top10_tab);
        mRatingQuartList = (ListView) mView.findViewById(R.id.rating_quart_tab);
    }

    @Override
    public void setListeners() {
        mRatingTop10List.setOnItemClickListener(new RatingItemClickListener());
        mRatingQuartList.setOnItemClickListener(new RatingItemClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.rating_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.rating);
        initTabs();
        return mView;
    }

    private void initTabs() {
        mTabHost = (TabHost) mView.findViewById(R.id.rating_tabHost);
        mTabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = mTabHost.newTabSpec("quart");
        tabSpec.setIndicator(getString(R.string.quart));
        tabSpec.setContent(R.id.rating_quart_tab);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec("top10");
        tabSpec.setIndicator(getString(R.string.global));
        tabSpec.setContent(R.id.rating_top10_tab);
        mTabHost.addTab(tabSpec);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsLoaded) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService service = new RestService(user, pass);
            service.getUserApi().getRating(new GetRatingHandler(mView));
        }
    }

    private class GetRatingHandler extends CallbackHandler<Rating> {

        public GetRatingHandler(View view) {
            super(view, R.id.rating_hide_when_loading, R.id.rating_loading_progress);
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

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(Rating data, Response response) {
            Log.enter();
            if (data != null) {
                if (data.top10 != null) {
                    RatingAdapter top10adapter = new RatingAdapter(mContext, R.layout.rating_list_item, data.top10);
                    mRatingTop10List.setAdapter(top10adapter);
                    if (mRatingTop10ListState != null) {
                        mRatingTop10List.onRestoreInstanceState(mRatingTop10ListState);
                    }
                }

                if (data.quart != null) {
                    RatingAdapter quartadapter = new RatingAdapter(mContext, R.layout.rating_list_item, data.quart);
                    mRatingQuartList.setAdapter(quartadapter);

                    if (mRatingQuartListState != null) {
                        mRatingQuartList.onRestoreInstanceState(mRatingQuartListState);
                    }
                }

                if (mTabPosition != 0) {
                    mTabHost.setCurrentTab(mTabPosition);
                }
            } else {
                Common.showMessage(mContext, R.string.empty_rating);
            }

            super.success(data, response);
        }

    }

    private class RatingItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            ProfileFragment fragment = ProfileFragment.getInstance(id);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
