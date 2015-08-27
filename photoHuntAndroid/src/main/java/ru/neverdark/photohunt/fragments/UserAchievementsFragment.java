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
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.UserAchievementsAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Achievement;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент для отображения пользовательских достижений
 */
public class UserAchievementsFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private ListView mAchievementsList;
    private Parcelable mListState = null;
    private long mUserId;
    private String mDisplayName;
    private String mAvatar;

    public static UserAchievementsFragment getInstance(String displayName, String avatar, long userId) {
        UserAchievementsFragment fragment = new UserAchievementsFragment();
        fragment.mUserId = userId;
        fragment.mDisplayName = displayName;
        fragment.mAvatar = avatar;
        return fragment;
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
    public void onDestroyView() {
        Log.enter();
        mListState = mAchievementsList.onSaveInstanceState();
        ((MainActivity) getActivity()).getActionBarLayout(false);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.achievements_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        loadData();
        getActivity().setTitle(null);
        return mView;
    }

    private void loadData() {
        RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
        service.getAchievementsApi().getUserAchievements(mUserId, new GetUserAchievementsListener(mView));
    }

    private class AchievementsListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }

    private class GetUserAchievementsListener extends CallbackHandler<List<Achievement>> {
        public GetUserAchievementsListener(View view) {
            super(view, R.id.achievements_hide_when_loading, R.id.achievements_loading_progress);
        }

        @Override
        public void success(List<Achievement> achievements, Response response) {
            Log.enter();
            if (achievements != null) {
                UserAchievementsAdapter adapter = new UserAchievementsAdapter(mContext, achievements);
                mAchievementsList.setAdapter(adapter);
                if (mListState != null) {
                    mAchievementsList.onRestoreInstanceState(mListState);
                }
            }

            ((MainActivity) getActivity()).setActionBarData(mDisplayName, R.string.achievements, mAvatar, mUserId);
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
}
