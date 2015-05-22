package ru.neverdark.photohunt.rest.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import ru.neverdark.photohunt.rest.data.Comment;
import ru.neverdark.photohunt.rest.data.UnreadComment;

public interface CommentApi {
    @GET("/image/{id}/comments")
    public void getImageComments(@Path("id") long imageId, Callback<List<Comment>> callback);

    @GET("/comments/unread")
    public void getUnreadComments(Callback<List<UnreadComment>> callback);

    @Multipart
    @POST("/image/{id}/comments")
    public void addImageComments(@Path("id") long imageId, @Part("comment") String comment, Callback<Void> callback);

    @DELETE("/comments/{id}")
    public void removeComment(@Path("id") long commentId, Callback<Void> callback);
}
