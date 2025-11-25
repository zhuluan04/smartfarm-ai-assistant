package com.linjiu.recognize.layout.bottomNav.monitor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.linjiu.recognize.R;
import com.linjiu.recognize.config.HttpUrlConnectConfig;
import com.linjiu.recognize.domain.ai.AIAnalysisData;
import com.linjiu.recognize.domain.sensor.SensorData;
import com.linjiu.recognize.layout.bottomNav.module.PlantGrowthFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.devices.LightIntensityFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.devices.NutrientDetailFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.devices.SoilMoistureFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.devices.TempHumidityFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.module.DataAnalysisFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.module.PestDetectionFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.module.PlantGrowthAnalysisFragment;
import com.linjiu.recognize.layout.bottomNav.monitor.module.SystemSettingsFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 监控界面 Fragment 类
 * 负责显示来自 ESP32 的摄像头视频流和传感器数据，并提供 AI 分析结果展示
 */
public class MonitorFragment extends Fragment {

    // 日志标签常量
    private static final String TAG = "MonitorFragment";

    // ESP32 摄像头视频流地址
    private static final String ESP32_STREAM_URL = "http://192.168.120.126/";

    // 控件引用声明（顶部状态栏）
    private View systemStatus;          // 系统在线状态指示器
    private TextView sensorCount;       // 显示当前在线传感器数量
    private ImageView btnRefreshAll;    // 全部刷新按钮

    // 控件引用声明（视频监控区域）
    private WebView webViewCamera;      // 展示ESP32摄像头视频流的WebView

    // 控件引用声明（环境监测模块）
    private TextView envTempValue;      // 温度值显示文本
    private TextView envHumidityValue;  // 湿度值显示文本
    private TextView lightIntensityValue;// 光照强度显示文本

    // 控件引用声明（土壤管理模块）
    private TextView soilMoistureValue; // 土壤湿度百分比数值
    private ProgressBar soilMoistureProgress; // 土壤湿度进度条

    // 控件引用声明（AI分析模块）
    private TextView growthStatus;      // 植物生长状况描述
    private TextView growthScore;       // 生长评分显示
    private TextView pestStatus;        // 害虫检测状态
    private TextView lastScanTime;      // 上次扫描时间戳

    // 控件引用声明（功能卡片）
    private CardView cardDataAnalysis;   // 数据分析功能卡片
    private CardView cardSystemSettings; // 系统设置功能卡片
    private Button btnStartMonitoring;   // 开始/停止监测按钮
    private Button btnExportReport;      // 报告导出按钮

    // 新增：传感器详情卡片
    private CardView tempHumidityCard;   // 温湿度详情卡片
    private CardView lightIntensityCard; // 光照强度详情卡片
    private CardView soilMoistureCard;   // 土壤湿度详情卡片
    private CardView nutrientsCard;      // 土壤养分详情卡片
    private CardView plantGrowthCard;    // 植物生长详情卡片
    private CardView pestDetectionCard;  // 病虫害检测详情卡片

    // 数据管理和通信组件
    private Handler handler = new Handler(); // 处理异步任务的消息队列
    private Runnable autoRefreshRunnable;    // 自动刷新任务
    private OkHttpClient client = new OkHttpClient(); // HTTP客户端实例
    private Gson gson = new Gson();         // JSON解析工具

    // 当前获取的数据对象
    private SensorData currentSensorData;   // 存储最新的传感器数据
    private AIAnalysisData currentAIData;   // 存储AI分析结果数据

    // 刷新控制参数
    private boolean isAutoRefreshEnabled = true; // 是否启用自动刷新
    private int refreshIntervalMs = 10000;       // 刷新间隔（毫秒）
    private int onlineSensorCount = 6;           // 在线传感器计数
    private int totalSensorCount = 6;            // 总共的传感器数量

    /**
     * 创建并返回与Fragment关联的视图层次结构
     *
     * @param inflater           用于实例化fragment视图的LayoutInflater对象
     * @param container          如果非空，则是包含此fragment的父ViewGroup
     * @param savedInstanceState 如果此fragment之前被创建过，则恢复其状态
     * @return 返回此fragment的UI布局
     */
    @Nullable
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 加载fragment布局文件
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        // 初始化所有控件引用
        initViews(view);
        // 设置WebView配置
        setupWebView();
        // 绑定点击事件监听器
        setupClickListeners();
        // 初始化初始数据
        initializeData();
        // 启动定时刷新机制
        // startAutoRefresh();

