package ru.neverdark.photohunt.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.adapters.MenuAdapter;
import ru.neverdark.photohunt.dialogs.ConfirmDialog;
import ru.neverdark.photohunt.dialogs.UploadAvatarDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOutput;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;
import ru.neverdark.photohunt.utils.UfoMenuItem;

/**
 * Фрагмент профиля пользователя как своего, так и чужого
 */
public class ProfileFragment extends UfoFragment {

    private final static int INSTAGRAM_MENU_ID = 1;
    private final static int BALANCE_MENU_ID = 2;

    private View mView;
    private Context mContext;
    private boolean mIsDataLoaded;

    private View mRateBalanceButton;
    private View mRatePlaceButton;
    private View mImagesCountButton;
    private View mWinsCountButton;

    private TextView mBalance;
    private TextView mRating;
    private TextView mWorks;
    private TextView mWins;

    private ListView mButtonsList;

    private boolean mIsSelf;
    private long mUserId;
    private RestService.User mUserData;
    private TextView mTitle;
    private ImageView mAvatar;
    private TextView mBalanceCountText;
    private TextView mWorksCountText;
    private TextView mWinsCountText;
    private Uri mOutputUri;

    public static ProfileFragment getInstance(long userId) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.mUserId = userId;
        fragment.mIsSelf = (userId == 0L);
        return fragment;
    }

    @Override
    public void bindObjects() {
        mTitle = (TextView) mView.findViewById(R.id.profile_title);

        mRateBalanceButton = mView.findViewById(R.id.profile_button_balance);
        mRatePlaceButton = mView.findViewById(R.id.profile_button_rating);
        mImagesCountButton = mView.findViewById(R.id.profile_button_works);
        mWinsCountButton = mView.findViewById(R.id.profile_button_wins);

        mBalance = (TextView) mView.findViewById(R.id.profile_balance);
        mBalanceCountText = (TextView) mView.findViewById(R.id.profile_balance_count);
        mRating = (TextView) mView.findViewById(R.id.profile_rating);
        mWorks = (TextView) mView.findViewById(R.id.profile_works);
        mWorksCountText = (TextView) mView.findViewById(R.id.profile_works_count);
        mWins = (TextView) mView.findViewById(R.id.profile_wins);
        mWinsCountText = (TextView) mView.findViewById(R.id.profile_wins_count);

        mAvatar = (ImageView) mView.findViewById(R.id.profile_avatar);

        mButtonsList = (ListView) mView.findViewById(R.id.profile_buttons_list);
    }

    @Override
    public void setListeners() {
        mRateBalanceButton.setOnClickListener(new ButtonClickListener());
        mRateBalanceButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mRatePlaceButton.setOnClickListener(new ButtonClickListener());
        mRatePlaceButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mImagesCountButton.setOnClickListener(new ButtonClickListener());
        mImagesCountButton.setOnTouchListener(new ButtonBGOnTouchListener());
        mWinsCountButton.setOnClickListener(new ButtonClickListener());
        mWinsCountButton.setOnTouchListener(new ButtonBGOnTouchListener());

        mButtonsList.setOnItemClickListener(new ButtonsListClickListener());

        if (mIsSelf) {
            mAvatar.setOnClickListener(new ButtonClickListener());
            registerForContextMenu(mAvatar);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {

        mView = inflater.inflate(R.layout.profile_fragment, container, false);
        mContext = mView.getContext();
        mIsDataLoaded = false;
        bindObjects();
        setListeners();
        getActivity().setTitle(R.string.profile);

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.user_profile, menu);

        if (!mIsSelf) {
            menu.removeItem(R.id.user_profile_edit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_profile_edit:
                openProfileEditFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Отображает фрагмент для редактирования профиля
     */
    private void openProfileEditFragment() {
        EditProfileFragment fragment = EditProfileFragment.getInstance(mUserData);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded) {
            setHasOptionsMenu(false);
            loadData();
        }
    }

    /**
     * Запрос данных с сервера
     */
    private void loadData() {
        Log.enter();
        String user = Settings.getUserId(mContext);
        String password = Settings.getPassword(mContext);
        if (user.length() != 0 || password.length() != 0) {
            RestService service = new RestService(user, password);
            if (mIsSelf) {
                service.getUserApi().getUser(user, new GetUserListener(mView));
            } else {
                service.getUserApi().getUser(mUserId, new GetUserListener(mView));
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        Log.enter();
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.avatar_contest, menu);

        if (!mUserData.avatar_present) {
            menu.removeItem(R.id.remove_avatar);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_avatar:
                if (mUserData.avatar_permission) {
                    uploadAvatar();
                } else {
                    showAttentionDialog();
                }
                break;
            case R.id.remove_avatar:
                removeAvatar();
                break;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Инициирует удаление аватара пользователя
     */
    private void removeAvatar() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessages(R.string.delete_confirmation_title, R.string.remove_avatar_confirmation_message);
        dialog.setCallback(new RemoveAvatarDialogListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    /**
     * Отображает диалог информирующий об отсутствии прав на загрузку аватара и предлагающий перейти
     * в магазин для покупки
     */
    private void showAttentionDialog() {
        ConfirmDialog dialog = ConfirmDialog.getInstance(mContext);
        dialog.setMessages(R.string.goto_shop_title, R.string.goto_shop_message);
        dialog.setCallback(new GotoShopListener());
        dialog.show(getFragmentManager(), ConfirmDialog.DIALOG_ID);
    }

    /**
     * Инициирует загрузку аватара пользователя
     */
    private void uploadAvatar() {
        mOutputUri = Common.chooseImage(mContext, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Common.PICTURE_REQUEST_CODE) {
                Uri selectedImageUri = Common.handleChoosenImage(data, mOutputUri);

                UploadAvatarDialog dialog = UploadAvatarDialog.getInstance(mContext);
                dialog.setData(selectedImageUri, mOutputUri.getPath());
                dialog.show(getFragmentManager(), UploadAvatarDialog.DIALOG_ID);
                dialog.setCallback(new UploadAvatarDialogListener());
            }
        }
    }

    /**
     * Обновляет элементы управления окна на основе полученной от сервера информации
     *
     * @param user объект содержащий информацию о пользователе полученную от сервера
     */
    private void updateProfileInfo(RestService.User user) {

        mTitle.setText(user.display_name);
        mBalance.setText(String.valueOf(user.balance));
        mBalanceCountText.setText(Common.declensionByNumber(user.balance, getResources().getStringArray(R.array.rate_count)));
        mRating.setText(String.valueOf(user.rank));
        mWorks.setText(String.valueOf(user.images_count));
        mWorksCountText.setText(Common.declensionByNumber(user.images_count, getResources().getStringArray(R.array.image_count)));
        mWins.setText(String.valueOf(user.wins_count));
        mWinsCountText.setText(Common.declensionByNumber(user.wins_count, getResources().getStringArray(R.array.contest_wins_count)));

        MenuAdapter adapter = new MenuAdapter(mContext, R.layout.profile_menu_item);
        if (user.insta != null && user.insta.trim().length() > 0) {
            UfoMenuItem menuItem = new UfoMenuItem(mContext, R.drawable.ic_insta_grey600_24dp, user.insta, INSTAGRAM_MENU_ID);
            adapter.add(menuItem);
        }

        if (mIsSelf) {
            String balance = String.format(Locale.US, "%d %s", user.money, Common.declensionByNumber(user.money, getResources().getStringArray(R.array.money)));
            UfoMenuItem menuItem = new UfoMenuItem(mContext, R.drawable.ic_attach_money_grey600_24dp, balance, BALANCE_MENU_ID);
            adapter.add(menuItem);
        }

        if (adapter.getCount() > 0) {
            mButtonsList.setAdapter(adapter);
        }

        if (user.avatar_present) {
            String url = String.format(Locale.US, "%s/avatars/%s.jpg", RestService.getRestUrl(), user.avatar);
            Picasso picasso = new Picasso.Builder(mContext).build();
            picasso.load(url).transform(new Transform()).placeholder(R.drawable.no_avatar).tag(mContext).into(mAvatar);
        }
    }

    /**
     * Открытие фрагмента с магазином
     */
    private void openShop() {
        ShopFragment fragment = ShopFragment.getInstance();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Трансформация загружаемого изображения (ресайз)
     */
    private class Transform implements Transformation {

        public Transform() {
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = (int) mContext.getResources().getDimension(R.dimen.avatar_size);
            Log.variable("width", String.valueOf(targetWidth));
            return Common.resizeBitmap(source, targetWidth, targetWidth);
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    }

    /**
     * Обработчик кликов на кнопках
     */
    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.profile_button_balance:
                    openUserStats();
                    break;

                case R.id.profile_button_rating:
                    openRating();
                    break;

                case R.id.profile_button_works:
                    openUserAlbum();
                    break;

                case R.id.profile_button_wins:
                    openUserWinList();
                    break;

                case R.id.profile_avatar:
                    getActivity().openContextMenu(mAvatar);
                    break;
            }
        }

        /**
         * Открывает статистику пользователя
         */
        private void openUserStats() {
            UserStatsFragment fragment = UserStatsFragment.getInstance(mUserData.id, mUserData.display_name);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        /**
         * Открывает список конкурсов в которых пользователь победил
         */
        private void openUserWinList() {
            StatsFragment fragment = StatsFragment.getInstance(mUserData.id, mUserData.display_name);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        /**
         * Открывает рейтинг пользователей
         */
        private void openRating() {
            RatingFragment fragment = RatingFragment.getInstance();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        /**
         * Отображение фрагмента с альбомом пользователя
         */
        private void openUserAlbum() {
            long userId = mUserData.id;
            String displayName = mUserData.display_name;

            UserImagesFragment fragment = UserImagesFragment.getInstance(userId, displayName);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    /**
     * Обработчик получения профиля пользователя
     */
    private class GetUserListener extends CallbackHandler<RestService.User> {
        public GetUserListener(View mView) {
            super(mView, R.id.profile_hide_when_loading, R.id.profile_loading_progress);
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
        public void success(RestService.User user, Response response) {
            updateProfileInfo(user);
            mIsDataLoaded = true;
            mUserData = user;
            setHasOptionsMenu(true);
            super.success(user, response);
        }
    }

    /**
     * Обработчик кликов по элементам списка
     */
    private class ButtonsListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (id == INSTAGRAM_MENU_ID) {
                openInstaProfile(mUserData.insta);
            } else if (id == BALANCE_MENU_ID) {
                openShop();
            }
        }

        /**
         * Откытие инстаграмма пользователя. Если instagram не установлен на телефоне, то профиль
         * пользователя будет открыт в браузере
         *
         * @param user имя пользователя
         */
        private void openInstaProfile(String user) {
            String url = String.format(Locale.US, "http://instagram.com/_u/%s", user);
            Uri uri = Uri.parse(url);
            Intent insta = new Intent(Intent.ACTION_VIEW, uri);
            insta.setPackage("com.instagram.android");

            if (isIntentAvailable(mContext, insta)) {
                startActivity(insta);
            } else {
                url = String.format(Locale.US, "http://instagram.com/%s", user);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }

        /**
         * Проверяет наличие интента способного обработать ссылку
         *
         * @param context контекст приложения
         * @param intent  интент для проверки
         * @return true если интент найден
         */
        private boolean isIntentAvailable(Context context, Intent intent) {
            final PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
    }

    /**
     * Обработчик нажатия "Да" в диалоге запроса на переход в магазин
     */
    private class GotoShopListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler() {
            openShop();
        }
    }

    /**
     * Обработчик нажатия "Да" в диалоге загрузки аватара
     */
    private class UploadAvatarDialogListener implements OnCallback, UploadAvatarDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler(Uri outputFileUri) {
            mOutputUri = outputFileUri;
            try {
                String user = Settings.getUserId(mContext);
                String pass = Settings.getPassword(mContext);
                RestService service = new RestService(user, pass);
                ImageOutput image = new ImageOutput(mContext, outputFileUri);
                service.getUserApi().addAvatar(image, new UploadAvatarListener());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Обработчик загрузки аватара на сервер
         */
        private class UploadAvatarListener implements Callback<Void> {
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

            @Override
            public void success(Void data, Response response) {
                Common.showMessage(mContext, R.string.upload_successfully);
                mUserData.avatar_present = true;
                int width = (int) getResources().getDimension(R.dimen.avatar_size);

                Bitmap imgBitmap = Common.decodeSampledBitmapFromUri(mContext, mOutputUri, width);
                mAvatar.setImageBitmap(imgBitmap);
            }
        }
    }

    /**
     * Обработчик нажатия "Да" в диалоге удаления аватара
     */
    private class RemoveAvatarDialogListener implements OnCallback, ConfirmDialog.OnPositiveClickListener {
        @Override
        public void onPositiveClickHandler() {
            String user = Settings.getUserId(mContext);
            String pass = Settings.getPassword(mContext);
            RestService service = new RestService(user, pass);
            service.getUserApi().deleteAvatar(new RemoveAvatarListener());
        }

        /**
         * Обработчик удаления аватара
         */
        private class RemoveAvatarListener implements Callback<Void> {
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

            @Override
            public void success(Void data, Response response) {
                Common.showMessage(mContext, R.string.remove_avatar_success);
                mUserData.avatar_present = false;
                mOutputUri = null;
                mAvatar.setImageResource(R.drawable.no_avatar);
            }
        }
    }


}
