package com.linjiu.recognize.layout.bottomNav.module;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;
import com.linjiu.recognize.api.ImageAnalysisApi;
import com.linjiu.recognize.domain.image.ImageAnalysisRequest;
import com.linjiu.recognize.domain.image.ImageAnalysisResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 植物信息分析页面
public class PlantAnalyzeFragment extends Fragment {

    private static final int REQUEST_IMAGE_GET = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private static final String[] PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.READ_MEDIA_IMAGES}
            : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    private ImageView imageView;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private ImageAnalysisApi imageAnalysisApi;
    private Call<ImageAnalysisResponse> currentCall; // 用于取消请求

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modules_analyze, container, false);

        imageView = view.findViewById(R.id.imageView);
        Button buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        textViewResult = view.findViewById(R.id.textViewResult);
        progressBar = view.findViewById(R.id.progressBar);

        initRetrofit();

        // 点击选择图片按钮
        buttonSelectImage.setOnClickListener(v -> {
            if (checkPermission()) {
                openImagePicker();
            }
        });

        return view;
    }

    private void initRetrofit() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.38.142:8080/") // 替换为你的服务器IP
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        imageAnalysisApi = retrofit.create(ImageAnalysisApi.class);
    }

    private boolean checkPermission() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionWithRationale();
                return false;
            }
        }
        return true;
    }

    private void requestPermissionWithRationale() {
        boolean shouldShowRationale = false;
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
                shouldShowRationale = true;
                break;
            }
        }

        if (shouldShowRationale) {
            // 用户之前拒绝过，显示说明
            new AlertDialog.Builder(requireContext())
                    .setTitle("需要相册权限")
                    .setMessage("请允许访问相册，以便选择图片进行识别。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            // 首次请求 or 用户勾选了“不再询问”
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    // 打开图片选择器
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        } else {
            Toast.makeText(getContext(), "❌ 找不到可用的图片选择器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == requireActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmap);
                    analyzeImage(bitmap);
                } catch (IOException e) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "图片处理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    // 分析图片
    private void analyzeImage(Bitmap bitmap) {
        if (!isAdded() || getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        textViewResult.setText("正在分析中，请稍候...");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String base64Image = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);

        // 使用 'base64_image' 而不是 'image_url'
        ImageAnalysisRequest request = new ImageAnalysisRequest(base64Image, "分析图像内的物品");

        currentCall = imageAnalysisApi.analyzeImage(request);
        currentCall.enqueue(new Callback<ImageAnalysisResponse>() {
            @Override
            public void onResponse(@NonNull Call<ImageAnalysisResponse> call, @NonNull Response<ImageAnalysisResponse> response) {
                if (!isAdded() || getContext() == null) return;

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    simulateTypingEffect(response.body().getAnswer());
                } else {
                    String errorMsg = "分析失败: " + (response.message().isEmpty() ? "未知错误" : response.message());
                    textViewResult.setText("❌ " + errorMsg);
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ImageAnalysisResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;

                progressBar.setVisibility(View.GONE);
                String errorMsg = "网络错误: " + t.getMessage();
                textViewResult.setText("❌ " + errorMsg);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                Log.e("NETWORK_ERROR", t.toString());
            }
        });
    }

    // 模拟打字机效果：逐字显示
    private void simulateTypingEffect(String fullText) {
        if (!isAdded() || getContext() == null) return;

        textViewResult.setText(""); // 清空
        final StringBuilder currentText = new StringBuilder();
        final int totalLength = fullText.length();

        mainHandler.postDelayed(new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < totalLength && isAdded() && getContext() != null) {
                    currentText.append(fullText.charAt(index));
                    textViewResult.setText(currentText.toString());
                    index++;
                    mainHandler.postDelayed(this, 30); // 每30ms显示一个字符（可调）
                }
            }
        }, 50); // 延迟50ms开始
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                openImagePicker(); // ✅ 权限通过后，打开选择器
            } else {
                if (isAdded() && getContext() != null) {
                    showPermissionDeniedDialog();
                }
            }
        }
    }

    // 显示权限许可
    private void showPermissionDeniedDialog() {
        if (!isAdded() || getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("权限被拒绝")
                .setMessage("需要“照片”权限才能选择图片。请前往设置手动开启。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null) {
            currentCall.cancel(); // 取消网络请求
        }
        mainHandler.removeCallbacksAndMessages(null); // 防止内存泄漏
    }
}