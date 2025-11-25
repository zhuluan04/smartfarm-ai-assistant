package com.linjiu.recognize.layout.program.entertainment.module;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.google.android.filament.*;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.ResourceLoader;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FilamentModelView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private Engine engine;
    private SwapChain swapChain;
    private Renderer renderer;
    private Scene scene;
    private View view;
    private Camera camera;
    private FilamentAsset asset;
    private AssetLoader assetLoader;

    private boolean modelLoaded = false;

    public FilamentModelView(Context context) {
        super(context);
        init();
    }

    public FilamentModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(3);

        // 初始化 Filament 工具

        engine = Engine.create();
        renderer = (Renderer) engine.createRenderer();
        scene = engine.createScene();

        view = engine.createView();
        view.setScene(scene);

        // 创建相机实体
        int cameraEntity = EntityManager.get().create();
        camera = engine.createCamera(cameraEntity);
        view.setCamera(camera);

        // 让 GLSurfaceView 回调渲染
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (swapChain == null) {
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float aspect = (float) width / (float) height;
        camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (modelLoaded && renderer != null && swapChain != null) {
        }
    }

    /**
     * 加载 glTF 模型
     */
    public void loadModel(ByteBuffer buffer) {
        if (assetLoader == null) {

        }
        asset = assetLoader.createAsset(buffer);

        if (asset != null) {
            ResourceLoader resourceLoader = new ResourceLoader(engine);
            resourceLoader.loadResources(asset);
            scene.addEntity(asset.getRoot());
            modelLoaded = true;
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        if (asset != null) {
            engine.destroyEntity(asset.getRoot());
            asset = null;
        }
        if (renderer != null) {
            renderer = null;
        }
        if (view != null) {
            engine.destroyView(view);
            view = null;
        }
        if (camera != null) {
            engine.destroyCameraComponent(camera.getEntity());
            camera = null;
        }
        if (scene != null) {
            engine.destroyScene(scene);
            scene = null;
        }
        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
            swapChain = null;
        }
        if (engine != null) {
            engine.destroy();
            engine = null;
        }
    }
}
