package com.example.fyp_chatapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient { //用于创建Retrofit实例，Retrofit是一个REST Client，用于Android和Java的类型安全HTTP客户端

    private static Retrofit retrofit = null;

    public static  Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/fcm/")
                    .addConverterFactory(ScalarsConverterFactory.create())  //用于将ResponseBody转换为String
                    .build();
        }
        return retrofit;
    }
}
