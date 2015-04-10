package ru.neverdark.photohunt.rest.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import ru.neverdark.photohunt.rest.data.Item;
import ru.neverdark.photohunt.rest.data.ShopData;

public interface ShopApi {
    @GET("/shop")
    public void getShop(Callback<ShopData> callback);

    @GET("/shop/my")
    public void getMyItems(Callback<List<Item>> callback);

    @POST("/shop/{id}")
    public void buyItem(@Path("id") long itemId, Callback<Void> callback);

    @PUT("/shop/my/{id}")
    public void useItem(@Path("id") long itemId, Callback<Void> callback);
}
