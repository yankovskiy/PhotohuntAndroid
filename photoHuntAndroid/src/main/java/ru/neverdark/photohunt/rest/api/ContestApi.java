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
import ru.neverdark.photohunt.rest.data.ContestDetail;
import ru.neverdark.photohunt.rest.data.Exif;
import ru.neverdark.photohunt.rest.data.Image;

public interface ContestApi {
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
                                  @Part("image") TypedOutput image,
                                  @Part("exif") Exif exif,
                                  @Part("description") String description,
                                  Callback<Void> callback);

    @PUT("/contest/{id}")
    public void voteForContest(@Path("id") long id, @Body Image image, Callback<Void> callback);
}
