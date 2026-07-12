package com.example.group3ma;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("ResponseCode")
    private String responseCode;
    @SerializedName("ResponseDescription")
    private String responseDescription;
    @SerializedName("MerchantRequestID")
    private String merchantRequestID;
    @SerializedName("CheckoutRequestID")
    private String checkoutRequestID;
    @SerializedName("CustomerMessage")
    private String customerMessage;

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public String getMerchantRequestID() {
        return merchantRequestID;
    }

    public String getCheckoutRequestID() {
        return checkoutRequestID;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }
}