        return view;
    }

    /**
     * 初始化所有视图控件的引用
     *
     * @param view Fragment根视图
     */
    private void initViews(View view) {
        systemStatus = view.findViewById(R.id.systemStatus);
        sensorCount = view.findViewById(R.id.sensorCount);
        btnRefreshAll = view.findViewById(R.id.btnRefreshAll);

        webViewCamera = view.findViewById(R.id.webViewCamera);

        WebSettings settings = webViewCamera.getSettings();
        settings.setJavaScriptEnabled(true); // 启用JavaScript支持
        settings.setLoadWithOverviewMode(true); // 页面缩放至适合屏幕大小
        settings.setUseWideViewPort(true); // 使用宽视口模式
        webViewCamera.setInitialScale(1); // 初始缩放比例为1

        envTempValue = view.findViewById(R.id.envTempValue);
        envHumidityValue = view.findViewById(R.id.envHumidityValue);
        lightIntensityValue = view.findViewById(R.id.lightIntensityValue);

        soilMoistureValue = view.findViewById(R.id.soilMoistureValue);
        soilMoistureProgress = view.findViewById(R.id.soilMoistureProgress);

        growthStatus = view.findViewById(R.id.growthStatus);
        growthScore = view.findViewById(R.id.growthScore);
        pestStatus = view.findViewById(R.id.pestStatus);
        lastScanTime = view.findViewById(R.id.lastScanTime);

        cardDataAnalysis = view.findViewById(R.id.cardDataAnalysis);
        cardSystemSettings = view.findViewById(R.id.cardSystemSettings);
        btnStartMonitoring = view.findViewById(R.id.btnStartMonitoring);
        btnExportReport = view.findViewById(R.id.btnExportReport);

        // 初始化传感器详情卡片
        tempHumidityCard = view.findViewById(R.id.tempHumidityCard);
        lightIntensityCard = view.findViewById(R.id.lightIntensityCard);
        soilMoistureCard = view.findViewById(R.id.soilMoistureCard);
        nutrientsCard = view.findViewById(R.id.nutrientsCard);
        plantGrowthCard = view.findViewById(R.id.plantGrowthCard);
        pestDetectionCard = view.findViewById(R.id.pestDetectionCard);
    }

    /**
     * 配置WebView以正确加载ESP32视频流
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webViewCamera.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setMediaPlaybackRequiresUserGesture(false); // 媒体播放不需要用户交互
        webSettings.setAllowFileAccess(true); // 允许访问本地文件
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 允许混合内容

        webViewCamera.setWebViewClient(new WebViewClient());
        webViewCamera.loadUrl(ESP32_STREAM_URL); // 加载视频流地址

        Log.d(TAG, "加载ESP32视频流: " + ESP32_STREAM_URL);
    }

    /**
     * 绑定各控件的点击事件处理器
     */
    private void setupClickListeners() {
        btnRefreshAll.setOnClickListener(v -> refreshAllData()); // 手动刷新全部数据
        cardDataAnalysis.setOnClickListener(v -> openDataAnalysis()); // 打开数据分析页
        cardSystemSettings.setOnClickListener(v -> openSystemSettings()); // 打开系统设置页
        btnStartMonitoring.setOnClickListener(v -> startMonitoring()); // 切换自动刷新状态
        btnExportReport.setOnClickListener(v -> exportReport()); // 导出报告

        // 绑定传感器详情卡片点击事件
        tempHumidityCard.setOnClickListener(v -> openTempHumidityDetail());
        lightIntensityCard.setOnClickListener(v -> openLightIntensityDetail());
        soilMoistureCard.setOnClickListener(v -> openSoilMoistureDetail());
        nutrientsCard.setOnClickListener(v -> openNutrientDetail()); // 土壤养分页面
        plantGrowthCard.setOnClickListener(v -> openPlantGrowth()); // 植物生长页面
        pestDetectionCard.setOnClickListener(v -> openPestDetection()); // 病虫害检测页面
    }

    /**
     * 打开养分详情页面
     */
    private void openNutrientDetail() {
        NutrientDetailFragment nutrientFragment = new NutrientDetailFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, nutrientFragment)
                .addToBackStack("nutrient_detail")
                .commit();
    }

    /**
     * 打开光照强度详情页面
     */
    private void openLightIntensityDetail() {
        LightIntensityFragment lightIntensityFragment = new LightIntensityFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, lightIntensityFragment)
                .addToBackStack("light_intensity_detail")
                .commit();
    }

    /**
     * 打开土壤水分详情页面
     */
    public void openSoilMoistureDetail() {
        SoilMoistureFragment soilMoistureFragment = new SoilMoistureFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, soilMoistureFragment)
                .addToBackStack("soil_moisture_detail")
                .commit();
    }

    /**
     * 打开环境温度详情页面
     */
    public void openTempHumidityDetail() {
        TempHumidityFragment tempHumidityFragment = new TempHumidityFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, tempHumidityFragment)
                .addToBackStack("temp_humidity_detail")
                .commit();
    }

    /**
     * 打开系统设置页面
     */
    public void openSystemSettings() {
        SystemSettingsFragment systemSettingsFragment = new SystemSettingsFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, systemSettingsFragment)
                .addToBackStack("system_settings")
                .commit();
    }

    /**
     * 打开数据分析页面
     */
    private void openDataAnalysis() {
        DataAnalysisFragment dataAnalysisFragment = new DataAnalysisFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, dataAnalysisFragment)
                .addToBackStack("data_analysis")
                .commit();
    }

    /**
     * 打开虫害检测页面
     */
    private void openPestDetection() {
        PestDetectionFragment pestDetectionFragment = new PestDetectionFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, pestDetectionFragment)
                .addToBackStack("pest_detection")
                .commit();
    }

    /**
     * 打开植物生长页面
     */
    private void openPlantGrowth() {
        PlantGrowthAnalysisFragment plantGrowthAnalysisFragment = new PlantGrowthAnalysisFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, plantGrowthAnalysisFragment)
                .addToBackStack("plant_growth")
                .commit();
    }

    /**
     * 初始化数据对象并触发首次刷新
     */
    private void initializeData() {
        currentSensorData = new SensorData();
        currentAIData = new AIAnalysisData();
        updateSensorStatus(); // 更新传感器状态显示
        refreshAllData(); // 获取最新数据
    }

    /**
     * 启动自动刷新任务
     */
    private void startAutoRefresh() {
        if (!isAutoRefreshEnabled) return;
        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    refreshAllData(); // 刷新数据
                    handler.postDelayed(this, refreshIntervalMs); // 循环执行
                }
            };
        }
        handler.postDelayed(autoRefreshRunnable, refreshIntervalMs);
        Log.d(TAG, "自动刷新已启动");
    }

    /**
     * 停止自动刷新任务
     */
    private void stopAutoRefresh() {
        if (handler != null && autoRefreshRunnable != null) {
            handler.removeCallbacks(autoRefreshRunnable);
            Log.d(TAG, "自动刷新已停止");
        }
    }

    /**
     * 手动刷新所有数据（包括传感器和AI分析）
     */
    private void refreshAllData() {
        Log.d(TAG, "开始刷新所有数据");
        fetchSensorData(); // 获取传感器数据
        fetchAIAnalysisData(); // 获取AI分析数据
    }

    /**
     * 异步从ESP32设备获取传感器数据
     */
    private void fetchSensorData() {
        Request request = new Request.Builder()
                .url(HttpUrlConnectConfig.ESP32_URL)
                .build();

        client.newCall(request).enqueue(new Callback() { // 异步请求
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    updateSystemStatus(false); // 更新系统状态为离线
                    Toast.makeText(getContext(), "传感器连接失败", Toast.LENGTH_SHORT).show();
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
                    parseSensorData(jsonObject); // 解析JSON数据
                    requireActivity().runOnUiThread(() -> {
                        updateSystemStatus(true); // 更新系统状态为在线
                        updateSensorDisplay(); // 更新UI显示
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show());
                }
                return;
            }
        });
    }

    /**
     * 获取AI分析结果（模拟实现）
     */
    private void fetchAIAnalysisData() {
        handler.postDelayed(() -> {
            if (getActivity() == null) return;
            simulateAIAnalysis(); // 模拟AI分析过程
            updateAIDisplay(); // 更新AI数据显示
        }, 1000); // 延迟1秒后执行
    }

    /**
     * 将接收到的JSON数据转换为SensorData对象
     *
     * @param jsonObject 来自服务器的原始JSON响应
     */
    private void parseSensorData(JsonObject jsonObject) {
        try {
            currentSensorData.temperature = jsonObject.has("temperature") ?
                    jsonObject.get("temperature").getAsDouble() : 0.0;
            currentSensorData.humidity = jsonObject.has("humidity") ?
                    jsonObject.get("humidity").getAsDouble() : 0.0;
            currentSensorData.lightIntensity = jsonObject.has("light_intensity") ?
                    jsonObject.get("light_intensity").getAsInt() : generateMockLightData();
            currentSensorData.soilMoisture = jsonObject.has("soil_moisture") ?
                    jsonObject.get("soil_moisture").getAsDouble() : generateMockSoilData();
            currentSensorData.timestamp = getCurrentTimestamp();
        } catch (Exception e) {
            Log.e(TAG, "传感器数据解析错误", e);
        }
    }

    /**
     * 模拟AI植物分析算法的结果
     */
    private void simulateAIAnalysis() {
        Random random = new Random();
        int score = 70 + random.nextInt(25); // 生成随机评分
        currentAIData.growthScore = score;

        if (score >= 85) currentAIData.growthStatus = "健康生长";
        else if (score >= 70) currentAIData.growthStatus = "良好";
        else currentAIData.growthStatus = "需关注";

        boolean hasPest = random.nextFloat() < 0.1; // 有10%概率检测到害虫
        currentAIData.pestDetected = hasPest;
        currentAIData.pestStatus = hasPest ? "发现异常" : "未发现异常";
        currentAIData.lastScanTime = getCurrentTimestamp(); // 记录扫描时间
    }

    /**
     * 更新系统在线状态指示灯颜色
     *
     * @param isOnline true表示在线，false表示离线
     */
    private void updateSystemStatus(boolean isOnline) {
        systemStatus.setBackgroundResource(isOnline ? R.drawable.circle_green : R.drawable.circle_red);
        onlineSensorCount = isOnline ? totalSensorCount : Math.max(0, totalSensorCount - 2);
        updateSensorStatus();
    }

    /**
     * 更新传感器在线数量显示
     */
    private void updateSensorStatus() {
        sensorCount.setText(String.format("传感器: %d/%d 在线", onlineSensorCount, totalSensorCount));
    }

    /**
     * 更新传感器数据UI显示
     */
    private void updateSensorDisplay() {
        envTempValue.setText(String.format("%.1f°C", currentSensorData.temperature));
        envHumidityValue.setText(String.format("%.0f%%", currentSensorData.humidity));
        lightIntensityValue.setText(String.format("%,d Lux", currentSensorData.lightIntensity));
        soilMoistureValue.setText(String.format("%.0f%%", currentSensorData.soilMoisture));
        soilMoistureProgress.setProgress((int) currentSensorData.soilMoisture);
    }

    /**
     * 更新AI分析结果UI显示
     */
    private void updateAIDisplay() {
        growthStatus.setText(currentAIData.growthStatus);
        growthScore.setText(String.format("评分: %d/100", currentAIData.growthScore));
        pestStatus.setText(currentAIData.pestStatus);
        lastScanTime.setText(currentAIData.lastScanTime);
    }

    /**
     * 开始或停止自动监测
     */
    private void startMonitoring() {
        if (!isAutoRefreshEnabled) {
            isAutoRefreshEnabled = true;
            startAutoRefresh();
            btnStartMonitoring.setText("停止监测");
            btnStartMonitoring.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.status_warning));
        } else {
            isAutoRefreshEnabled = false;
            stopAutoRefresh();
            btnStartMonitoring.setText("开始监测");
            btnStartMonitoring.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.status_success));
        }
    }

    /**
     * 导出监测报告
     */
    private void exportReport() {
        Toast.makeText(getContext(), "正在生成监测报告...", Toast.LENGTH_SHORT).show();
        handler.postDelayed(() -> {
            String reportName = "监测报告_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(new Date()) + ".pdf";
            Toast.makeText(getContext(), "报告已生成: " + reportName, Toast.LENGTH_LONG).show();
        }, 2000);
    }

    /**
     * 生成模拟光照强度数据（Lux单位）
     *
     * @return 模拟的光照强度值
     */
    private int generateMockLightData() {
        Random random = new Random();
        return 5000 + random.nextInt(20000);
    }

    /**
     * 生成模拟土壤湿度数据（百分比）
     *
     * @return 模拟的土壤湿度值
     */
    private double generateMockSoilData() {
        Random random = new Random();
        return 20 + random.nextDouble() * 60;
    }

    /**
     * 获取当前时间戳字符串
     *
     * @return 格式化的当前时间
     */
    private String getCurrentTimestamp() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date());
    }

    /**
     * 销毁Fragment时清理资源
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh(); // 停止自动刷新
        if (webViewCamera != null) {
            webViewCamera.destroy(); // 销毁WebView
        }
        Log.d(TAG, "Fragment销毁，释放资源");
    }
}



