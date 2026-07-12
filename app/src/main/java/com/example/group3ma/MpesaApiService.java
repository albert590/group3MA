package com.example.group3ma;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

interface MpesaApiService {
    @GET("oauth/v1/generate?grant_type=client_credentials")
    Call<AccessTokenResponse> getAccessToken(@Header("Authorization") String authHeader);

    @POST("mpesa/stkpush/v1/processrequest")
    Call<PaymentResponse> initiateStkPush(@Header("Authorization") String authHeader, @Body PaymentRequest paymentRequest);
}
