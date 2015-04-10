package ru.neverdark.photohunt.rest.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import ru.neverdark.photohunt.rest.data.Message;
import ru.neverdark.photohunt.rest.data.Messages;

public interface MessagesApi {
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
