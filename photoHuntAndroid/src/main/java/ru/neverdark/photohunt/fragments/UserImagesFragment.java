package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.UserImagesAdapter;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.PicassoScrollListener;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class UserImagesFragment extends UfoFragment {
    private final static String USER_ID = "userId";
    private final static String DISPLAY_NAME = "displayName";

    private long mUserId;
    private Context mContext;
    private View mView;
    private GridView mGrid;
    private boolean mIsDataLoaded;
    private String mDisplayName;

    public static UserImagesFragment getInstance(long userId, String displayName) {
        UserImagesFragment fragment = new UserImagesFragment();
        Bundle args = new Bundle();
        args.putLong(USER_ID, userId);
        args.putString(DISPLAY_NAME, displayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void bindObjects() {
        mGrid = (GridView) mView.findViewById(R.id.user_images_grid);
    }

    @Override
    public void setListeners() {
        mGrid.setOnItemClickListener(new GridItemClickListener());
        mGrid.setOnScrollListener(new PicassoScrollListener(mContext));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.user_images_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.user_album);
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(mDisplayName);
        return mView;
    }

    @Override
    public void onDestroy() {
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getLong(USER_ID);
            mDisplayName = getArguments().getString(DISPLAY_NAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            refresh();
        }
    }

    private void refresh() {
        setHasOptionsMenu(false);
        String user = Settings.getUserId(mContext);
        String password = Settings.getPassword(mContext);
        try {
            if (user.length() == 0 || password.length() == 0) {
                throw new ToastException(R.string.error_not_authorized);
            }

            RestService service = new RestService(user, password);
            service.getUserApi().getUserImages(mUserId, new GetUserImagesListener(mView));
        } catch (ToastException e) {
            e.show(mContext);
        }
    }

    private class GridItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            RestService.Image image = (RestService.Image) mGrid.getAdapter().getItem(position);
            ViewImageFragment fragment = ViewImageFragment.getInstance(mDisplayName, image);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private class GetUserImagesListener extends CallbackHandler<List<RestService.Image>> {

        public GetUserImagesListener(View view) {
            super(view, R.id.user_images_hide_when_loading, R.id.user_images_loading_progress);
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
                Common.showMessage(mContext, err.error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(List<RestService.Image> data, Response response) {
            setHasOptionsMenu(true);
            if (data != null) {
                UserImagesAdapter adapter = new UserImagesAdapter(mContext, data);
                mGrid.setAdapter(adapter);
                mIsDataLoaded = true;
            }

            super.success(data, response);
        }
    }
}