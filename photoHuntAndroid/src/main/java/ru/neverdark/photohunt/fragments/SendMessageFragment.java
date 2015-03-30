package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент для отправки сообщения
 */
public class SendMessageFragment extends UfoFragment {
    private boolean mIsDataLoaded;
    private Context mContext;
    private View mView;
    private RestService.FavoriteUser mRecipient;
    private String mCalledTag;
    private ImageView mAvatar;
    private TextView mUser;
    private EditText mTitle;
    private EditText mText;
    private String mTitleData;
    private String mMessageData;
    private boolean mIsReply;

    @Override
    public void bindObjects() {
        mAvatar = (ImageView) mView.findViewById(R.id.send_message_avatar);
        mUser = (TextView) mView.findViewById(R.id.send_message_user);
        mTitle = (EditText) mView.findViewById(R.id.send_message_title);
        mText = (EditText) mView.findViewById(R.id.send_message_text);
    }

    @Override
    public void setListeners() {

    }

    /**
     * Создание фрагмента
     * @param recipient данные о получателе
     * @param calledTag tag вызывающего фрагмента
     * @return созданный фрагмент
     */
    public static SendMessageFragment getInstance(RestService.FavoriteUser recipient, String calledTag) {
        SendMessageFragment fragment = new SendMessageFragment();
        fragment.mRecipient = recipient;
        fragment.mCalledTag = calledTag;
        fragment.mIsReply = false;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.send_message_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.message);
        bindData();
        setHasOptionsMenu(true);
        mIsDataLoaded = false;
        return mView;
    }

    /**
     * Маппинг данных в элементы управления окна
     */
    private void bindData() {
        mUser.setText(mRecipient.display_name);
        if (mRecipient.fid == 1L) {// system
            mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
        } else if (mRecipient.avatar != null && mRecipient.avatar.trim().length() > 0) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg", RestService.getRestUrl(), mRecipient.avatar);
            Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(mAvatar);
        }

        if (mIsReply) {
            mTitle.setText(String.format("%s: %s", getString(R.string.re), mTitleData));
            mText.setText(String.format("\n\n------------------------\n%s:\n%s", mRecipient.display_name, mMessageData));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.send_message, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_message:
                sendMessage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Отправка сообщения
     */
    private void sendMessage() {
        String title = mTitle.getText().toString().trim();
        String message = mText.getText().toString().trim();

        try {
            if (title.length() == 0) {
                mTitle.requestFocus();
                throw new ToastException(R.string.error_empty_title);
            }

            if (message.length() == 0) {
                mText.requestFocus();
                throw new ToastException(R.string.error_empty_text);
            }

            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService.Message data = new RestService.Message();
            data.title = title;
            data.message = message;
            data.to_user_id = mRecipient.fid;

            setHasOptionsMenu(false);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
            RestService service = new RestService(user, pass);
            service.getMessagesApi().sendMessage(data, new SendMessageListener());
        } catch (ToastException e) {
            e.show(mContext);
        }
    }

    /**
     * Создание фрагмента для отправки сообщения (ответ на сообщение)
     * @param data данные о получателе
     * @param tag tag вызывающего фрагмента
     * @param title тема сообщения
     * @param message текст сообщения
     * @return созданный фрагмент
     */
    public static SendMessageFragment getInstance(RestService.FavoriteUser data, String tag, String title, String message) {
        SendMessageFragment fragment = getInstance(data, tag);
        fragment.mTitleData = title;
        fragment.mMessageData = message;
        fragment.mIsReply = true;
        return fragment;
    }

    /**
     * Обработка загружаемого аватара
     */
    private class Transform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = (int) mContext.getResources().getDimension(R.dimen.min_avatar_size);
            return Common.resizeBitmap(source, targetWidth, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    /**
     * Обработчик отправки сообщения
     */
    private class SendMessageListener implements Callback<Void> {
        @Override
        public void success(Void data, Response response) {
            if (mCalledTag == null) {
                getFragmentManager().popBackStack();
            } else {
                getFragmentManager().popBackStackImmediate(mCalledTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            Common.showMessage(mContext, R.string.message_sent);
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

            setHasOptionsMenu(true);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void onDetach() {
        ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        super.onDetach();
    }
}
