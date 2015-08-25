package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.AchievementsAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Achievement;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент с достижениями
 */
public class AchievementsFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private ListView mAchievementsList;
    private Parcelable mListState = null;

    public static AchievementsFragment getInstance() {
        return new AchievementsFragment();
    }

    @Override
    public void bindObjects() {
        mAchievementsList = (ListView) mView.findViewById(R.id.achievements_list);
    }

    @Override
    public void setListeners() {
        mAchievementsList.setOnItemClickListener(new AchievementsListClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.achievements_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        loadData();
        getActivity().setTitle(R.string.achievements);
        return mView;
    }

    private void loadData() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getAchievementsApi().getAchievements(new GetAchievementsListener(mView));
    }

    @Override
    public void onDestroyView() {
        Log.enter();
        mListState = mAchievementsList.onSaveInstanceState();
        super.onDestroyView();
    }

    private void openAchievementsUsersFragment(String badge) {
        AchievementsUsersFragment fragment = AchievementsUsersFragment.getInstance(badge);
        Common.openFragment(this, fragment, true);
    }

    private class GetAchievementsListener extends CallbackHandler<List<Achievement>> {
        public GetAchievementsListener(View view) {
            super(view, R.id.achievements_hide_when_loading, R.id.achievements_loading_progress);
        }

        @Override
        public void success(List<Achievement> achievements, Response response) {
            Log.enter();
            if (achievements != null) {
                AchievementsAdapter adapter = new AchievementsAdapter(mContext, achievements);
                mAchievementsList.setAdapter(adapter);
                if (mListState != null) {
                    mAchievementsList.onRestoreInstanceState(mListState);
                }
            }

            super.success(achievements, response);
        }

        @Override
        public void failure(RetrofitError error) {
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
                if (err != null) {
                    throw new ToastException(err.error);
                } else {
                    throw new ToastException((R.string.error_unexpected_error));
                }

            } catch (ToastException e) {
                e.show(mContext);
            }

            super.failure(error);
        }
    }

    private class AchievementsListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Achievement achievement = (Achievement) parent.getAdapter().getItem(position);
            openAchievementsUsersFragment(achievement.service_name);
        }
    }
}
