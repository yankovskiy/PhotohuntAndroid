package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.Item;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.FavoriteUserItem;
import ru.neverdark.photohunt.adapters.HeaderItem;
import ru.neverdark.photohunt.adapters.HeadersArrayAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.FavoriteUser;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент избранных пользователей
 */
public class FavoritesUsersFragment extends UfoFragment {


    public final static int ACTION_SEND_MESSAGE = 1;
    public final static int ACTION_OPEN_PROFILE = 2;
    public final static String TAG = "fav";

    private View mView;
    private int mAction;
    private long mUserId;
    private Context mContext;
    private ListView mFavoritesList;
    private Parcelable mFavoritesListState = null;
    private boolean mIsDataLoaded;

    public static FavoritesUsersFragment getInstance(long userId, int action) {
        FavoritesUsersFragment fragment = new FavoritesUsersFragment();
        fragment.mUserId = userId;
        fragment.mAction = action;
        return fragment;
    }

    @Override
    public void onDestroyView() {
        mFavoritesListState = mFavoritesList.onSaveInstanceState();
        super.onDestroyView();
    }

    @Override
    public void bindObjects() {
        mFavoritesList = (ListView) mView.findViewById(R.id.favorites_users_list);
    }

    @Override
    public void setListeners() {
        mFavoritesList.setOnItemClickListener(new FavoritesListClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {

        mView = inflater.inflate(R.layout.favorites_users_fragment, container, false);
        mContext = mView.getContext();

        bindObjects();
        setListeners();
        int resId;
        if (mAction == ACTION_OPEN_PROFILE) {
            resId = R.string.favorite_users;
        } else {
            resId = R.string.new_message;
        }

        getActivity().setTitle(resId);
        mIsDataLoaded = false;
        return mView;
    }

    /**
     * Открывает указанный фрагмент
     *
     * @param fragment    фрагмент для открытия
     * @param isBackStack true если фрагмент необходимо поместить в стек
     */
    private void openFragment(Fragment fragment, boolean isBackStack) {
        Common.openFragment(this, fragment, isBackStack);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsDataLoaded) {
            loadData();
        }
    }

    /**
     * Загрузка данных с сервера
     */
    private void loadData() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getUserApi().getFavoritesUsers(new GetFavoritesUsersListener(mView));
    }

    /**
     * Обработка кликов по элементам списка
     */
    private class FavoritesListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (mAction) {
                case ACTION_OPEN_PROFILE:
                    openUserProfile(id);
                    break;
                case ACTION_SEND_MESSAGE:
                    HeadersArrayAdapter adapter = (HeadersArrayAdapter) mFavoritesList.getAdapter();
                    sendMessageToUser((FavoriteUser) adapter.getItem(position).getObject());
                    break;
            }
        }

        /**
         * Открывает фрагмент для отсылки сообщения пользователю
         *
         * @param user данные о пользователе для отправки сообщения
         */
        private void sendMessageToUser(FavoriteUser user) {
            SendMessageFragment fragment = SendMessageFragment.getInstance(user, TAG);
            openFragment(fragment, true);
        }

        /**
         * Открывает профиль пользователя
         *
         * @param id id пользователя открываемого профиля
         */
        private void openUserProfile(long id) {
            ProfileFragment fragment = ProfileFragment.getInstance(id);
            openFragment(fragment, true);
        }
    }

    /**
     * Обработчик полученного списка избранных пользователей
     */
    private class GetFavoritesUsersListener extends CallbackHandler<List<FavoriteUser>> {
        public GetFavoritesUsersListener(View view) {
            super(view, R.id.favorites_users_hide_when_loading, R.id.favorites_users_loading_progress);
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

                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);
                throw new ToastException(err.error);
            } catch (ToastException e) {
                e.show(mContext);
            }

            mIsDataLoaded = false;
        }

        @Override
        public void success(List<FavoriteUser> data, Response response) {
            if (data != null) {
                List<Item> items = new ArrayList<>();

                if (mAction == ACTION_SEND_MESSAGE) {
                    items.add(new FavoriteUserItem(mContext, new FavoriteUser(1L, getString(R.string.system_user), null)));
                    items.add(new HeaderItem(getString(R.string.favorite_users)));
                }

                for (FavoriteUser user : data) {
                    items.add(new FavoriteUserItem(mContext, user));
                }

                HeadersArrayAdapter adapter = new HeadersArrayAdapter(mContext, items);
                mFavoritesList.setAdapter(adapter);

                if (mFavoritesListState != null) {
                    mFavoritesList.onRestoreInstanceState(mFavoritesListState);
                }
                mIsDataLoaded = true;
            }

            super.success(data, response);
        }
    }
}
