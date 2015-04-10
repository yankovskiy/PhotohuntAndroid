package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.MessagesAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Message;
import ru.neverdark.photohunt.rest.data.Messages;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

/**
 * Фрагмент содержащий список уведомлений
 */
public class MessagesFragment extends UfoFragment {
    private final static String INBOX_LIST_POSITION = "inboxListPosition";
    private final static String OUTBOX_LIST_POSITION = "outboxListPosition";
    private final static String TAB_POSITION = "tabPosition";
    private static final int INBOX_FOLDER = 0;
    private Parcelable mInboxState = null;
    private Parcelable mOutboxState = null;
    private int mTabIndex = 0;

    private View mView;
    private Context mContext;
    private ListView mInboxList;
    private boolean mIsDataLoaded;
    private ListView mOutboxList;
    private TabHost mTabHost;
    private long mUserId;
    private MenuItem mRemoveItemMenu;

    public static MessagesFragment getInstance(long userId) {
        MessagesFragment fragment = new MessagesFragment();
        fragment.mUserId = userId;
        return fragment;
    }

    @Override
    public void bindObjects() {
        mInboxList = (ListView) mView.findViewById(R.id.messages_inbox_list);
        mOutboxList = (ListView) mView.findViewById(R.id.messages_outbox_list);
    }

