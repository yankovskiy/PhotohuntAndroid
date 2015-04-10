package ru.neverdark.photohunt.rest.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedOutput;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.FavoriteUser;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.rest.data.Stats;
import ru.neverdark.photohunt.rest.data.User;

public interface UserApi {
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
