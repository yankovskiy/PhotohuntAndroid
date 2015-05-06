package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Image;

public class ImageInfoFragment extends UfoFragment {
    private View mView;
    private Context mContext;
    private Image mImage;

    public static ImageInfoFragment getInstance(Image image) {
        ImageInfoFragment fragment = new ImageInfoFragment();
        fragment.mImage = image;
        return fragment;
    }

    @Override
    public void bindObjects() {

    }

    @Override
    public void setListeners() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.image_info_fragment, container, false);
        mContext = mView.getContext();
        getActivity().setTitle(R.string.about_image);
        return mView;
    }
}
