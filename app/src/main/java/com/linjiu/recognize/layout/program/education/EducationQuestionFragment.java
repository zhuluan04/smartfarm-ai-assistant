package com.linjiu.recognize.layout.program.education;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.linjiu.recognize.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EducationQuestionFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    // UI组件
    private PreviewView previewView;
    private ImageButton captureButton;
    private ImageButton flashButton;
    private ImageButton switchCameraButton;
    private ImageView previewImage;
    private TextView resultText;
    private ProgressBar progressBar;
    private View cameraContainer;
    private View resultContainer;
    private ImageButton retakeButton;
    private ImageButton confirmButton;

    // 相机相关
    private ImageCapture imageCapture;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private boolean flashEnabled = false;
    private boolean isFrontCamera = false; // 使用标志管理前/后摄像头

    // 线程池
    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_education_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            // ❌ 不要使用 ActivityCompat.requestPermissions(requireActivity(), ...);
            // ✅ 使用 Fragment 自身来请求权限，这样 onRequestPermissionsResult 能被 fragment 接收
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @SuppressLint("WrongViewCast")
    private void initViews(View view) {
        previewView = view.findViewById(R.id.preview_view);
        captureButton = view.findViewById(R.id.btn_capture);
        flashButton = view.findViewById(R.id.btn_flash);
        switchCameraButton = view.findViewById(R.id.btn_switch_camera);
        previewImage = view.findViewById(R.id.iv_preview);
        resultText = view.findViewById(R.id.tv_result);
        progressBar = view.findViewById(R.id.progress_bar);
        cameraContainer = view.findViewById(R.id.camera_container);
        resultContainer = view.findViewById(R.id.result_container);
        retakeButton = view.findViewById(R.id.btn_retake);
        confirmButton = view.findViewById(R.id.btn_confirm);
    }

    private void setupClickListeners() {
        captureButton.setOnClickListener(v -> takePhoto());
        flashButton.setOnClickListener(v -> toggleFlash());
        switchCameraButton.setOnClickListener(v -> switchCamera());
        retakeButton.setOnClickListener(v -> {
            showCameraView();
            startCamera();
        });
        confirmButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "答案已保存", Toast.LENGTH_SHORT).show();
            showCameraView();
        });
    }

    private void startCamera() {
        if (!isAdded() || getContext() == null) return;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "相机启动失败", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || previewView == null || !isAdded()) return;

        // 获取 display rotation（防止 getDisplay() 为 null 导致 NPE）
        int rotation = Surface.ROTATION_0;
        if (previewView.getDisplay() != null) {
            rotation = previewView.getDisplay().getRotation();
        } else if (getActivity() != null && getActivity().getWindow() != null && getActivity().getWindow().getDecorView() != null) {
            // 兜底获取，尽量避免 NPE（在旧 API 上可能已弃用，但作为兜底）
            try {
                rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            } catch (Exception ignored) {
            }
        }

        Preview preview = new Preview.Builder()
                .setTargetRotation(rotation)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(rotation)
                .setFlashMode(flashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                .build();

        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, (ImageProxy image) -> {
            try {
                // 你的实时分析逻辑（占位）
            } finally {
                image.close();
            }
        });

        try {
            cameraProvider.unbindAll();

            camera = cameraProvider.bindToLifecycle(
                    (LifecycleOwner) this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
            );

        } catch (Exception e) {
            if (isAdded()) {
                Toast.makeText(getContext(), "相机绑定失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(getContext(), "相机未准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        captureButton.setEnabled(false);

        File photoFile = createTempFile();
        if (photoFile == null) {
            showError("无法创建临时文件");
            return;
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        String path = photoFile.getAbsolutePath();
                        processImage(path);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showError("拍照失败: " + exception.getMessage());
                    }
                }
        );
    }

    private void processImage(String imagePath) {
        cameraExecutor.execute(() -> {
            if (!isAdded() || getActivity() == null) return;

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap == null) {
                    showErrorOnUiThread("图片加载失败");
                    return;
                }

                // 计算旋转角度（使用 isFrontCamera 标志）
                int degrees = getRotationCompensation(isFrontCamera);
                Bitmap rotated = rotateBitmap(bitmap, degrees);
                if (rotated != bitmap) {
                    // rotated 已创建，旧 bitmap 可以回收
                    try {
                        bitmap.recycle();
                    } catch (Exception ignored) {
                    }
                }

                String questionText = performOCR(rotated);
                String answer = getAnswer(questionText);

                if (isAdded() && getActivity() != null) {
                    Bitmap finalBitmap = rotated;
                    getActivity().runOnUiThread(() -> {
                        displayResult(finalBitmap, questionText, answer);
                        progressBar.setVisibility(View.GONE);
                        captureButton.setEnabled(true);
                    });
                }

            } catch (Exception e) {
                showErrorOnUiThread("处理失败: " + e.getMessage());
            }
        });
    }

    private void displayResult(Bitmap bitmap, String question, String answer) {
        previewImage.setImageBitmap(bitmap);

        StringBuilder result = new StringBuilder();
        result.append("题目：\n").append(question).append("\n\n");
        result.append("解答：\n").append(answer);

        resultText.setText(result.toString());
        showResultView();
    }

    private void showCameraView() {
        if (cameraContainer != null && resultContainer != null) {
            cameraContainer.setVisibility(View.VISIBLE);
            resultContainer.setVisibility(View.GONE);
        }
    }

    private void showResultView() {
        if (cameraContainer != null && resultContainer != null) {
            cameraContainer.setVisibility(View.GONE);
            resultContainer.setVisibility(View.VISIBLE);
        }
    }

    private void toggleFlash() {
        flashEnabled = !flashEnabled;
        if (flashButton != null) flashButton.setSelected(flashEnabled);

        if (imageCapture != null) {
            imageCapture.setFlashMode(flashEnabled ?
                    ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        }
    }

    private void switchCamera() {
        isFrontCamera = !isFrontCamera;
        cameraSelector = isFrontCamera ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        bindCameraUseCases();
    }

    // 计算旋转角度：传入是否为前摄像头
    private int getRotationCompensation(boolean isFrontFacing) {
        int rotation = Surface.ROTATION_0;
        if (previewView != null && previewView.getDisplay() != null) {
            rotation = previewView.getDisplay().getRotation();
        } else if (getActivity() != null) {
            try {
                rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            } catch (Exception ignored) {
            }
        }

        int rotationDegrees = rotation * 90;
        switch (rotationDegrees) {
            case 0:
                return isFrontFacing ? 270 : 90;
            case 90:
                return 0;
            case 180:
                return isFrontFacing ? 90 : 270;
            case 270:
                return 180;
            default:
                return 0;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        try {
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                try {
                    bitmap.recycle();
                } catch (Exception ignored) {
                }
            }
            return rotated;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    private File createTempFile() {
        if (!isAdded() || getContext() == null) return null;

        File dir = requireContext().getExternalFilesDir(null);
        if (dir == null) {
            dir = requireContext().getFilesDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(dir, "IMG_" + timeStamp + ".jpg");

        try {
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) return null;
            }
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    private String performOCR(Bitmap bitmap) {
        // TODO: 集成真实 OCR
        return "这是一道数学题：2 + 2 = ?";
    }

    private String getAnswer(String question) {
        // TODO: 集成真实 AI 答题
        return "根据题目分析：\n2 + 2 = 4\n\n解题思路：\n这是一道简单的加法题，直接相加即可得到答案。";
    }

    private void showError(String msg) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> showErrorOnUiThread(msg));
        }
    }

    private void showErrorOnUiThread(String msg) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (captureButton != null) captureButton.setEnabled(true);
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean allPermissionsGranted() {
        if (!isAdded() || getContext() == null) return false;

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Fragment 会接收到这个回调（因为我们使用了 requestPermissions(...)）
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "需要相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}
