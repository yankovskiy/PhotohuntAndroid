package ru.neverdark.photohunt.rest;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedOutput;
import ru.neverdark.photohunt.BuildConfig;

public class RestService {
    private final static String DEBUG_REST_URL = "http://192.168.0.3/api.tim-sw.com";
    private final static String RELEASE_REST_URL = "http://api.tim-sw.com";
    private final RestAdapter mRestAdapter;
    private final UserMgmt mUserMgmt;
    private final ContestMgmt mContestMgmt;
    private final ShopMgmt mShopMgmt;
    private final ShopApi mShopApi;
    private final UserApi mUserApi;
    private final ContestApi mContestApi;
    private final MessagesApi mMessagesApi;
    private final MessagesMgmt mMessagesMgmt;

    public RestService() {
        BasicInterceptor interceptor = new BasicInterceptor();

        mContestApi = new ContestApi();
        mUserApi = new UserApi();
        mShopApi = new ShopApi();
        mMessagesApi = new MessagesApi();

        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(interceptor).setEndpoint(getRestUrl()).build();

        mUserMgmt = mRestAdapter.create(UserMgmt.class);
        mContestMgmt = mRestAdapter.create(ContestMgmt.class);
        mShopMgmt = mRestAdapter.create(ShopMgmt.class);
        mMessagesMgmt = mRestAdapter.create(MessagesMgmt.class);
    }

    public RestService(String user, String password) {
        AuthInterceptor auth = new AuthInterceptor();
        auth.setAuthData(user, password);

        mContestApi = new ContestApi();
        mUserApi = new UserApi();
        mShopApi = new ShopApi();
        mMessagesApi = new MessagesApi();

        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(auth).setEndpoint(getRestUrl()).build();

        mUserMgmt = mRestAdapter.create(UserMgmt.class);
        mContestMgmt = mRestAdapter.create(ContestMgmt.class);
        mShopMgmt = mRestAdapter.create(ShopMgmt.class);
        mMessagesMgmt = mRestAdapter.create(MessagesMgmt.class);
    }

