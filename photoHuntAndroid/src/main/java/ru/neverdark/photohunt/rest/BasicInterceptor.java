package ru.neverdark.photohunt.rest;

import retrofit.RequestInterceptor;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.SingletonHelper;

/**
 * Перехватчик для отправки http-заголовков (авторизация, версия)
 */
public class BasicInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade requestFacade) {
        Log.enter();
        requestFacade.addHeader("User-Agent", SingletonHelper.getInstance().getUserAgent());
        requestFacade.addHeader("Content-Version", String.valueOf(SingletonHelper.getInstance().getVersion()));
    }
}
