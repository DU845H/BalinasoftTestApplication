package com.egor_dubovik.testapplication.api.service;

import com.egor_dubovik.testapplication.api.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserClient {
    @POST("/api/account/signup")
    Call<User> signUpUser(@Body User user);
}
