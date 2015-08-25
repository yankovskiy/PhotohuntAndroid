package ru.neverdark.photohunt.rest.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import ru.neverdark.photohunt.rest.data.Achievement;
import ru.neverdark.photohunt.rest.data.AchievementUser;

public interface AchievementsApi {
    @GET("/achievements")
    public void getAchievements(Callback<List<Achievement>> callback);

    @GET("/achievements/user/{id}")
    public void getUserAchievements(@Path("id") long userId, Callback<List<Achievement>> callback);

    @GET("/achievements/users/{achievement}")
    public void getAchievementUserList(@Path("achievement") String achievement, Callback<List<AchievementUser>> callback);
}
