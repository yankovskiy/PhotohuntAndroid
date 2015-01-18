package ru.neverdark.photohunt.rest;

import android.util.Base64;
import retrofit.RequestInterceptor;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.SingletonHelper;

/**
 * Перехватчик для отправки http-заголовков (авторизация, версия)
 */
public class AuthInterceptor extends BasicInterceptor {
    private String mAuthValue;
    
    @Override
    public void intercept(RequestFacade requestFacade) {
        Log.enter();
        super.intercept(requestFacade);

        if (mAuthValue != null && mAuthValue.length() > 0) {
            requestFacade.addHeader("Authorization", mAuthValue);
        }
    }
    
    /**
     * Устанавливает имя пользователя и пароль для авторизации
     * @param user имя пользователя
     * @param password пароль пользователя
     */
    public void setAuthData(String user, String password) {
        String userAndPass = user.concat(":").concat(password);
        mAuthValue = "Basic ".concat(Base64.encodeToString(userAndPass.getBytes(), Base64.NO_WRAP));
    }

}
