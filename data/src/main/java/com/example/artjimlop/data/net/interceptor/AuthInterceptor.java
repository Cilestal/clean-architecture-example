/**
 * Copyright (C) 2016 Arturo Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.artjimlop.data.net.interceptor;

import android.util.Log;
import com.example.artjimlop.data.net.ApiConstants;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {

    public static final String HASH_ALGORITHM = "MD5";
    private static final String TAG = AuthInterceptor.class.getName();
    private String publicKey;
    private String privateKey;

    @Inject
    public AuthInterceptor(@Named("public_key") String publicKey, @Named("private_key") String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();

        long timeStamp = System.currentTimeMillis();
        String hash = digest(HASH_ALGORITHM, String.valueOf(timeStamp), privateKey, publicKey);

        HttpUrl.Builder urlBuilder = url.newBuilder();
        urlBuilder.addEncodedQueryParameter(ApiConstants.QUERY_PARAM_TS, String.valueOf(timeStamp));
        urlBuilder.addEncodedQueryParameter(ApiConstants.QUERY_PARAM_API_KEY, publicKey);
        urlBuilder.addEncodedQueryParameter(ApiConstants.QUERY_PARAM_HASH, hash);

        url = urlBuilder.build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }

    private String digest(String algorithm, String... params) {
        String message = "";
        for (String param : params) {
            message += param;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(message.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }

        return "";
    }
}
