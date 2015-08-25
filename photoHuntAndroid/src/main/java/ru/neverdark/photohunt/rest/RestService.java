package ru.neverdark.photohunt.rest;

import retrofit.RestAdapter;
import ru.neverdark.photohunt.BuildConfig;
import ru.neverdark.photohunt.rest.api.AchievementsApi;
import ru.neverdark.photohunt.rest.api.CommentApi;
import ru.neverdark.photohunt.rest.api.ContestApi;
import ru.neverdark.photohunt.rest.api.MessagesApi;
import ru.neverdark.photohunt.rest.api.ShopApi;
import ru.neverdark.photohunt.rest.api.UserApi;

public class RestService {
    private final static String DEBUG_REST_URL = "http://192.168.0.3/api.tim-sw.com";
    private final static String RELEASE_REST_URL = "http://api.tim-sw.com";
    private final RestAdapter mRestAdapter;
    private final UserApi mUserApi;
    private final ContestApi mContestApi;
    private final ShopApi mShopApi;
    private final MessagesApi mMessagesApi;
    private final CommentApi mCommentApi;
    private final AchievementsApi mAchievementsApi;

    public RestService() {
        BasicInterceptor interceptor = new BasicInterceptor();

        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(interceptor).setEndpoint(getRestUrl()).build();

        mUserApi = mRestAdapter.create(UserApi.class);
        mContestApi = mRestAdapter.create(ContestApi.class);
        mShopApi = mRestAdapter.create(ShopApi.class);
        mMessagesApi = mRestAdapter.create(MessagesApi.class);
        mCommentApi = mRestAdapter.create(CommentApi.class);
        mAchievementsApi = mRestAdapter.create(AchievementsApi.class);
    }


    public RestService(String user, String password) {
        AuthInterceptor auth = new AuthInterceptor();
        auth.setAuthData(user, password);

        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(auth).setEndpoint(getRestUrl()).build();

        mUserApi = mRestAdapter.create(UserApi.class);
        mContestApi = mRestAdapter.create(ContestApi.class);
        mShopApi = mRestAdapter.create(ShopApi.class);
        mMessagesApi = mRestAdapter.create(MessagesApi.class);
        mCommentApi = mRestAdapter.create(CommentApi.class);
        mAchievementsApi = mRestAdapter.create(AchievementsApi.class);
    }

    public static String getRestUrl() {
        if (BuildConfig.DEBUG) {
            return DEBUG_REST_URL;
        } else {
            return RELEASE_REST_URL;
        }
    }

    public AchievementsApi getAchievementsApi() {
        return mAchievementsApi;
    }

    public CommentApi getCommentApi() {
        return mCommentApi;
    }

    public ContestApi getContestApi() {
        return mContestApi;
    }

    public UserApi getUserApi() {
        return mUserApi;
    }

    public ShopApi getShopApi() {
        return mShopApi;
    }

    public MessagesApi getMessagesApi() {
        return mMessagesApi;
    }

    public static class ErrorData {
        public String error;
    }
}
