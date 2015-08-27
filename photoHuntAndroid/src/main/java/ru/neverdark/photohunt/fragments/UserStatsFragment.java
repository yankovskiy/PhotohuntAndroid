package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.UserStatsAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Stats;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент распределения очков пользователя
 */
public class UserStatsFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;
    private String mDisplayName;
    private long mUserId;
    private ListView mStatsList;
    private String mAvatar;

    /**
     * Создание экземпляра класса
     *
     * @param userId      id пользователя по которому произвести запрос данных
     * @param displayName отображаемое имя пользователя
     * @param avatar      название аватар-файла
     * @return объект класса
     */
    public static UserStatsFragment getInstance(long userId, String displayName, String avatar) {
        UserStatsFragment fragment = new UserStatsFragment();
        fragment.mDisplayName = displayName;
        fragment.mUserId = userId;
        fragment.mAvatar = avatar;
        return fragment;
    }

    @Override
    public void bindObjects() {
        mStatsList = (ListView) mView.findViewById(R.id.users_stats_list);
    }

    @Override
    public void setListeners() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {

        mView = inflater.inflate(R.layout.user_stats_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(null);

        return mView;
    }

    /**
     * Загрузка данных с сервера
     */
    private void loadData() {
        String user = Settings.getUserId(mContext);
        String password = Settings.getPassword(mContext);

        RestService service = new RestService(user, password);
        service.getUserApi().getUserStats(mUserId, new GetUserStatsListener(mView));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsDataLoaded) {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).getActionBarLayout(false);
        super.onDestroyView();
    }

    private class GetUserStatsListener extends CallbackHandler<Stats> {
        public GetUserStatsListener(View view) {
            super(view, R.id.user_stats_hide_when_loading, R.id.user_stats_loading_progress);
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

                if (response.getStatus() == 403) {
                    RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                    throw new ToastException(err.error);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
            mIsDataLoaded = false;
        }

        @Override
        public void success(Stats data, Response response) {
            mIsDataLoaded = true;
            bindLoadedData(data);
            ((MainActivity) getActivity()).setActionBarData(mDisplayName, R.string.user_stats_title, mAvatar, mUserId);
            super.success(data, response);
        }

        /**
         * Маппинг загруженных данных в список
         */
        private void bindLoadedData(Stats data) {
            UserStatsAdapter adapter = new UserStatsAdapter(mContext, data);
            mStatsList.setAdapter(adapter);
        }
    }
}
