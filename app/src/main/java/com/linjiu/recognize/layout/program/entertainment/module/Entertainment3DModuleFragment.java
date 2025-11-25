package com.linjiu.recognize.layout.program.entertainment.module;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.filament.Engine;
import com.google.android.filament.Entity;
import com.google.android.filament.LightManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.Scene;
import com.google.android.filament.Skybox;
import com.google.android.filament.SwapChain;
import com.google.android.filament.Camera;
import com.google.android.filament.EntityManager;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.ResourceLoader;
import com.google.android.filament.gltfio.UbershaderProvider;
import com.linjiu.recognize.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * PlantGrowthMonitorFragment 是一个用于智能植物监测的3D可视化界面
 * 它使用 Google Filament 渲染引擎加载并渲染植物3D模型，同时显示植物生长状态数据
 */
public class Entertainment3DModuleFragment extends Fragment {

    private static final String TAG = "PlantGrowthMonitor";
    private SurfaceView surfaceView;
    private Engine engine;
    private Renderer renderer;
    private Scene scene;
    private Camera camera;
    private com.google.android.filament.View filamentView;
    private SwapChain swapChain;
    private AssetLoader assetLoader;
    private ResourceLoader resourceLoader;
    private UbershaderProvider ubershaderProvider;
    private FilamentAsset asset;

    @Entity
    private int cameraEntity;
    private boolean isRendering = false;

    // 交互相关变量
    private float yaw = 0f;  // 绕 Y 轴旋转
    private float pitch = 0f; // 绕 X 轴旋转
    private float distance = 4f; // 相机距离
    private float minDistance = 2f;
    private float maxDistance = 10f;

    // UI组件
    private TextView plantName;
    private TextView connectionStatus;
    private TextView growthIndex;
    private TextView healthStatus;
    private TextView maturityLevel;
    private ProgressBar moistureProgress;
    private ProgressBar lightProgress;
    private TextView moistureValue;
    private TextView lightValue;
    private TextView lastUpdate;
    private Button btnWater;
    private Button btnFertilize;
    private Button btnHistory;

