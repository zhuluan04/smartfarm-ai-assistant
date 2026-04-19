package com.linjiu.recognize.layout.bottomNav.person;

import static com.linjiu.recognize.api.PersonApi.SYSTEM_STATUS_URL;
import static com.linjiu.recognize.api.PersonApi.USER_PROFILE_URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.linjiu.recognize.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Switch;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.linjiu.recognize.domain.person.SystemStatus;
import com.linjiu.recognize.domain.person.UserProfile;
import com.linjiu.recognize.layout.bottomNav.person.items.AccountSettingsFragment;
import com.linjiu.recognize.layout.bottomNav.person.items.AiManagement;
import com.linjiu.recognize.layout.bottomNav.person.items.DeviceManagement;
import com.linjiu.recognize.layout.bottomNav.person.items.NotificationSettings;
import com.linjiu.recognize.layout.bottomNav.person.items.SystemOverview;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 用户信息界面
public class PersonFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // 🎯 用户信息控件
    private CircleImageView civAvatar;
    private Uri imageUri; // 拍照时临时 Uri
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    // 使用新的 Activity Result API
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private View onlineIndicator;
    private TextView tvUsername, tvEmail, tvUserLevel;
    private TextView tvDevicesCount, tvAiTasks, tvNotifications;

    // 🔧 系统管理卡片
    private CardView cardSystemOverview, cardDeviceManagement, cardAiManagement;
    private TextView deviceStatusBadge, aiModelVersion;

    // 👤 个人设置卡片
    private CardView cardAccountSettings, cardNotificationSettings;
    private Switch switchNotifications;

    // 📱 应用信息卡片
    private CardView cardHelp, cardAbout;
    private MaterialButton btnLogout;

    // 📊 数据管理
    private Handler handler = new Handler();
    private Runnable statusUpdateRunnable;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    // 🔧 用户数据
    private UserProfile userProfile;
    private SystemStatus systemStatus;

    // 📱 配置参数
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SmartFarmPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private boolean isStatusUpdating = true;
    private int statusUpdateInterval = 30000; // 30秒更新间隔


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);

        initViews(view);
        initData();
        loadUserProfile();
        startStatusUpdates();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 头像点击 - 更换头像
        civAvatar.setOnClickListener(v -> showAvatarOptions());

        // 系统管理卡片点击事件
        cardSystemOverview.setOnClickListener(v -> navigateTo(new SystemOverview(), "system_overview"));
        cardDeviceManagement.setOnClickListener(v -> navigateTo(new DeviceManagement(), "device_management"));
        cardAiManagement.setOnClickListener(v -> navigateTo(new AiManagement(), "ai_management"));

        // 个人设置卡片点击事件
        cardAccountSettings.setOnClickListener(v -> navigateTo(new AccountSettingsFragment(), "account_settings"));
        cardNotificationSettings.setOnClickListener(v -> navigateTo(new NotificationSettings(), "notification_settings"));

        // 通知开关监听
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting(isChecked);
            showNotificationToast(isChecked);
        });

        // 应用信息卡片点击事件
        cardHelp.setOnClickListener(v -> openHelp());
        cardAbout.setOnClickListener(v -> openAbout());

        // 退出登录按钮
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void initViews(View view) {
        // 用户信息控件
        civAvatar = view.findViewById(R.id.civ_avatar);

        // 相机回调
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && imageUri != null) {
                        civAvatar.setImageURI(imageUri);
                        Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 相册回调
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        civAvatar.setImageURI(selectedImage);
                        Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        onlineIndicator = view.findViewById(R.id.online_indicator);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvUserLevel = view.findViewById(R.id.tv_user_level);

        // 状态统计控件
        tvDevicesCount = view.findViewById(R.id.tv_devices_count);
        tvAiTasks = view.findViewById(R.id.tv_ai_tasks);
        tvNotifications = view.findViewById(R.id.tv_notifications);

        // 系统管理卡片
        cardSystemOverview = view.findViewById(R.id.card_system_overview);
        cardDeviceManagement = view.findViewById(R.id.card_device_management);
        cardAiManagement = view.findViewById(R.id.card_ai_management);
        deviceStatusBadge = view.findViewById(R.id.device_status_badge);
        aiModelVersion = view.findViewById(R.id.ai_model_version);

        // 个人设置卡片
        cardAccountSettings = view.findViewById(R.id.card_account_settings);
        cardNotificationSettings = view.findViewById(R.id.card_notification_settings);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        // 应用信息卡片
        cardHelp = view.findViewById(R.id.card_help);
        cardAbout = view.findViewById(R.id.card_about);

        // 退出按钮
        btnLogout = view.findViewById(R.id.btn_logout);

        // 确保 CardView 可点击（避免布局拦截）
        CardView[] cards = new CardView[]{cardSystemOverview, cardDeviceManagement, cardAiManagement,
                cardAccountSettings, cardNotificationSettings, cardHelp, cardAbout};
        for (CardView c : cards) {
            if (c != null) {
                c.setClickable(true);
                c.setFocusable(true);
            }
        }
    }

    private void initData() {
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userProfile = new UserProfile();
        systemStatus = new SystemStatus();
        loadUserSettings();
    }

    // fragment页面跳转
    private void navigateTo(Fragment fragment, String tag) {
//        Toast.makeText(getContext(), "正在跳转到" + fragment.getClass().getSimpleName() + "页面", Toast.LENGTH_SHORT).show();
        if (!isAdded() || getActivity() == null) {
            Log.w(TAG, "navigateTo: Fragment not attached");
            return;
        }

        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss();

            Log.d(TAG, "跳转到: " + tag);
        } catch (Exception e) {
            Log.e(TAG, "跳转失败: " + tag, e);
            Toast.makeText(requireContext(), "页面打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔄 数据加载和更新方法
    private void loadUserProfile() {
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "朱栾");
        String savedEmail = sharedPreferences.getString(KEY_EMAIL, "guodesong04@163.com");

        userProfile.username = savedUsername;
        userProfile.email = savedEmail;
        userProfile.userLevel = "🌟 农业专家";

        updateUserDisplay();

        // 尝试从服务器加载最新用户信息
        fetchUserProfileFromServer();
    }

    private void fetchUserProfileFromServer() {
        Request request = new Request.Builder()
                .url(USER_PROFILE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "用户信息获取失败，使用本地缓存", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (getActivity() == null) return;

                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                        if (jsonObject.has("username")) {
                            userProfile.username = jsonObject.get("username").getAsString();
                        }
                        if (jsonObject.has("email")) {
                            userProfile.email = jsonObject.get("email").getAsString();
                        }
                        if (jsonObject.has("level")) {
                            userProfile.userLevel = jsonObject.get("level").getAsString();
                        }

                        requireActivity().runOnUiThread(() -> {
                            updateUserDisplay();
                            saveUserProfile();
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "用户数据解析错误", e);
                    }
                }
                return;
            }
        });
    }

    private void startStatusUpdates() {
        if (!isStatusUpdating) return;

        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchSystemStatus();
                if (isStatusUpdating) {
                    handler.postDelayed(this, statusUpdateInterval);
                }
            }
        };

        fetchSystemStatus();
        handler.postDelayed(statusUpdateRunnable, statusUpdateInterval);
    }

    private void fetchSystemStatus() {
        Request request = new Request.Builder()
                .url(SYSTEM_STATUS_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "系统状态获取失败，使用模拟数据", e);
                if (getActivity() == null) return;

                generateMockSystemStatus();
                requireActivity().runOnUiThread(() -> updateSystemStatusDisplay());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (getActivity() == null) return ;

                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonData = response.body().string();
                        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                        parseSystemStatus(jsonObject);

                        requireActivity().runOnUiThread(() -> updateSystemStatusDisplay());

                    } catch (Exception e) {
                        Log.e(TAG, "系统状态数据解析错误", e);
                        generateMockSystemStatus();
                        requireActivity().runOnUiThread(() -> updateSystemStatusDisplay());
                    }
                } else {
                    generateMockSystemStatus();
                    requireActivity().runOnUiThread(() -> updateSystemStatusDisplay());
                }
                return ;
            }
        });
    }

    private void parseSystemStatus(JsonObject jsonObject) {
        try {
            systemStatus.onlineDevices = jsonObject.has("online_devices") ?
                    jsonObject.get("online_devices").getAsInt() : 6;
            systemStatus.totalDevices = jsonObject.has("total_devices") ?
                    jsonObject.get("total_devices").getAsInt() : 6;
            systemStatus.aiTasksCompleted = jsonObject.has("ai_tasks") ?
                    jsonObject.get("ai_tasks").getAsInt() : 28;
            systemStatus.pendingNotifications = jsonObject.has("notifications") ?
                    jsonObject.get("notifications").getAsInt() : 3;
            systemStatus.aiModelVersion = jsonObject.has("ai_version") ?
                    jsonObject.get("ai_version").getAsString() : "v2.1";
            systemStatus.systemHealthy = !jsonObject.has("system_healthy") || jsonObject.get("system_healthy").getAsBoolean();

            Log.d(TAG, "系统状态更新: 设备=" + systemStatus.onlineDevices +
                    ", AI=" + systemStatus.aiTasksCompleted +
                    ", 通知=" + systemStatus.pendingNotifications);
        } catch (Exception e) {
            Log.e(TAG, "系统状态解析错误", e);
            generateMockSystemStatus();
        }
    }

    private void generateMockSystemStatus() {
        Random random = new Random();
        systemStatus.onlineDevices = 5 + random.nextInt(2); // 5-6
        systemStatus.totalDevices = 6;
        systemStatus.aiTasksCompleted = 25 + random.nextInt(10); // 25-35
        systemStatus.pendingNotifications = random.nextInt(5); // 0-4
        systemStatus.aiModelVersion = "v2." + (1 + random.nextInt(3)); // v2.1-v2.3
        systemStatus.systemHealthy = systemStatus.onlineDevices >= 5;

        Log.d(TAG, "使用模拟系统状态数据");
    }

    private void updateUserDisplay() {
        tvUsername.setText(userProfile.username);
        tvEmail.setText(userProfile.email);
        tvUserLevel.setText(userProfile.userLevel);

        onlineIndicator.setVisibility(View.VISIBLE);
    }

    private void updateSystemStatusDisplay() {
        tvDevicesCount.setText(String.valueOf(systemStatus.onlineDevices));
        tvAiTasks.setText(String.valueOf(systemStatus.aiTasksCompleted));
        tvNotifications.setText(String.valueOf(systemStatus.pendingNotifications));

        deviceStatusBadge.setText(systemStatus.onlineDevices + "台");
        aiModelVersion.setText(systemStatus.aiModelVersion);

        Log.d(TAG, "系统状态显示已更新");
    }

    // 显示头像选择菜单
    private void showAvatarOptions() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("更换头像")
                .setItems(new String[]{"拍摄照片", "从相册选择", "使用默认头像"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(getContext(), "拍摄照片功能开发中", Toast.LENGTH_SHORT).show();
                            openCamera();
                            break;
                        case 1:
                            Toast.makeText(getContext(), "相册选择功能开发中", Toast.LENGTH_SHORT).show();
                            openGallery();
                            break;
                        case 2:
                            civAvatar.setImageResource(R.drawable.ic_person);
                            Toast.makeText(getContext(), "已设置为默认头像", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    // 打开摄像机保存照片
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                // ⚠️ 注意：这里 authorities 必须和 AndroidManifest.xml 中一致！
                imageUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.linjiu.recognize.fileprovider", // ←←← 你的配置
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(takePictureIntent);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "创建照片文件失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "设备无可用相机", Toast.LENGTH_SHORT).show();
        }
    }

    // 打开相册
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openHelp() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("帮助与反馈")
                .setItems(new String[]{"使用指南", "常见问题", "联系客服", "意见反馈"}, (dialog, which) -> {
                    String[] options = {"使用指南", "常见问题", "联系客服", "意见反馈"};
                    Toast.makeText(getContext(), "正在打开" + options[which] + "...", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openAbout() {
        String aboutMessage = "智慧农业监测系统\n\n" +
                "版本：v1.2.0\n" +
                "构建时间：" + new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()) + "\n\n" +
                "开发团队：林炜宾\n" +
                "技术栈：Android + IoT + AI\n\n" +
                "© 2024 智慧农业科技";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("关于应用")
                .setMessage(aboutMessage)
                .setPositiveButton("确定", null)
                .setNeutralButton("检查更新", (dialog, which) -> {
                    Toast.makeText(getContext(), "正在检查更新...", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出当前账户吗？\n\n退出后需要重新登录才能使用系统功能。")
                .setPositiveButton("确认退出", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .setIcon(R.drawable.ic_logout)
                .show();
    }

    private void performLogout() {
        isStatusUpdating = false;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(getContext(), "正在退出登录...", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            if (getActivity() != null) {
                requireActivity().finish();
                Log.d(TAG, "用户已退出登录");
            }
        }, 1500);
    }

    // 💾 数据存储方法
    private void loadUserSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void saveNotificationSetting(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NOTIFICATIONS, enabled);
        editor.apply();
        Log.d(TAG, "通知设置已保存: " + enabled);
    }

    private void saveUserProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, userProfile.username);
        editor.putString(KEY_EMAIL, userProfile.email);
        editor.apply();
        Log.d(TAG, "用户信息已保存");
    }

    private void showNotificationToast(boolean enabled) {
        String message = enabled ? "通知提醒已开启" : "通知提醒已关闭";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isStatusUpdating = false;
        if (handler != null && statusUpdateRunnable != null) {
            handler.removeCallbacks(statusUpdateRunnable);
        }
        Log.d(TAG, "ProfileFragment销毁，清理资源");
    }
}
