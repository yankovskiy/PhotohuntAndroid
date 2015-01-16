package ru.neverdark.photohunt.fragments;

import java.util.Locale;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.RestService.Contest;
import ru.neverdark.photohunt.utils.ButtonOnTouchListener;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;
import ru.neverdark.abs.UfoFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Главный фрагмент приложения содержит информацию о текущем конкурсе Время до
 * окончания, тема, награда, автор
 */
public class BriefContestFragment extends UfoFragment {
    private class SubjectClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            if (mContestId != -1) {
                DetailContestFragment fragment = new DetailContestFragment(mContestId);
                ActionBarDrawerToggle toggle = ((UfoFragmentActivity) getActivity()).getDrawerToggle();
                fragment.setDrawerToggle(toggle);
                fragment.setChangeNavi(true);
                fragment.setBackHandle(true);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

    private class GetLastContestHandler extends CallbackHandler<Contest> {

        public GetLastContestHandler(View view) {
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
                
                if (response.getStatus() == 404) {
                    throw new ToastException(R.string.error_contest_not_found);
                }

                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }
        }

        @Override
        public void success(Contest data, Response response) {
            if (data != null) {
                String subject = data.subject;//String.format("%s: %s", getString(R.string.subject), data.subject);
                String author = String.format("%s: %s", getString(R.string.author), data.display_name);
                String closeDate = String.format("%s: %s", getString(R.string.close_date), data.close_date);
                String rewards = String.format(Locale.US, "%s: %d", getString(R.string.reward), data.rewards);
                String works = String.format("%s: %s", getString(R.string.works_count), data.works);
                
                mAuthor.setText(author);
                mSubject.setText(subject);
                mCloseDate.setText(closeDate);
                mRewards.setText(rewards);
                mWorks.setText(works);
                mContestId = data.id;
                
                String enterText = null;
                switch (data.status) {
                case Contest.STATUS_OPEN:
                    enterText = getString(R.string.take_part);
                    break;
                case Contest.STATUS_VOTES:
                    enterText = getString(R.string.vote);
                    break;
                case Contest.STATUS_CLOSE:
                    enterText = getString(R.string.view);
                }
                
                mEnter.setText(enterText.toUpperCase(Locale.getDefault()));
                mIsDataLoaded = true;
            }
            
            super.success(data, response);
        }

    }

    private long mContestId = -1;
    private Context mContext;
    private View mView;
    private boolean mIsDataLoaded;
    private TextView mSubject;
    private TextView mAuthor;
    private TextView mCloseDate;
    private TextView mRewards;
    private TextView mEnter;
    private TextView mWorks;

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mIsDataLoaded = false;
        mView = inflater.inflate(R.layout.brief_contest_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        return mView;
    }

    @Override
    public void bindObjects() {
        mSubject = (TextView) mView.findViewById(R.id.brief_contest_subject);
        mAuthor = (TextView) mView.findViewById(R.id.brief_contest_author);
        mCloseDate = (TextView) mView.findViewById(R.id.brief_contest_close);
        mRewards = (TextView) mView.findViewById(R.id.brief_contest_reward);
        mEnter = (TextView) mView.findViewById(R.id.brief_contest_enter);
        mWorks = (TextView) mView.findViewById(R.id.brief_contest_works);
    }

    @Override
    public void setListeners() {
        mEnter.setOnClickListener(new SubjectClickListener());
        mEnter.setOnTouchListener(new ButtonOnTouchListener());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            
            RestService service = new RestService(user, pass);
            service.getContestApi().getLastContest(new GetLastContestHandler(mView));
        }
    }
}
