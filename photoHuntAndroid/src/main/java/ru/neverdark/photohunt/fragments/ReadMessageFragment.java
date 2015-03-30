package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.abs.UfoFragmentActivity;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class ReadMessageFragment extends UfoFragment {
    private long mMessageId;
    private boolean mIsDataLoaded;
    private View mView;
    private Context mContext;
    private RestService.Message mMessage;
    private View mReadMessageHeader;

    private ImageView mAvatar;
    private TextView mFrom;
    private TextView mTitle;
    private TextView mDate;
    private TextView mText;
    private boolean mIsInbox;

    @Override
    public void bindObjects() {
        mAvatar = (ImageView) mView.findViewById(R.id.read_message_avatar);
        mFrom = (TextView) mView.findViewById(R.id.read_message_from);
        mTitle = (TextView) mView.findViewById(R.id.read_message_title);
        mDate = (TextView) mView.findViewById(R.id.read_message_date);
        mText = (TextView) mView.findViewById(R.id.read_message_text);
        mReadMessageHeader = mView.findViewById(R.id.read_message_header);
    }

    @Override
    public void setListeners() {
        mReadMessageHeader.setOnClickListener(new OnHeaderClickListener());
        mReadMessageHeader.setOnTouchListener(new ButtonBGOnTouchListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.read_message_remove:
                showRemoveMessageDialog();
                return true;
            case R.id.read_message_reply:
                openReply();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Открытие фрагмента для написание ответа
     */
    private void openReply() {
        RestService.FavoriteUser data = new RestService.FavoriteUser();
        data.avatar = mMessage.from_avatar;
        data.display_name = mMessage.from;
        data.fid = mMessage.from_user_id;
        SendMessageFragment fragment = SendMessageFragment.getInstance(data, null, mMessage.title, mMessage.message);
        Common.openFragment(this, fragment, true);
    }

    /**
     * Отображение диалога для подтверждения удаления
     */
    private void showRemoveMessageDialog() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessage(R.string.remove_message_confirm);
        dialog.setCallback(new RemoveMessageDialogListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.read_message, menu);

        if (!mIsInbox) {
            menu.removeItem(R.id.read_message_reply);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public static ReadMessageFragment getInstance(long messageId, boolean isInbox) {
        ReadMessageFragment fragment = new ReadMessageFragment();
        fragment.mMessageId = messageId;
        fragment.mIsInbox = isInbox;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.read_message_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        setListeners();
        getActivity().setTitle("");
        mIsDataLoaded = false;
        return mView;
    }

    /**
     * Загрузить сообщение с сервера
     */
    private void loadData() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getMessagesApi().readMessage(mMessageId, new ReadMessageListener(mView));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            loadData();
        }
    }

    private class ReadMessageListener extends CallbackHandler<RestService.Message> {
        public ReadMessageListener(View view) {
            super(view, R.id.read_message_hide_when_loading, R.id.read_message_loading_progress);
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
            setHasOptionsMenu(false);
        }

        @Override
        public void success(RestService.Message data, Response response) {
            if (data != null) {
                if (isAdded()) {
                    updateView(data);
                    mMessage = data;
                }
                mIsDataLoaded = true;
                setHasOptionsMenu(true);
            }

            super.success(data, response);
        }

        /**
         * Обновляет элементы управления фрагмента на основе полученных данных
         * @param message полученное сообщение
         */
        private void updateView(RestService.Message message) {
            String date = Common.parseDate(mContext, message.date);
            String time = message.date.split(" ")[1];

            String avatar;
            long userId;
            String displayName;

            if (mIsInbox) {
                avatar = message.from_avatar;
                userId = message.from_user_id;
                displayName = message.from;
            } else {
                avatar = message.to_avatar;
                userId = message.to_user_id;
                displayName = message.to;
            }

            if (userId == 1L) {// system
                mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.system_avatar_48dp));
            } else if (message.from_avatar != null && message.from_avatar.trim().length() > 0) {
                String url = String.format(Locale.US, "%s/avatars/%s.jpg", RestService.getRestUrl(), avatar);
                Picasso.with(mContext).load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(mAvatar);
            }

            mFrom.setText(displayName);
            mTitle.setText(message.title);
            mDate.setText(String.format("%s\n%s", date, time));
            mText.setText(message.message);
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
    }

    /**
     * Обработчик удаления сообщения
     */
    private class RemoveMessageListener implements Callback<Void> {
        @Override
        public void success(Void aVoid, Response response) {
            getFragmentManager().popBackStack();
            Common.showMessage(mContext, R.string.message_removed);
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
                throw new ToastException(R.string.error_unexpected_error);
            } catch (ToastException e) {
                e.show(mContext);
            }

            setHasOptionsMenu(true);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void onDetach() {
        ((UfoFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        super.onDetach();
    }

    /**
     * Обработчик подтверждения удаления в диалоге
     */
    private class RemoveMessageDialogListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
        /**
         * Запуск процесса удаления сообщения
         */
        private void removeMessage() {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            setHasOptionsMenu(false);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);

            RestService service = new RestService(user, pass);
            service.getMessagesApi().removeMessage(mMessageId, new RemoveMessageListener());
        }

        @Override
        public void onPositiveClickHandler() {
            removeMessage();
        }
    }

    /**
     * Обработка клика по данным о получателе / отправителе
     */
    private class OnHeaderClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openUserProfile();
        }
    }

    /**
     * Открыть профиль пользователя (получателя / отправителя)
     */
    private void openUserProfile() {
        long userId;
        if (mIsInbox) {
            userId = mMessage.from_user_id;
        } else {
            userId = mMessage.to_user_id;
        }

        ProfileFragment fragment = ProfileFragment.getInstance(userId);
        Common.openFragment(this, fragment, true);
    }
}
