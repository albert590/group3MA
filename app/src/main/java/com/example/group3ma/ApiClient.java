package com.example.group3ma;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "https://sandbox.safaricom.co.ke/";
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Force TLS 1.2 and include COMPATIBLE_TLS for maximum compatibility with Daraja API
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        // Using .header("Connection", "close") prevents "SSL peer shut down incorrectly"
                        // which often happens when the server closes a pooled connection prematurely.
                        // .header() replaces any existing Connection header, ensuring only "close" is sent.
                        Request request = chain.request().newBuilder()
                                .header("Connection", "close")
                                .build();
                        return chain.proceed(request);
                    })
                    // Force HTTP/1.1 as Safaricom's sandbox can have issues with HTTP/2 handshakes
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    // Include both the forced TLS 1.2 spec and COMPATIBLE_TLS
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.COMPATIBLE_TLS))
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
