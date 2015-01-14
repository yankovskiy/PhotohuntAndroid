package ru.neverdark.photohunt.rest;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.UProgressDialog;

public class CallbackHandler<T> implements Callback<T>{
    private View mView;
    private RelativeLayout mProgress;
    private RelativeLayout mHideLayout;
    
    public CallbackHandler(View view) {
        mView = view;
        mProgress = (RelativeLayout) mView.findViewById(R.id.loading_progress);
        mHideLayout = (RelativeLayout) mView.findViewById(R.id.hide_when_loading);
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
