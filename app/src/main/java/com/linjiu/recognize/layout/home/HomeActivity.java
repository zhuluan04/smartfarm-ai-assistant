package com.linjiu.recognize.layout.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linjiu.recognize.R;
import com.linjiu.recognize.config.AppConfig;
import com.linjiu.recognize.layout.bottomNav.monitor.MonitorFragment;
import com.linjiu.recognize.layout.bottomNav.person.PersonFragment;
import com.linjiu.recognize.layout.bottomNav.module.ModulesFragment;
import com.linjiu.recognize.layout.home.agent.AiChatActivity;
import com.linjiu.recognize.layout.login.LoginActivity;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 校验登录状态
        if (!isLoggedIn()) {
            Log.d(TAG, "未检测到登录状态，跳转登录页");
//            goToLogin();
//            return;
        }

        Log.d(TAG, "已登录，初始化主页");

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 默认加载 MonitorFragment
        if (savedInstanceState == null) {
            navigateToFragment(new MonitorFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_monitor);
        }

        setupBottomNavigation();
        setupFloatingButton();
    }

    // 底部导航栏监听
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            int itemId = item.getItemId();

            if ((itemId == R.id.nav_monitor && currentFragment instanceof MonitorFragment) ||
                    (itemId == R.id.nav_settings && currentFragment instanceof ModulesFragment) ||
                    (itemId == R.id.nav_person && currentFragment instanceof PersonFragment)) {
                return true;
            }

            Fragment selectedFragment = null;
            if (itemId == R.id.nav_monitor) {
                selectedFragment = new MonitorFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new ModulesFragment();
            } else if (itemId == R.id.nav_person) {
                selectedFragment = new PersonFragment();
            }

            if (selectedFragment != null) {
                navigateToFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    // 悬浮按钮监听
    @SuppressLint("ClickableViewAccessibility")
    private void setupFloatingButton() {
        FloatingActionButton fabAiChat = findViewById(R.id.fab_ai_chat);
        fabAiChat.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AiChatActivity.class));
        });

        fabAiChat.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private boolean isDragging;
            private long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup parent = (ViewGroup) v.getParent();
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        isDragging = false;
                        downTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            isDragging = true;
                        }

                        float newX = v.getX() + dx;
                        float newY = v.getY() + dy;

                        newX = Math.max(0, Math.min(newX, parentWidth - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, parentHeight - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);

                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging && (System.currentTimeMillis() - downTime) < 200) {
                            v.performClick();
                        } else {
                            float middle = parentWidth / 2f;
                            if (v.getX() < middle) {
                                v.animate().x(0).setDuration(200).start();
                            } else {
                                v.animate().x(parentWidth - v.getWidth()).setDuration(200).start();
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    // 是否登录
    private boolean isLoggedIn() {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String token = sp.getString("token", null);
        Log.d(TAG, "读取 token: " + token);
        return token != null && !token.isEmpty();
    }

    // 跳转登录页并清除任务栈
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // 跳转 Fragment
    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}