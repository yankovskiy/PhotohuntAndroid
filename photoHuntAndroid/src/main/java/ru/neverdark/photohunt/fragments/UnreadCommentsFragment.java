package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.UnreadCommentsAdapter;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.rest.data.UnreadComment;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class UnreadCommentsFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private ListView mCommentsList;

    public static UnreadCommentsFragment getInstance() {
        return new UnreadCommentsFragment();
    }

    @Override
    public void bindObjects() {
        mCommentsList = (ListView) mView.findViewById(R.id.unread_comments_list);
    }

    @Override
    public void setListeners() {
        mCommentsList.setOnItemClickListener(new OnCommentClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.unread_comments_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.comments);
        loadData();
        return mView;
    }

    private void loadData() {
        RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
        service.getCommentApi().getUnreadComments(new GetUnreadCommentsListener());
    }

    private void openViewImageFragment(Image image) {
        Log.enter();
        ViewSingleImageFragment fragment = ViewSingleImageFragment.getInstance(image, image.contest_status);
        Common.openFragment(this, fragment, true);
    }

    private class GetUnreadCommentsListener implements Callback<List<UnreadComment>> {
        @Override
        public void success(List<UnreadComment> comments, Response response) {
            if (comments != null) {
                UnreadCommentsAdapter adapter = new UnreadCommentsAdapter(mContext, comments);
                mCommentsList.setAdapter(adapter);
            }
        }

        @Override
        public void failure(RetrofitError error) {
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
        }
    }

    private class OnCommentClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            RestService service = new RestService(Settings.getUserId(mContext), Settings.getPassword(mContext));
            service.getContestApi().getImageById(id, new GetByIdListener());
        }

        private class GetByIdListener implements Callback<Image> {
            @Override
            public void success(Image image, Response response) {
                if (image != null) {
                    openViewImageFragment(image);
                }
            }

            @Override
            public void failure(RetrofitError error) {
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
            }
        }
    }
}
