package com.linjiu.recognize.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class EspWebSocketClient {
    private static final String TAG = "EspWebSocketClient";
    private OkHttpClient client;
    private WebSocket webSocket;
    private boolean isConnected = false;

    public void connect(String wsUrl) {
        client = new OkHttpClient();
        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                Log.d(TAG, "✅ 已连接到 ESP32: " + wsUrl);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "📩 收到消息: " + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                Log.e(TAG, "❌ WebSocket 连接失败", t);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                Log.d(TAG, "🔌 WebSocket 已关闭: " + reason);
            }
        });
    }

    public void send(String msg) {
        if (webSocket != null && isConnected) {
            webSocket.send(msg);
            Log.d(TAG, "📤 发送消息: " + msg);
        } else {
            Log.w(TAG, "⚠️ 未连接，消息未发送");
        }
    }

    public void close() {
        if (webSocket != null) webSocket.close(1000, "App closed");
    }
}