    public static String getRestUrl() {
        if (BuildConfig.DEBUG) {
            return DEBUG_REST_URL;
        } else {
            return RELEASE_REST_URL;
        }
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

    private interface MessagesMgmt {
        @GET("/messages")
        public void getMessages(Callback<Messages> callback);

        @PUT("/messages/{id}")
        public void readMessage(@Path("id") long messageId, Callback<Message> callback);

        @DELETE("/messages/{id}")
        public void removeMessage(@Path("id") long messageId, Callback<Void> callback);

        @DELETE("/messages/{folder}")
        public void removeMessages(@Path("folder") String folder, Callback<Void> callback);

        @POST("/messages")
        void sendMessage(@Body Message data, Callback<Void> callback);
    }


    private interface ShopMgmt {
        @GET("/shop")
        public void getShop(Callback<ShopData> callback);

        @GET("/shop/my")
        public void getMyItems(Callback<List<Item>> callback);

        @POST("/shop/{id}")
        public void buyItem(@Path("id") long itemId, Callback<Void> callback);

        @PUT("/shop/my/{id}")
        public void useItem(@Path("id") long itemId, Callback<Void> callback);
    }

    private interface ContestMgmt {
        @DELETE("/image/{id}")
        public void deleteImage(@Path("id") long imageId, Callback<Void> callback);

        @PUT("/image/{id}")
        public void updateImage(@Path("id") long imageId, @Body Image image, Callback<Void> callback);

        @GET("/contests")
        public void getContests(Callback<List<Contest>> callback);

        @GET("/contest")
        public void getOpenContests(Callback<List<Contest>> callback);

        @GET("/contest/{id}")
        public void getContestDetails(@Path("id") long id, Callback<ContestDetail> callback);

        @Multipart
        @POST("/contest/{id}")
        public void addImageToContest(@Path("id") long id, @Part("subject") String subject,
                                      @Part("image") TypedOutput image, Callback<Void> callback);

        @PUT("/contest/{id}")
        public void voteForContest(@Path("id") long id, @Body Image image, Callback<Void> callback);
    }

    private interface UserMgmt {
        @GET("/favorites/users")
        public void getFavoritesUsers(Callback<List<FavoriteUser>> callback);

        @PUT("/favorites/users/{id}")
        public void updateFavoriteUser(@Path("id") long id, Callback<Void> callback);

        @GET("/user/{id}/wins")
        public void getWinsList(@Path("id") long id, Callback<List<Contest>> callback);

        @GET("/user/{id}/stats")
        public void getUserStats(@Path("id") long id, Callback<Stats> callback);

        @Multipart
        @POST("/avatar")
        public void addAvatar(@Part("image") TypedOutput image, Callback<Void> callback);

        @DELETE("/avatar")
        public void deleteAvatar(Callback<Void> callback);

        @GET("/user/{id}/images")
        public void getUserImages(@Path("id") long id, Callback<List<Image>> callback);

        @POST("/user")
        public void addUser(@Body User user, Callback<User> callback);

        @DELETE("/user/{id}")
        public void deleteUser(@Path("id") String userId, Callback<User> callback);

        @PUT("/reset")
        public void generateHash(@Body User user, Callback<User> callback);

        @GET("/user/{id}")
        public void getUser(@Path("id") String userId, Callback<User> callback);

        @GET("/user/{id}")
        public void getUser(@Path("id") long userId, Callback<User> callback);


        @PUT("/user/{id}")
        public void updateUser(@Path("id") String userId, @Body User user, Callback<Void> callback);

        @PUT("/user/{id}")
        public Void updateUser(@Path("id") String userId, @Body User user);

        @GET("/user")
        public void getRating(Callback<List<User>> callback);
    }

    public static class ErrorData {
        public String error;
    }

    public static class Goods {
        public long id;
        public String service_name;
        public String name;
        public String description;
        public int price_money;
        public int price_dc;
        public int auto_use;
    }

    public static class Item {
        public final static String EXTRA_PHOTO = "extra_photo";
        public final static String AVATAR = "avatar";
        public final static String EXTRA_CONTEST = "extra_contest";
        public final static String PREMIUM7 = "premium7";
        public final static String PREMIUM30 = "premium30";

        public long id;
        public String name;
        public String service_name;
        public String description;
        public int count;
        public int auto_use;

        public Item() {

        }

        public Item(Goods goods) {
            name = goods.name;
            description = goods.description;
            auto_use = goods.auto_use;
            service_name = goods.service_name;
            count = 1;
        }
    }

    public static class ShopData {
        public int money;
        public int dc;
        public List<Goods> shop_items;
        public List<Item> my_items;
    }

    public static class Image implements Serializable {
        public long id;
        public long contest_id;
        public long user_id;
        public String subject;
        public String display_name;
        public int vote_count;
        public boolean is_editable;
        public boolean is_voted;
        public String contest_subject;

        @Override
        public String toString() {
            return String
                    .format(Locale.US,
                            "Image id = %d, contest_id = %d, user_id = %d, subject = %s, display_name = %s, vote_count = %d, is_editable = %b, is_voted = %b, contest_subject = %s",
                            id, contest_id, user_id, subject, display_name, vote_count, is_editable, is_voted, contest_subject);
        }
    }

    public static class Contest {
        public final static int STATUS_CLOSE = 0;
        public final static int STATUS_OPEN = 1;
        public final static int STATUS_VOTES = 2;

        public long id;
        public String subject;
        public int rewards;
        public String open_date;
        public String close_date;
        public int status;
        public long user_id;
        public String display_name;
        public int works;
        public long prev_id;

        @Override
        public String toString() {
            return String
                    .format(Locale.US,
                            "Contest id = %d, subject = %s, rewards = %d, open_date = %s, close_date = %s, status = %d, user_id = %d, display_name = %s, prev_id = %d",
                            id, subject, rewards, open_date, close_date, status, user_id, display_name, prev_id);
        }
    }

    public static class ContestDetail {
        public Contest contest;
        public List<Image> images;
        public int votes;
    }

    public static class User {
        public long id;
        public String user_id;
        public String display_name;
        public String password;
        public int balance;
        public String hash;
        public String insta;
        public int images_count;
        public int rank;
        public int wins_count;
        public int money;
        public int dc;
        public boolean avatar_present;
        public boolean avatar_permission;
        public String avatar;
        public String regid;
        public int unread_messages;
        public int client_version;
        public boolean is_bookmarked;
        public boolean is_have_favorites;
    }

    public static class Stats {
        public int total;
        public int works;
        public int wins_rewards;
        public int other;
    }

    public static class Message {
        public final static int UNSENT = 0;
        public final static int SENT = 1;
        public final static int READ = 2;

        public long id;
        public long from_user_id;
        public long to_user_id;
        public String from;
        public String to;
        public String date;
        public String title;
        public String message;
        public int status;
        public String from_avatar;
        public String to_avatar;
    }

    public static class Messages {
        public List<Message> inbox;
        public List<Message> outbox;
    }

    public class ContestApi {
        public void updateImage(long imageId, Image image, Callback<Void> callback) {
            mContestMgmt.updateImage(imageId, image, callback);
        }

        public void deleteImage(long imageId, Callback<Void> callback) {
            mContestMgmt.deleteImage(imageId, callback);
        }

        public void getContests(Callback<List<Contest>> callback) {
            mContestMgmt.getContests(callback);
        }

        public void getOpenContests(Callback<List<Contest>> callback) {
            mContestMgmt.getOpenContests(callback);
        }

        public void getContestDetails(long id, Callback<ContestDetail> callback) {
            mContestMgmt.getContestDetails(id, callback);
        }

        public void addImageToContest(long id, String subject, TypedOutput image, Callback<Void> callback) {
            mContestMgmt.addImageToContest(id, subject, image, callback);
        }

        public void voteForContest(long id, Image image, Callback<Void> callback) {
            mContestMgmt.voteForContest(id, image, callback);
        }
    }

    public class ShopApi {
        public void getShop(Callback<ShopData> callback) {
            mShopMgmt.getShop(callback);
        }

        public void getMyItems(Callback<List<Item>> callback) {
            mShopMgmt.getMyItems(callback);
        }

        public void buyItem(long itemId, Callback<Void> callback) {
            mShopMgmt.buyItem(itemId, callback);
        }

        public void useItem(long itemId, Callback<Void> callback) {
            mShopMgmt.useItem(itemId, callback);
        }
    }

    public class MessagesApi {
        public void sendMessage(Message data, Callback<Void> callback) {
            mMessagesMgmt.sendMessage(data, callback);
        }
        public void getMessages(Callback<Messages> callback) {
            mMessagesMgmt.getMessages(callback);
        }

        public void readMessage(long messageId, Callback<Message> callback) {
            mMessagesMgmt.readMessage(messageId, callback);
        }

        public void removeMessage(long messageId, Callback<Void> callback) {
            mMessagesMgmt.removeMessage(messageId, callback);
        }

        public void removeMessages(String folder, Callback<Void> callback) {
            mMessagesMgmt.removeMessages(folder, callback);
        }
    }

    public class UserApi {
        public void getFavoritesUsers(Callback<List<FavoriteUser>> callback) {
            mUserMgmt.getFavoritesUsers(callback);
        }

        public void updateFavoriteUser(long id, Callback<Void> callback) {
            mUserMgmt.updateFavoriteUser(id, callback);
        }

        public void getUserStats(long id, Callback<Stats> callback) {
            mUserMgmt.getUserStats(id, callback);
        }

        public void addAvatar(TypedOutput image, Callback<Void> callback) {
            mUserMgmt.addAvatar(image, callback);
        }

        public void deleteAvatar(Callback<Void> callback) {
            mUserMgmt.deleteAvatar(callback);
        }

        public void getUserImages(long id, Callback<List<Image>> callback) {
            mUserMgmt.getUserImages(id, callback);
        }

        public void addUser(User user, Callback<User> callback) {
            mUserMgmt.addUser(user, callback);
        }

        public void deleteUser(String userId, Callback<User> callback) {
            mUserMgmt.deleteUser(userId, callback);
        }

        public void generateHash(User user, Callback<User> callback) {
            mUserMgmt.generateHash(user, callback);
        }

        public void getUser(String userId, Callback<User> callback) {
            mUserMgmt.getUser(userId, callback);
        }

        public void getUser(long userId, Callback<User> callback) {
            mUserMgmt.getUser(userId, callback);
        }

        public void updateUser(String userId, User user, Callback<Void> callback) {
            mUserMgmt.updateUser(userId, user, callback);
        }

        public Void updateUser(String userId, User user) {
            return mUserMgmt.updateUser(userId, user);
        }

        public void getRating(Callback<List<User>> callback) {
            mUserMgmt.getRating(callback);
        }

        public void getWinsList(long id, Callback<List<Contest>> callback) {
            mUserMgmt.getWinsList(id, callback);
        }
    }

    public static class FavoriteUser {
        public long fid;
        public String display_name;
        public String avatar;

        public FavoriteUser(long fid, String display_name, String avatar) {
            this.fid = fid;
            this.display_name = display_name;
            this.avatar = avatar;
        }

        public FavoriteUser() {

        }
    }
}