    @Override
    public void setListeners() {
        mInboxList.setOnItemClickListener(new MessageClickListener());
        mOutboxList.setOnItemClickListener(new MessageClickListener());
        mTabHost.setOnTabChangedListener(new TabChangeListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.enter();
        mView = inflater.inflate(R.layout.messages_fragment, container, false);
        mContext = mView.getContext();
        bindObjects();
        getActivity().setTitle(R.string.messages);
        initTabs();
        setListeners();
        setHasOptionsMenu(true);
        mIsDataLoaded = false;
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsDataLoaded) {
            loadData();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messages_new_message:
                openFavoritesUsersFragment();
                return true;
            case R.id.messages_remove:
                showRemoveMessageDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Открывать фрагмент с избранными авторами
     */
    private void openFavoritesUsersFragment() {
        FavoritesUsersFragment fragment = FavoritesUsersFragment.getInstance(mUserId, FavoritesUsersFragment.ACTION_SEND_MESSAGE);
        Common.openFragment(this, fragment, FavoritesUsersFragment.TAG);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages, menu);
        mRemoveItemMenu = menu.findItem(R.id.messages_remove);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Запрос данных с сервера
     */
    private void loadData() {
        String user = Settings.getUserId(mContext);
        String pass = Settings.getPassword(mContext);

        RestService service = new RestService(user, pass);
        service.getMessagesApi().getMessages(new GetMessagesListener(mView));
    }

    /**
     * Инициализация вкладок
     */
    private void initTabs() {
        mTabHost = (TabHost) mView.findViewById(R.id.messages_tabHost);
        mTabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = mTabHost.newTabSpec("inbox");
        tabSpec.setIndicator(getString(R.string.inbox));
        tabSpec.setContent(R.id.messages_inbox_list);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec("outbox");
        tabSpec.setIndicator(getString(R.string.outbox));
        tabSpec.setContent(R.id.messages_outbox_list);
        mTabHost.addTab(tabSpec);
    }

    /**
     * Отображение диалога для подтверждения удаления
     */
    private void showRemoveMessageDialog() {
        int messageId;
        if (mTabHost.getCurrentTab() == INBOX_FOLDER) {
            messageId = R.string.remove_inbox_messages_confirm;
        } else {
            messageId = R.string.remove_outbox_messages_confirm;
        }
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessage(messageId);
        dialog.setCallback(new RemoveMessageDialogListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    /**
     * Открытие фрагмента для чтения сообщения
     *
     * @param id      id сообщения для прочтения
     * @param isInbox true если открыть сообщение из входящих
     */
    private void openReadMessageFragment(long id, boolean isInbox) {
        Common.openFragment(this, ReadMessageFragment.getInstance(id, isInbox), true);
    }

    @Override
    public void onDestroyView() {
        Log.enter();
        mInboxState = mInboxList.onSaveInstanceState();
        mOutboxState = mOutboxList.onSaveInstanceState();
        mTabIndex = mTabHost.getCurrentTab();
        super.onDestroyView();
    }

    /**
     * Обработчик загрузки сообщений с сервера
     */
    private class GetMessagesListener extends CallbackHandler<Messages> {
        public GetMessagesListener(View view) {
            super(view, R.id.messages_hide_when_loading, R.id.messages_loading_progress);
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

            mIsDataLoaded = false;
        }

        @Override
        public void success(Messages data, Response response) {
            if (data != null) {
                if (isAdded()) {
                    updateList(data);
                }
                mIsDataLoaded = true;

                if (mInboxState != null) {
                    mInboxList.onRestoreInstanceState(mInboxState);
                }

                if (mOutboxState != null) {
                    mOutboxList.onRestoreInstanceState(mOutboxState);
                }

                if (mTabIndex != 0) {
                    mTabHost.setCurrentTab(mTabIndex);
                }

                setRemoveItemVisible();
            }

            super.success(data, response);
        }

        /**
         * Обновляет список сообщений вновь полученными данными
         *
         * @param data полученный список сообщений
         */
        private void updateList(Messages data) {
            MessagesAdapter inboxAdapter = new MessagesAdapter(mContext, R.layout.messages_list_item, data.inbox, true);
            mInboxList.setAdapter(inboxAdapter);
            MessagesAdapter outboxAdapter = new MessagesAdapter(mContext, R.layout.messages_list_item, data.outbox, false);
            mOutboxList.setAdapter(outboxAdapter);
        }
    }

    /**
     * Обработчик кликов по элементам списка с сообщениями
     */
    private class MessageClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            boolean isInbox = (parent.getId() == R.id.messages_inbox_list);
            openReadMessageFragment(id, isInbox);
        }
    }

    /**
     * Обработчик подтверждения удаления в диалоге
     */
    private class RemoveMessageDialogListener implements ConfirmDialog.OnPositiveClickListener, OnCallback {
        @Override
        public void onPositiveClickHandler() {
            String folder;
            if (mTabHost.getCurrentTab() == INBOX_FOLDER) {
                folder = "inbox";
            } else {
                folder = "outbox";
            }

            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            setHasOptionsMenu(false);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
            RestService service = new RestService(user, pass);
            service.getMessagesApi().removeMessages(folder, new RemoveMessagesListener());
        }
    }

    @Override
    public void onDetach() {
        ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
        super.onDetach();
    }

    /**
     * Обработчик удаления сообщений
     */
    private class RemoveMessagesListener implements Callback<Void> {
        @Override
        public void success(Void data, Response response) {
            setHasOptionsMenu(true);
            ((MainActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(false);
            MessagesAdapter adapter;

            if (mTabHost.getCurrentTab() == INBOX_FOLDER) {
                adapter = (MessagesAdapter) mInboxList.getAdapter();
                for (int i = adapter.getCount() - 1; i >= 0 ; i--) {
                    Log.variable("i", String.valueOf(i));
                    Message message = adapter.getItem(i);
                    if (message.status == Message.READ) {
                        adapter.remove(message);
                    }
                }
            } else {
                adapter = (MessagesAdapter) mOutboxList.getAdapter();
                adapter.clear();
            }
            adapter.notifyDataSetChanged();

            setRemoveItemVisible();
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

    private class TabChangeListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            setRemoveItemVisible();
        }
    }

    public void setRemoveItemVisible() {
        if (mTabHost.getCurrentTab() == INBOX_FOLDER) {
            MessagesAdapter adapter = (MessagesAdapter) mInboxList.getAdapter();
            mRemoveItemMenu.setVisible(adapter.isReadInbox());
        } else {
            mRemoveItemMenu.setVisible(mOutboxList.getAdapter().getCount() > 0);
        }
    }
}