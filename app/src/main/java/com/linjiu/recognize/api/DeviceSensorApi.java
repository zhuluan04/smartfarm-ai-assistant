package com.linjiu.recognize.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linjiu.recognize.domain.sensor.LightSensorData;
import com.linjiu.recognize.domain.sensor.ShtSensorData;
import com.linjiu.recognize.domain.sensor.SoilSensorData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceSensorApi {

    private static final String BASE_URL = "http://192.168.1.100:8080"; // 替换为你的服务器地址
    private static final String TAG = "DeviceSensorApi";

    private final OkHttpClient client;
    private final Gson gson;

    public DeviceSensorApi() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    // ========== DHT 温湿度 ==========
    public List<ShtSensorData> fetchDhtRealSensorData() {
        String url = BASE_URL + "/device/dht";
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<ShtSensorData>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        } catch (IOException e) {
            Log.e(TAG, "DHT request failed", e);
        }
        return Collections.emptyList();
    }

    // ========== 土壤湿度 ==========
    public List<SoilSensorData> fetchSoilSensorData() {
        String url = BASE_URL + "/device/soil";
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<SoilSensorData>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        } catch (IOException e) {
            Log.e(TAG, "Soil request failed", e);
        }
        return Collections.emptyList();
    }

    // ========== 光照强度 ==========
    public List<LightSensorData> fetchLightSensorData() {
        String url = BASE_URL + "/device/light";
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<LightSensorData>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        } catch (IOException e) {
            Log.e(TAG, "Light request failed", e);
        }
        return Collections.emptyList();
    }

    /**
     * 同步方法：获取 DHT 温湿度数据（必须在子线程中调用！）
     *
     * @return 传感器数据列表，失败时返回空列表
     */
    public List<ShtSensorData> fetchDhtSensorData() {
        String url = BASE_URL + "/device/dht";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute(); // 同步执行
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Type listType = new TypeToken<List<ShtSensorData>>(){}.getType();
                return gson.fromJson(json, listType);
            } else {
                Log.e(TAG, "Response not successful: " + response.code());
                return Collections.emptyList();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network request failed", e);
            return Collections.emptyList();
        }
    }

}