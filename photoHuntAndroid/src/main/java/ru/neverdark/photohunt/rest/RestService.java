package ru.neverdark.photohunt.rest;

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
import ru.neverdark.photohunt.utils.Log;

public class RestService {
    public static class Image {
        public long id;
        public long contest_id;
        public long user_id;
        public String subject;
        public String display_name;
        public int vote_count;

        @Override
        public String toString() {
            return String
                    .format(Locale.US,
                            "Image id = %d, contest_id = %d, user_id = %d, subject = %s, display_name = %s, vote_count = %d",
                            id, contest_id, user_id, subject, display_name, vote_count);
        }
    }

    public static class Contest {
        public final static int STATUS_CLOSE = 0;
        public final static int STATUS_OPEN = 1;
        public final static int STATUS_VOTES = 2;

        public long id;
        public String subject;
        public int rewards;
        public String close_date;
        public int status;
        public long user_id;
        public String display_name;
        public int works;

        @Override
        public String toString() {
            return String
                    .format(Locale.US,
                            "Contest id = %d, subject = %s, rewards = %d, close_date = %s, status = %d, user_id = %d, display_name = %s",
                            id, subject, rewards, close_date, status, user_id, display_name);
        }
    }

    public static class ContestDetail {
        public Contest contest;
        public List<Image> images;
        public int votes;
    }

    public class ContestApi {
        public void getContests(Callback<List<Contest>> callback) {
            mContestMgmt.getContests(callback);
        }

        public void getLastContest(Callback<Contest> callback) {
            mContestMgmt.getLastContest(callback);
        }

        public void getContestDetails(long id, Callback<ContestDetail> callback) {
            mContestMgmt.getContestDetails(id, callback);
        }
        
        public void addImageToContest(long id, String subject, TypedOutput image, Callback<Void> callback) {
            Log.enter();
            mContestMgmt.addImageToContest(id, subject, image, callback);
        }
        
        public void voteForContest(long id, Image image, Callback<Void> callback) {
            mContestMgmt.voteForContest(id, image, callback);
        }
    }

    private interface ContestMgmt {
        @GET("/contests")
        public void getContests(Callback<List<Contest>> callback);

        @GET("/contest")
        public void getLastContest(Callback<Contest> callback);

        @GET("/contest/{id}")
        public void getContestDetails(@Path("id") long id, Callback<ContestDetail> callback);

        @Multipart
        @POST("/contest/{id}")
        public void addImageToContest(@Path("id") long id, @Part("subject") String subject,
                @Part("image") TypedOutput image, Callback<Void> callback);
        
        @PUT("/contest/{id}")
        public void voteForContest(@Path("id") long id, @Body Image image, Callback<Void> callback);
    }

    public static class User {
        public long id;
        public String user_id;
        public String display_name;
        public String password;
        public int balance;
        public int vote_count;
        public String hash;
    }

    public class UserApi {
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

        public void updateUser(String userId, User user, Callback<User> callback) {
            mUserMgmt.updateUser(userId, user, callback);
        }
        
        public void getRating(Callback<List<User>> callback) {
            mUserMgmt.getRating(callback);
        }
    }

    private interface UserMgmt {
        @POST("/user")
        public void addUser(@Body User user, Callback<User> callback);

        @DELETE("/user/{id}")
        public void deleteUser(@Path("id") String userId, Callback<User> callback);

        @PUT("/reset")
        public void generateHash(@Body User user, Callback<User> callback);

        @GET("/user/{id}")
        public void getUser(@Path("id") String userId, Callback<User> callback);

        @PUT("/user/{id}")
        public void updateUser(@Path("id") String userId, @Body User user, Callback<User> callback);
        
        @GET("/user")
        public void getRating(Callback<List<User>> callback);
    }

    public final static String REST_URL = "http://192.168.1.3/api.tim-sw.com";
    //public final static String REST_URL = "http://api.tim-sw.com";

    private final RestAdapter mRestAdapter;

    private final UserMgmt mUserMgmt;

    private final ContestMgmt mContestMgmt;

    private final UserApi mUserApi;

    private final ContestApi mContestApi;

    public RestService() {
        BasicInterceptor interceptor = new BasicInterceptor();

        mContestApi = new ContestApi();
        mUserApi = new UserApi();
        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(interceptor).setEndpoint(REST_URL).build();
        mUserMgmt = mRestAdapter.create(UserMgmt.class);
        mContestMgmt = mRestAdapter.create(ContestMgmt.class);
    }

    public RestService(String user, String password) {
        AuthInterceptor auth = new AuthInterceptor();
        auth.setAuthData(user, password);

        mContestApi = new ContestApi();
        mUserApi = new UserApi();
        mRestAdapter = new RestAdapter.Builder().setRequestInterceptor(auth).setEndpoint(REST_URL)
                .build();
        mUserMgmt = mRestAdapter.create(UserMgmt.class);
        mContestMgmt = mRestAdapter.create(ContestMgmt.class);
    }

    public ContestApi getContestApi() {
        return mContestApi;
    }

    public UserApi getUserApi() {
        return mUserApi;
    }

}
