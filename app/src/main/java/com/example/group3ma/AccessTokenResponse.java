package com.example.group3ma;

import com.google.gson.annotations.SerializedName;

public class AccessTokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("expires_in")
    public String expiresIn;
}
