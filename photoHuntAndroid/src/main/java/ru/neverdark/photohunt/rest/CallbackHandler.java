package ru.neverdark.photohunt.rest;

import android.view.View;
import android.widget.RelativeLayout;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CallbackHandler<T> implements Callback<T> {
    private View mView;
    private RelativeLayout mProgress;
    private RelativeLayout mHideLayout;

    public CallbackHandler(View view, int hideViewId, int progressBarId) {
        mView = view;
        mProgress = (RelativeLayout) mView.findViewById(progressBarId);
        mHideLayout = (RelativeLayout) mView.findViewById(hideViewId);
        mProgress.setVisibility(View.VISIBLE);
        mHideLayout.setVisibility(View.GONE);
    }

    @Override
    public void failure(RetrofitError error) {
        mProgress.setVisibility(View.GONE);
        mHideLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void success(T data, Response response) {
        mProgress.setVisibility(View.GONE);
        mHideLayout.setVisibility(View.VISIBLE);
    }

}
