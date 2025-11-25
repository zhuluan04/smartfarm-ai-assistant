package com.linjiu.recognize.layout.bottomNav.monitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.linjiu.recognize.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MonitorFragmentBackUP extends Fragment {

    private static final String TAG = "FunctionCenterFragment";

    // 🌐 API接口配置
    private static final String BASE_URL = "http://192.168.38.142:8080";
    private static final String SENSOR_DATA_URL = BASE_URL + "/sensors/latest";
    private static final String AI_ANALYSIS_URL = BASE_URL + "/ai/plant-analysis";

    // 🎯 顶部状态栏控件
    private View systemStatus;
    private TextView sensorCount;
    private ImageView btnRefreshAll;

    // 📸 摄像头相关
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    // 🌡️ 环境监测控件
    private TextView envTempValue, envHumidityValue;
    private TextView lightIntensityValue;

    // 🌿 土壤管理控件
    private TextView soilMoistureValue;
    private ProgressBar soilMoistureProgress;

    // 🤖 AI分析控件
    private TextView growthStatus, growthScore;
    private TextView pestStatus, lastScanTime;

    // 📋 功能卡片控件
    private CardView cardDataAnalysis, cardSystemSettings;
    private Button btnStartMonitoring, btnExportReport;

    // 📊 数据管理
    private Handler handler = new Handler();
    private Runnable autoRefreshRunnable;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    // 📈 传感器数据
    private SensorData currentSensorData;
    private AIAnalysisData currentAIData;

    // 🔧 配置参数
    private boolean isAutoRefreshEnabled = true;
    private int refreshIntervalMs = 10000; // 10秒刷新间隔
    private int onlineSensorCount = 6;
    private int totalSensorCount = 6;

    private boolean isFrontCamera = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 创建布局
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        // 初始化布局
        initViews(view);
        // 设置点击监听
        setupClickListeners();
        // 初始化传感器数据
        initializeData();
        // 开启自动刷新
        startAutoRefresh();

        return view;
    }

    // 初始化布局方法
    private void initViews(View view) {
        // 顶部状态栏
        systemStatus = view.findViewById(R.id.systemStatus);
        sensorCount = view.findViewById(R.id.sensorCount);
        btnRefreshAll = view.findViewById(R.id.btnRefreshAll);

        // 开关控件
//        switchAutoRefresh = view.findViewById(R.id.switchAutoRefresh);
//        switchSaveToGallery = view.findViewById(R.id.switchSaveToGallery);

        // 浮层控件
//        overlayTemperature = view.findViewById(R.id.overlayTemperature);
//        overlayHumidity = view.findViewById(R.id.overlayHumidity);
//        overlayTimestamp = view.findViewById(R.id.overlayTimestamp);

        // 环境监测数据
        envTempValue = view.findViewById(R.id.envTempValue);
        envHumidityValue = view.findViewById(R.id.envHumidityValue);
        lightIntensityValue = view.findViewById(R.id.lightIntensityValue);

        // 土壤管理数据
        soilMoistureValue = view.findViewById(R.id.soilMoistureValue);
        soilMoistureProgress = view.findViewById(R.id.soilMoistureProgress);

        // AI分析数据
        growthStatus = view.findViewById(R.id.growthStatus);
        growthScore = view.findViewById(R.id.growthScore);
        pestStatus = view.findViewById(R.id.pestStatus);
        lastScanTime = view.findViewById(R.id.lastScanTime);

        // 功能卡片
        cardDataAnalysis = view.findViewById(R.id.cardDataAnalysis);
        cardSystemSettings = view.findViewById(R.id.cardSystemSettings);
        btnStartMonitoring = view.findViewById(R.id.btnStartMonitoring);
        btnExportReport = view.findViewById(R.id.btnExportReport);

        // 摄像头
        previewView = view.findViewById(R.id.previewView);
    }

    private void setupClickListeners() {
        // 刷新所有数据
        btnRefreshAll.setOnClickListener(v -> refreshAllData());

        // 功能卡片点击事件
        cardDataAnalysis.setOnClickListener(v -> openDataAnalysis());
        cardSystemSettings.setOnClickListener(v -> openSystemSettings());

        // 快捷操作按钮
        btnStartMonitoring.setOnClickListener(v -> startMonitoring());
        btnExportReport.setOnClickListener(v -> exportReport());
    }

    private void initializeData() {
        // 初始化数据对象
        currentSensorData = new SensorData();
        currentAIData = new AIAnalysisData();

        // 更新传感器状态
        updateSensorStatus();

        // 首次加载数据
        refreshAllData();
    }

    // 📸 权限检查
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "摄像头权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 📸 启动摄像头
    private void startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor();

        ProcessCameraProvider.getInstance(requireContext())
                .addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get();

                        Preview preview = new Preview.Builder().build();
                        preview.setSurfaceProvider(previewView.getSurfaceProvider());

                        imageCapture = new ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build();

                        CameraSelector cameraSelector = isFrontCamera ?
                                CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(
                                this, cameraSelector, preview, imageCapture);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "摄像头启动失败", Toast.LENGTH_SHORT).show();
                    }
                }, ContextCompat.getMainExecutor(requireContext()));
    }

    // 📸 拍照
    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(getContext(), "摄像头未准备就绪", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(requireContext().getExternalFilesDir(null),
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA)
                        .format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "已保存: " + photoFile.getName(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "拍照失败: " + exc.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    // 切换摄像头
//    private void switchCamera() {
//        isFrontCamera = !isFrontCamera;
//        startCamera();
//        Toast.makeText(getContext(),
//                isFrontCamera ? "已切换到前置摄像头" : "已切换到后置摄像头",
//                Toast.LENGTH_SHORT).show();
//    }
//
//    // 切换手电筒
//    private void toggleFlashlight() {
//        // 注意：实际的手电筒控制需要Camera2 API或CameraX的更高级功能
//        isFlashlightOn = !isFlashlightOn;
//        btnFlashlight.setImageResource(isFlashlightOn ?
//                R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off);
//        Toast.makeText(getContext(),
//                isFlashlightOn ? "手电筒已开启" : "手电筒已关闭",
//                Toast.LENGTH_SHORT).show();
//    }

    // 🔄 自动刷新控制
    private void startAutoRefresh() {
        if (!isAutoRefreshEnabled) return;

        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    refreshAllData();
                    handler.postDelayed(this, refreshIntervalMs);
                }
            };
        }
        handler.postDelayed(autoRefreshRunnable, refreshIntervalMs);
        Log.d(TAG, "自动刷新已启动，间隔: " + refreshIntervalMs + "ms");
    }

    private void stopAutoRefresh() {
        if (handler != null && autoRefreshRunnable != null) {
            handler.removeCallbacks(autoRefreshRunnable);
            Log.d(TAG, "自动刷新已停止");
        }
    }

    // 🌐 数据获取方法
    private void refreshAllData() {
        Log.d(TAG, "开始刷新所有数据");
        fetchSensorData();
        fetchAIAnalysisData();
    }

    private void fetchSensorData() {
        Request request = new Request.Builder()
                .url(SENSOR_DATA_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "传感器数据获取失败", e);
                if (getActivity() == null) return;

                requireActivity().runOnUiThread(() -> {
                    updateSystemStatus(false);
                    Toast.makeText(getContext(), "传感器连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (getActivity() == null) return;

                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        updateSystemStatus(false);
                        Toast.makeText(getContext(), "服务器错误: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                    // 解析传感器数据
                    if (jsonObject.has("error")) {
                        final String errorMsg = jsonObject.get("error").getAsString();
                        requireActivity().runOnUiThread(() -> {
                            updateSystemStatus(false);
                            Toast.makeText(getContext(), "数据错误: " + errorMsg, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        parseSensorData(jsonObject);
                        requireActivity().runOnUiThread(() -> {
                            updateSystemStatus(true);
                            updateSensorDisplay();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "数据解析错误", e);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }
        });
    }

    private void fetchAIAnalysisData() {
        // 模拟AI分析请求（实际项目中调用神经网络API）
        handler.postDelayed(() -> {
            if (getActivity() == null) return;

            // 模拟AI分析结果
            simulateAIAnalysis();
            updateAIDisplay();
        }, 1000);
    }

    // 📊 数据解析方法
    private void parseSensorData(JsonObject jsonObject) {
        try {
            // 环境温湿度
            currentSensorData.temperature = jsonObject.has("temperature") ?
                    jsonObject.get("temperature").getAsDouble() : 0.0;
            currentSensorData.humidity = jsonObject.has("humidity") ?
                    jsonObject.get("humidity").getAsDouble() : 0.0;

            // 光照强度 (如果API提供)
            currentSensorData.lightIntensity = jsonObject.has("light_intensity") ?
                    jsonObject.get("light_intensity").getAsInt() : generateMockLightData();

            // 土壤湿度 (如果API提供)
            currentSensorData.soilMoisture = jsonObject.has("soil_moisture") ?
                    jsonObject.get("soil_moisture").getAsDouble() : generateMockSoilData();

            currentSensorData.timestamp = jsonObject.has("timestamp") ?
                    jsonObject.get("timestamp").getAsString() : getCurrentTimestamp();

            Log.d(TAG, "传感器数据解析完成: T=" + currentSensorData.temperature +
                    ", H=" + currentSensorData.humidity +
                    ", L=" + currentSensorData.lightIntensity +
                    ", S=" + currentSensorData.soilMoisture);
        } catch (Exception e) {
            Log.e(TAG, "传感器数据解析错误", e);
        }
    }

    private void simulateAIAnalysis() {
        // 模拟植物生长分析 (实际项目中替换为真实的AI接口调用)
        Random random = new Random();

        // 生长状态评估
        int score = 70 + random.nextInt(25); // 70-95分
        currentAIData.growthScore = score;

        if (score >= 85) {
            currentAIData.growthStatus = "健康生长";
        } else if (score >= 70) {
            currentAIData.growthStatus = "良好";
        } else {
            currentAIData.growthStatus = "需要关注";
        }

        // 病虫害检测
        boolean hasPest = random.nextBoolean() && random.nextFloat() < 0.1; // 10%概率有问题
        currentAIData.pestDetected = hasPest;
        currentAIData.pestStatus = hasPest ? "发现异常" : "未发现异常";

        currentAIData.lastScanTime = getCurrentTimestamp();

        Log.d(TAG, "AI分析完成: 评分=" + score + ", 状态=" + currentAIData.growthStatus +
                ", 病虫害=" + currentAIData.pestStatus);
    }

    // 🎨 UI更新方法
    private void updateSystemStatus(boolean isOnline) {
        systemStatus.setBackgroundResource(
                isOnline ? R.drawable.circle_green : R.drawable.circle_red
        );

        if (isOnline) {
            onlineSensorCount = totalSensorCount;
        } else {
            onlineSensorCount = Math.max(0, totalSensorCount - 2);
        }
        updateSensorStatus();
    }

    private void updateSensorStatus() {
        sensorCount.setText(String.format("传感器: %d/%d 在线", onlineSensorCount, totalSensorCount));
    }

    private void updateSensorDisplay() {
        // 环境温湿度显示
        envTempValue.setText(String.format("%.1f°C", currentSensorData.temperature));
        envHumidityValue.setText(String.format("%.0f%%", currentSensorData.humidity));

        // 光照强度显示
        String lightStatus = getLightStatus(currentSensorData.lightIntensity);
        lightIntensityValue.setText(String.format("%,d Lux", currentSensorData.lightIntensity));

        // 土壤湿度显示
        soilMoistureValue.setText(String.format("%.0f%%", currentSensorData.soilMoisture));
        soilMoistureProgress.setProgress((int) currentSensorData.soilMoisture);
    }

    private void updateAIDisplay() {
        // 植物生长分析显示
        growthStatus.setText(currentAIData.growthStatus);
        growthScore.setText(String.format("评分: %d/100", currentAIData.growthScore));

        // 设置评分颜色
        int scoreColor = getScoreColor(currentAIData.growthScore);
        growthStatus.setTextColor(ContextCompat.getColor(requireContext(), scoreColor));

        // 病虫害检测显示
        pestStatus.setText(currentAIData.pestStatus);
        lastScanTime.setText(formatScanTime(currentAIData.lastScanTime));

        // 设置病虫害状态颜色
        int pestColor = currentAIData.pestDetected ? R.color.status_error : R.color.status_success;
        pestStatus.setTextColor(ContextCompat.getColor(requireContext(), pestColor));
    }

    // 🔧 功能方法
    private void openDataAnalysis() {
        Toast.makeText(getContext(), "正在打开数据分析页面...", Toast.LENGTH_SHORT).show();
        // TODO: 启动数据分析Activity
    }

    private void openSystemSettings() {
        Toast.makeText(getContext(), "正在打开系统设置页面...", Toast.LENGTH_SHORT).show();
        // TODO: 启动系统设置Activity
    }

    private void startMonitoring() {
        if (!isAutoRefreshEnabled) {
            isAutoRefreshEnabled = true;
            startAutoRefresh();
            btnStartMonitoring.setText("停止监测");
            btnStartMonitoring.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.status_warning));
            Toast.makeText(getContext(), "开始实时监测", Toast.LENGTH_SHORT).show();
        } else {
            isAutoRefreshEnabled = false;
            stopAutoRefresh();
            btnStartMonitoring.setText("开始监测");
            btnStartMonitoring.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.status_success));
            Toast.makeText(getContext(), "监测已停止", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportReport() {
        Toast.makeText(getContext(), "正在生成监测报告...", Toast.LENGTH_SHORT).show();

        // 模拟报告生成
        handler.postDelayed(() -> {
            if (getContext() != null) {
                String reportName = "监测报告_" + new SimpleDateFormat("yyyyMMdd_HHmm",
                        Locale.CHINA).format(new Date()) + ".pdf";
                Toast.makeText(getContext(), "报告已生成: " + reportName, Toast.LENGTH_LONG).show();
            }
        }, 2000);
    }

    // 🛠️ 辅助方法
    private int generateMockLightData() {
        // 生成模拟光照数据 (5000-25000 Lux)
        Random random = new Random();
        return 5000 + random.nextInt(20000);
    }

    private double generateMockSoilData() {
        // 生成模拟土壤湿度数据 (20%-80%)
        Random random = new Random();
        return 20 + random.nextDouble() * 60;
    }

    private String getLightStatus(int lightIntensity) {
        if (lightIntensity < 10000) return "不足";
        else if (lightIntensity > 20000) return "过强";
        else return "适宜";
    }

    private int getScoreColor(int score) {
        if (score >= 85) return R.color.status_success;
        else if (score >= 70) return R.color.status_warning;
        else return R.color.status_error;
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date());
    }

    private String formatScanTime(String timestamp) {
        return timestamp + " 检测";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
        Log.d(TAG, "Fragment销毁，清理资源");
    }

    // 📦 数据类定义
    private static class SensorData {
        double temperature = 0.0;
        double humidity = 0.0;
        int lightIntensity = 0;
        double soilMoisture = 0.0;
        String timestamp = "";
    }

    private static class AIAnalysisData {
        String growthStatus = "分析中";
        int growthScore = 0;
        boolean pestDetected = false;
        String pestStatus = "检测中";
        String lastScanTime = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

}