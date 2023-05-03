package com.cst.vingcard.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitAPICollection {

    //GET method-----
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/door")
    Call<String> getAllDoor(@Header("Authorization") String token, @Query("details") String details, @Query("doorGroup") String doorGroup);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/door")
    Call<String> getAllDoorNoDoorGroup(@Header("Authorization") String token, @Query("details") String details);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/door/{door_number}")
    Call<String> getSingleDoor(@Header("Authorization") String token, @Path("door_number") String doorNumber);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/card")
    Call<String> getDoorCard(@Header("Authorization") String token, @Query("guestDoor") String guestDoor, @Query("validTime") String validTime);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/card/{door_number}")
    Call<String> getShowCard(@Header("Authorization") String token, @Path("door_number") String doorNumber);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @GET("api/v1/card")
    Call<String> getSNCard(@Header("Authorization") String token, @Query("serialNumber") String serialNumber);


    //POST method-----
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("api/v1/card")
    Call<String> encodeCard(@Header("Authorization") String token, @Query("override") String override, @Query("pendingImage") String pendingImage, @Query("sendOnline") String sendOnline, @Body String body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("api/v1/card")
    Call<String> encodeJoinerCard(@Header("Authorization") String token, @Query("autoJoin") String autoJoin, @Query("override") String override, @Query("pendingImage") String pendingImage, @Query("sendOnline") String sendOnline, @Body String body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("assa-abloy/card")
    Call<String> getSNCards(@Header("Client-Id") String clientId, @Header("Client-Secret") String clientSecret, @Body String body);







    //PUT method-----

}
