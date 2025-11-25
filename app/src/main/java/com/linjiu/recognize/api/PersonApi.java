package com.linjiu.recognize.api;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.linjiu.recognize.config.HttpUrlConnectConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PersonApi {
    public static final String SYSTEM_STATUS_URL = HttpUrlConnectConfig.APP_URL + "/system/status";
    public static final String USER_PROFILE_URL = HttpUrlConnectConfig.APP_URL + "/user/profile";

}
