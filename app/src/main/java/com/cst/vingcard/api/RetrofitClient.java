package com.cst.vingcard.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    public static Retrofit getRetrofitClient(String serverUrl) {
        String endPointUrl;

        if (!serverUrl.trim().endsWith("/")) {
            endPointUrl = serverUrl.trim() + "/";
        } else {
            endPointUrl = serverUrl.trim();
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(endPointUrl)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

}
