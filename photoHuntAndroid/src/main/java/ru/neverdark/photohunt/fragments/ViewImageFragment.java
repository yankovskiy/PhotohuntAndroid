package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;

public class ViewImageFragment extends UfoFragment {
    private static final String IMAGE = "image";
    private static final String DISPLAY_NAME = "display_name";
    private ImageView mImage;
    private ImageView mContextButton;
    private TextView mVoteCount;
    private RelativeLayout mHeader;
    private TextView mContestSubject;
    private View mView;
    private Context mContext;
    private RestService.Image mImageData;
    private String mDisplayName;

    public static ViewImageFragment getInstance(String displayName, RestService.Image image) {
        ViewImageFragment fragment = new ViewImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMAGE, image);
        args.putString(DISPLAY_NAME, displayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void bindObjects() {
        mImage = (ImageView) mView.findViewById(R.id.view_image);
        mContextButton = (ImageView) mView.findViewById(R.id.view_image_context_button);
        mVoteCount = (TextView) mView.findViewById(R.id.view_image_vote_count);
        mContestSubject = (TextView) mView.findViewById(R.id.view_image_contest_subject);
        mHeader = (RelativeLayout) mView.findViewById(R.id.view_image_header);
    }

    @Override
    public void setListeners() {
        registerForContextMenu(mHeader);
        mContextButton.setOnTouchListener(new ImageOnTouchListener(false));
        mContextButton.setOnClickListener(new MoreButtonClickListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.view_image_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        loadData();
        getActivity().setTitle(R.string.user_album);
        return mView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        Log.enter();
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.view_image, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.enter();

        switch (item.getItemId()) {
            case R.id.view_image_contest:
                showContest(mImageData.contest_id);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showContest(long contestId) {
        DetailContestFragment fragment = new DetailContestFragment(contestId);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageData = (RestService.Image) getArguments().getSerializable(IMAGE);
            mDisplayName = getArguments().getString(DISPLAY_NAME);
        }
    }

    private void loadData() {
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(mDisplayName);
        String url = String.format(Locale.US, "%s/images/%d.jpg", RestService.getRestUrl(), mImageData.id);
        String voteCount = String.format(Locale.US, "%s: %d", getString(R.string.vote_count), mImageData.vote_count);

        Picasso.with(mContext).load(url).transform(new Transform(mImage)).tag(mContext).into(mImage);
        mContestSubject.setText(mImageData.contest_subject);
        mVoteCount.setText(voteCount);
    }

    @Override
    public void onDestroyView() {
        ((UfoFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        super.onDestroyView();
    }

    private class Transform implements Transformation {

        private ImageView mImage;

        public Transform(ImageView image) {
            this.mImage = image;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = this.mImage.getWidth();

            return Common.resizeBitmap(source, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    private class MoreButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().openContextMenu(mHeader);
        }
    }
}
