package ru.neverdark.photohunt.fragments;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.RatingAdapter;
import ru.neverdark.photohunt.dialogs.UserInfoDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.User;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class RatingFragment extends UfoFragment {

    private class GetRatingHandler extends CallbackHandler<List<User>> {

        public GetRatingHandler(View view) {
            super(view);
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

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(List<User> data, Response response) {
            if (data != null) {
                RatingAdapter adapter = new RatingAdapter(mContext, R.layout.rating_list_item, data);
                mRatingList.setAdapter(adapter);
            } else {
                Common.showMessage(mContext, R.string.empty_rating);
            }
            
            super.success(data, response);
        }

    }

    private class RatingItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            UserInfoDialog dialog = UserInfoDialog.getInstance(mContext);
            dialog.setUserInfo((User) adapter.getItemAtPosition(position));
            dialog.show(getFragmentManager(), UserInfoDialog.DIALOG_ID);
        }
    }

    private View mView;
    private Context mContext;
    private ListView mRatingList;
    private boolean mIsLoaded = false;

    @Override
    public void bindObjects() {
        mRatingList = (ListView) mView.findViewById(R.id.rating_list);
    }
    

    @Override
    public void setListeners() {
        // TODO: вернуть отображения диалога с пользователем
        //mRatingList.setOnItemClickListener(new RatingItemClickListener());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.rating_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        return mView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (!mIsLoaded) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService service = new RestService(user, pass);
            service.getUserApi().getRating(new GetRatingHandler(mView));
        }
    }
    
}
