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
import ru.neverdark.photohunt.adapters.AchievementsUsersAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.AchievementUser;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class AchievementsUsersFragment extends UfoFragment {
    private String mBadge;
    private ListView mUserList;
    private Parcelable mListState = null;
    private View mView;
    private Context mContext;

    public static AchievementsUsersFragment getInstance(String badge) {
        AchievementsUsersFragment fragment = new AchievementsUsersFragment();
        fragment.mBadge = badge;
        return fragment;
    }

    @Override
    public void bindObjects() {
        mUserList = (ListView) mView.findViewById(R.id.achievements_users_list);
    }

    @Override
    public void setListeners() {
        mUserList.setOnItemClickListener(new UserListClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        Log.enter();
        mView = inflater.inflate(R.layout.achievements_users_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        loadData();
        getActivity().setTitle(R.string.achievement_gets);
        return mView;
    }

    private void loadData() {
        RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
        service.getAchievementsApi().getAchievementUserList(mBadge, new GetAchievementUserListListener(mView));
    }

    @Override
    public void onDestroyView() {
        Log.enter();
        mListState = mUserList.onSaveInstanceState();
        super.onDestroyView();
    }

    private void openProfile(long id) {
        ProfileFragment fragment = ProfileFragment.getInstance(id);
        Common.openFragment(this, fragment, true);
    }

    private class GetAchievementUserListListener extends CallbackHandler<List<AchievementUser>> {
        public GetAchievementUserListListener(View view) {
            super(view, R.id.achievements_users_hide_when_loading, R.id.achievements_users_loading_progress);
        }

        @Override
        public void success(List<AchievementUser> achievementUsers, Response response) {
            if (achievementUsers != null) {
                AchievementsUsersAdapter adapter = new AchievementsUsersAdapter(mContext, achievementUsers);
                mUserList.setAdapter(adapter);

                if (mListState != null) {
                    mUserList.onRestoreInstanceState(mListState);
                }
            }

            super.success(achievementUsers, response);
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

    private class UserListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openProfile(id);
        }
    }
}
