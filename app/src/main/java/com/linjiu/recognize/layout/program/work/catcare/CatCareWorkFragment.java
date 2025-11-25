package com.linjiu.recognize.layout.program.work.catcare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 顾猫模块 Fragment
 * 功能：
 * 1. 通过 Java 后端访问 ESP32 摄像头
 * 2. 显示摄像头画面（WebView 加载后端提供的图片流）
 * 3. 语音识别并通过 WebSocket 发送指令到后端
 */
public class CatCareWorkFragment extends Fragment {

    private static final String TAG = "CatCareWorkFragment";

    // ===== 后端服务器配置 =====
    private static final String BACKEND_HOST = "123.249.3.233:8088";
    // 显示的esp32设备Id
    private static final String ESP32_ID = "esp32_1";
    // app设备id
    private static final String APP_ID = "app_1";

    // webSocket 地址, app的地址
    private static final String WS_URL = "ws://" + BACKEND_HOST + "/ws/app";

    private static final String MJPEG_URL = "http://" + BACKEND_HOST + "/camera/" + ESP32_ID + "/mjpeg";

    private TextView tvRecognizedText;
    private FloatingActionButton fabMic;

    // WebSocket 相关
    private OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private boolean isWebSocketConnected = false;

    // 音频采集录制
    private static final int SAMPLE_RATE = 16000; // 16kHz
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    WebView webView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceRecognition();
                } else {
                    Toast.makeText(getContext(), "需要麦克风权限才能语音控制", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cat_cate_work_fragment, container, false);

        webView = view.findViewById(R.id.webView);
        tvRecognizedText = view.findViewById(R.id.tvRecognizedText);
        fabMic = view.findViewById(R.id.fabMic);

        view.findViewById(R.id.btnFeeding).setOnClickListener(v -> openFragment(new CatCareFeedFragment()));
        view.findViewById(R.id.btnPlay).setOnClickListener(v -> openFragment(new CatCarePlayFragment()));
        view.findViewById(R.id.btnHealth).setOnClickListener(v -> openFragment(new CatCareHealthFragment()));

        setupMicButton();

        // ✅ 启动 MJPEG
//        setupMjpeg();
        setupWebViewForMjpeg();

        // ✅ 启动 WebSocket
        connectToBackend();
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebViewForMjpeg() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadsImagesAutomatically(true);

        String html = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            </head>
            <body style="margin:0; background:#000; display:flex; justify-content:center; align-items:center; height:100vh; overflow:hidden;">
                <img src="%s" style="max-width:100%%; max-height:100%%; object-fit:contain;" />
            </body>
            </html>
            """.formatted(MJPEG_URL);
        }

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    /**
     * 连接到后端 WebSocket
     */
    private void connectToBackend() {
        okHttpClient = new OkHttpClient();
        // 添加设备Id参数
        String wsUrlWithId = WS_URL + "?id=" + APP_ID;
        Request request = new Request.Builder().url(wsUrlWithId).build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                isWebSocketConnected = true;
                Log.d(TAG, "✅ WebSocket 已连接到后端: " + wsUrlWithId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "已连接到服务器", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.d(TAG, "📩 收到后端消息: " + text);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "ESP32: " + text, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                isWebSocketConnected = false;
                Log.e(TAG, "❌ WebSocket 连接失败: " + wsUrlWithId, t);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "服务器连接失败", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                isWebSocketConnected = false;
                Log.d(TAG, "🔌 WebSocket 已关闭: " + reason);
            }
        });
    }

    /**
     * 切换页面
     */
    private void openFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * 语音识别按钮逻辑
     * 短按：启动语音识别 -> 发送文本指令
     * 长按：启动音频流
     * 音频流：长按按钮启动音频流，松开按钮停止音频流
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupMicButton() {
        fabMic.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    // 长按：启动音频流
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    } else {
                        startAudioStreaming();
                    }
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时，延迟 500ms 触发长按
                        handler.postDelayed(longPressRunnable, 500);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable); // 取消长按

                        // 如果不是长按（即短按），且当前没在录音，则执行语音识别
                        if (!isRecording) {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                            } else {
                                startVoiceRecognition(); // 文本识别
                            }
                        } else {
                            // 如果正在录音，短按则停止
                            stopAudioStreaming();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    /**
     * 启动语音识别
     */
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA);

        try {
            startActivityForResult(intent, 1001);
        } catch (Exception e) {
            Toast.makeText(getContext(), "设备不支持语音识别", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 语音识别回调
     */
    @Override
    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                tvRecognizedText.setText("你说的是: " + spokenText);

                if (isWebSocketConnected && webSocket != null) {
                    // 发送 JSON 格式指令到后端
                    String command = "{\"type\":\"command\",\"deviceId\":\"" + ESP32_ID +
                            "\",\"command\":\"" + spokenText + "\"}";
                    webSocket.send(command);
                    Log.d(TAG, "📤 已发送指令到后端: " + command);
                    Toast.makeText(getContext(), "指令已发送: " + spokenText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "未连接到服务器，无法发送指令", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Fragment destroyed");
        }
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
        }
    }

    // 录音
    private void startAudioStreaming() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }

        if (isRecording) return;

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE * 4
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Toast.makeText(getContext(), "麦克风初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        audioRecord.startRecording();

        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0 && isWebSocketConnected && webSocket != null) {
                    // 发送原始 PCM 数据（注意：WebSocket 通常传文本，需用 Base64 或二进制帧）
                    // OkHttp WebSocket 支持发送 ByteString（二进制）
                    okio.ByteString byteString = okio.ByteString.of(buffer, 0, read);
                    webSocket.send(byteString);
                }
            }
        });

        recordingThread.start();
        fabMic.setImageResource(R.drawable.ic_mic_off); // 切换图标表示正在录音
    }

    private void stopAudioStreaming() {
        isRecording = false;
        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        fabMic.setImageResource(R.drawable.ic_mic); // 恢复图标
    }
}