    // 模拟数据更新
    private Random random = new Random();
    private int updateCount = 0;
    private Runnable dataUpdateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entertainment_3d_module, container, false);

        // 初始化UI组件
        initUIComponents(view);

        // 初始化Filament
        initFilament();

        // 设置3D模型显示
        surfaceView = view.findViewById(R.id.surface_view);
        setupSurfaceView();

        // 设置触摸交互
        setupTouchInteraction();

        // 设置按钮点击事件
        setupButtonEvents();

        return view;
    }

    private void initUIComponents(View view) {
        plantName = view.findViewById(R.id.plant_name);
        connectionStatus = view.findViewById(R.id.connection_status);
        growthIndex = view.findViewById(R.id.growth_index);
        healthStatus = view.findViewById(R.id.health_status);
        maturityLevel = view.findViewById(R.id.maturity_level);
        moistureProgress = view.findViewById(R.id.moisture_progress);
        lightProgress = view.findViewById(R.id.light_progress);
        moistureValue = view.findViewById(R.id.moisture_value);
        lightValue = view.findViewById(R.id.light_value);
        lastUpdate = view.findViewById(R.id.last_update);
        btnWater = view.findViewById(R.id.btn_water);
        btnFertilize = view.findViewById(R.id.btn_fertilize);
        btnHistory = view.findViewById(R.id.btn_history);

        // 初始化UI数据
        updateInitialUI();
    }

    private void updateInitialUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                plantName.setText("智能盆栽监测系统");
                connectionStatus.setText("已连接");
                growthIndex.setText("85%");
                healthStatus.setText("良好");
                maturityLevel.setText("70%");
                moistureProgress.setProgress(75);
                lightProgress.setProgress(85);
                moistureValue.setText("75%");
                lightValue.setText("85%");
                lastUpdate.setText("刚刚更新");

                // 设置初始颜色
                growthIndex.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                healthStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            });
        }
    }

    private void setupSurfaceView() {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "Surface created");
                try {
                    swapChain = engine.createSwapChain(holder.getSurface());
                    loadModel("models/PottedPlant.glb");
                    if (!isRendering) {
                        startRenderLoop();
                        isRendering = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error creating swap chain", e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "Surface changed: " + width + "x" + height);
                if (camera != null) {
                    try {
                        double fovDegrees = 45.0;
                        double aspect = (double) width / height;
                        double near = 0.1;
                        double far = 1000.0;

                        double fovRad = Math.toRadians(fovDegrees);
                        double top = near * Math.tan(fovRad / 2.0);
                        double bottom = -top;
                        double right = top * aspect;
                        double left = -right;

                        camera.setProjection(Camera.Projection.PERSPECTIVE, left, right, bottom, top, near, far);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting camera projection", e);
                    }
                }
                if (filamentView != null) {
                    try {
                        filamentView.setViewport(new com.google.android.filament.Viewport(0, 0, width, height));
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting viewport", e);
                    }
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "Surface destroyed");
                isRendering = false;
                cleanup();
            }
        });
    }

    private void setupTouchInteraction() {
        // 旋转手势检测器
        GestureDetector.SimpleOnGestureListener rotationListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 拖动旋转
                yaw += distanceX * 0.01f;
                pitch -= distanceY * 0.01f;

                // 限制 pitch 范围防止翻转
                pitch = (float) Math.max(-Math.PI / 2 + 0.1f, Math.min(Math.PI / 2 - 0.1f, pitch));

                updateCamera();
                return true;
            }
        };

        GestureDetector gestureDetector = new GestureDetector(requireContext(), rotationListener);

        // 缩放手势检测器
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();
                        distance *= scaleFactor;
                        distance = Math.max(minDistance, Math.min(maxDistance, distance));
                        updateCamera();
                        return true;
                    }
                });

        surfaceView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void updateCamera() {
        if (camera != null) {
            try {
                // 计算相机位置（球形轨道）
                float x = (float) (distance * Math.sin(pitch) * Math.cos(yaw));
                float y = (float) (distance * Math.sin(pitch) * Math.sin(yaw));
                float z = (float) (distance * Math.cos(pitch));

                camera.lookAt(x, y, z, 0, 0, 0, 0, 1, 0); // 朝向原点
            } catch (Exception e) {
                Log.e(TAG, "Error updating camera", e);
            }
        }
    }

    private void initFilament() {
        try {
            engine = Engine.create();
            renderer = engine.createRenderer();
            scene = engine.createScene();

            EntityManager entityManager = EntityManager.get();
            cameraEntity = entityManager.create();
            camera = engine.createCamera(cameraEntity);

            filamentView = engine.createView();
            filamentView.setCamera(camera);
            filamentView.setScene(scene);

            // 设置背景色
            Skybox skybox = new Skybox.Builder()
                    .color(new float[]{0.95f, 0.98f, 1.0f, 1.0f}) // 浅蓝色背景
                    .build(engine);
            scene.setSkybox(skybox);

            ubershaderProvider = new UbershaderProvider(engine);
            assetLoader = new AssetLoader(engine, ubershaderProvider, entityManager);
            resourceLoader = new ResourceLoader(engine);

            Log.d(TAG, "Filament initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Filament", e);
        }
    }

    private void loadModel(String filename) {
        Log.d(TAG, "Attempting to load model: " + filename);

        try (InputStream inputStream = requireContext().getAssets().open(filename)) {
            Log.d(TAG, "Input stream opened successfully");

            int size = inputStream.available();
            if (size <= 0) {
                Log.e(TAG, "Model file is empty or not found: " + filename);
                return;
            }
            Log.d(TAG, "File size: " + size + " bytes");

            byte[] buffer = new byte[size];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead != size) {
                Log.e(TAG, "Incomplete read from asset: " + filename);
                return;
            }

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
            byteBuffer.put(buffer);
            byteBuffer.flip();

            asset = assetLoader.createAsset(byteBuffer);
            if (asset != null) {
                Log.d(TAG, "✅ Model loaded successfully: " + filename);
                resourceLoader.loadResources(asset);
                asset.releaseSourceData();

                scene.addEntities(asset.getEntities());

                // 设置初始相机位置
                updateCamera();

                addDefaultLight();

                Log.d(TAG, "Added " + asset.getEntities().length + " entities to scene");
            } else {
                Log.e(TAG, "❌ Failed to create asset from buffer: " + filename);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while loading model: " + filename, e);
        } catch (Exception e) {
            Log.e(TAG, "Error loading model: " + filename, e);
        }
    }

    private void addDefaultLight() {
        try {
            EntityManager em = EntityManager.get();

            // 创建主光源（太阳光）
            @Entity int mainLight = em.create();
            LightManager.Builder mainBuilder = new LightManager.Builder(LightManager.Type.SUN)
                    .color(1.0f, 1.0f, 1.0f)
                    .intensity(100000.0f) // 太阳光强度（lux）
                    .direction(0.0f, -1.0f, -1.0f); // 从上方照射
            mainBuilder.build(engine, mainLight);
            scene.addEntity(mainLight);

            // 创建辅助光源（点光源）
            @Entity int auxLight = em.create();
            LightManager.Builder auxBuilder = new LightManager.Builder(LightManager.Type.POINT)
                    .color(1.0f, 1.0f, 1.0f)
                    .intensity(1000.0f)
                    .position(2.0f, 2.0f, 2.0f); // 在场景中放置一个辅助光源
            auxBuilder.build(engine, auxLight);
            scene.addEntity(auxLight);

            Log.d(TAG, "Lights added: sun and point light");
        } catch (Exception e) {
            Log.e(TAG, "Error adding lights", e);
        }
    }

    private void startRenderLoop() {
        android.view.Choreographer.getInstance().postFrameCallback(new android.view.Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (isRendering && renderer != null && swapChain != null && filamentView != null) {
                    try {
                        if (renderer.beginFrame(swapChain, frameTimeNanos)) {
                            renderer.render(filamentView);
                            renderer.endFrame();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in render loop", e);
                    }
                    android.view.Choreographer.getInstance().postFrameCallback(this);
                }
            }
        });
        Log.d(TAG, "Render loop started");
    }

    private void setupButtonEvents() {
        btnWater.setOnClickListener(v -> {
            // 模拟浇水操作
            Log.d(TAG, "自动浇水按钮点击");
            // 这里可以调用实际的浇水接口
        });

        btnFertilize.setOnClickListener(v -> {
            // 模拟施肥操作
            Log.d(TAG, "施肥按钮点击");
            // 这里可以调用实际的施肥接口
        });

        btnHistory.setOnClickListener(v -> {
            // 跳转到历史数据页面
            Log.d(TAG, "历史数据按钮点击");
            // 这里可以跳转到历史数据页面
        });
    }

    private void startDataUpdateSimulation() {
        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && isAdded()) {
                    updatePlantData();
                    updateCount++;
                    if (lastUpdate != null) {
                        lastUpdate.setText("上次更新: " + updateCount + "秒前");
                    }

                    // 30秒后重置计时器
                    if (updateCount >= 30) {
                        updateCount = 0;
                    }

                    // 每5秒更新一次
                    getActivity().runOnUiThread(this);
                }
            }
        };

        // 每5秒更新一次数据
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updatePlantData();
                // 启动数据更新线程
                new Thread(() -> {
                    while (isAdded() && isRendering) {
                        try {
                            Thread.sleep(5000); // 每5秒更新一次
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(this::updatePlantData);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }).start();
            });
        }
    }

    private void updatePlantData() {
        if (!isAdded()) return; // 检查fragment是否已添加到activity

        // 模拟植物数据更新
        int growth = 80 + random.nextInt(11); // 80-90%
        int moisture = 70 + random.nextInt(21); // 70-90%
        int light = 80 + random.nextInt(16); // 80-95%
        int maturity = 65 + random.nextInt(11); // 65-75%

        // 在主线程中更新UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (growthIndex != null) {
                    growthIndex.setText(growth + "%");
                }
                if (moistureProgress != null) {
                    moistureProgress.setProgress(moisture);
                }
                if (moistureValue != null) {
                    moistureValue.setText(moisture + "%");
                }
                if (lightProgress != null) {
                    lightProgress.setProgress(light);
                }
                if (lightValue != null) {
                    lightValue.setText(light + "%");
                }
                if (maturityLevel != null) {
                    maturityLevel.setText(maturity + "%");
                }

                // 根据数值更新颜色
                if (growthIndex != null) {
                    if (growth >= 85) {
                        growthIndex.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                        if (healthStatus != null) {
                            healthStatus.setText("优秀");
                            healthStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                        }
                    } else if (growth >= 70) {
                        growthIndex.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
                        if (healthStatus != null) {
                            healthStatus.setText("良好");
                            healthStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
                        }
                    } else {
                        growthIndex.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
                        if (healthStatus != null) {
                            healthStatus.setText("一般");
                            healthStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
                        }
                    }
                }

                if (moistureProgress != null) {
                    if (moisture < 60) {
                        moistureProgress.getProgressDrawable().setColorFilter(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    } else if (moisture > 80) {
                        moistureProgress.getProgressDrawable().setColorFilter(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        moistureProgress.getProgressDrawable().setColorFilter(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
            });
        }
    }

    private void cleanup() {
        isRendering = false;

        if (dataUpdateRunnable != null) {
            // 取消数据更新线程
            dataUpdateRunnable = null;
        }

        if (asset != null) {
            try {
                assetLoader.destroyAsset(asset);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying asset", e);
            }
            asset = null;
        }
        if (assetLoader != null) {
            try {
                assetLoader.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying asset loader", e);
            }
            assetLoader = null;
        }
        if (resourceLoader != null) {
            try {
                resourceLoader.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying resource loader", e);
            }
            resourceLoader = null;
        }
        if (ubershaderProvider != null) {
            try {
                ubershaderProvider.destroyMaterials();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying ubershader provider", e);
            }
            ubershaderProvider = null;
        }
        if (swapChain != null) {
            try {
                engine.destroySwapChain(swapChain);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying swap chain", e);
            }
            swapChain = null;
        }
        if (filamentView != null) {
            try {
                engine.destroyView(filamentView);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying filament view", e);
            }
            filamentView = null;
        }
        if (renderer != null) {
            try {
                engine.destroyRenderer(renderer);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying renderer", e);
            }
            renderer = null;
        }
        if (scene != null) {
            try {
                engine.destroyScene(scene);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying scene", e);
            }
            scene = null;
        }
        if (camera != null) {
            try {
                engine.destroyCameraComponent(cameraEntity);
                EntityManager.get().destroy(cameraEntity);
            } catch (Exception e) {
                Log.e(TAG, "Error destroying camera", e);
            }
            camera = null;
        }
        if (engine != null) {
            try {
                engine.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying engine", e);
            }
            engine = null;
        }

        Log.d(TAG, "Cleanup completed");
    }

    @Override
    public void onResume() {
        super.onResume();
        // 重新开始数据更新
        startDataUpdateSimulation();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 暂停数据更新
        isRendering = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }
}



