package com.example.ids.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.example.ids.data.session.SessionEventBus;

import java.io.IOException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        // BEFORE REQUEST
        if (isTokenExpired()) {
            logout();
            throw new IOException("SESSION_EXPIRED");
        }

        // Proceed with request
        SharedPreferences prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("session_token", null);

        Request.Builder builder = chain.request().newBuilder();
        if(token != null)
            builder.addHeader("Authorization", "Bearer " + token);
        Request request = builder.build();
        Response response = chain.proceed(request);

        // BACKEND INVALIDATED TOKEN
        if (response.code() == 401) {
            logout();

            // Close response body to avoid leaks
            //response.close();

            //throw new IOException("SESSION_EXPIRED");
        }

        // AFTER REQUEST
        return response;
    }

    private boolean isTokenExpired() {
        SharedPreferences prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("session_token", null);
        if (token == null) return false;

        try {
            JWT jwt = new JWT(token);
            Date expiresAt = jwt.getExpiresAt();
            return expiresAt == null || expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private void logout() {
        Log.d("AuthInterceptor", "Logout");
        SharedPreferences prefs =
                appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        if (prefs == null){
            return;
        }

        String token = prefs.getString("session_token", null);
        String user_id = prefs.getString("user_id", null);
        String is_admin = prefs.getString("is_admin", null);
        boolean activate = false;
        SharedPreferences.Editor editor = prefs.edit();
        if(token!=null && !token.isEmpty()) {
            editor = editor.remove("session_token");
            activate = true;
        }
        if(user_id!=null && !user_id.isEmpty()) {
            editor = editor.remove("user_id");
            activate = true;
        }
        if(is_admin!=null && !is_admin.isEmpty()){
            editor = editor.remove("is_admin");
            activate = true;
        }
        editor.apply();

        // Notify observers
        if (activate)
            SessionEventBus.sessionExpired.postValue(true);
    }
}
