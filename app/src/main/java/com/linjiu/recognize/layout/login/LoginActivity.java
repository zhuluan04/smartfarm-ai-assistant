package com.linjiu.recognize.layout.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.linjiu.recognize.R;
import com.linjiu.recognize.config.AppConfig;
import com.linjiu.recognize.config.HttpUrlConnectConfig;
import com.linjiu.recognize.domain.user.UserResponse;
import com.linjiu.recognize.layout.home.HomeActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    // 演示开关：true 时跳过后端校验，使用本地 mock 账号登录
    private static final boolean USE_MOCK_LOGIN = true;

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private CheckBox cbRememberMe;

    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        loadLoginInfo();
        autoLoginCheck();

        btnLogin.setOnClickListener(v -> performLogin());
        tvGoToRegister.setOnClickListener(v -> performRegister());
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        cbRememberMe = findViewById(R.id.cbRememberMe);
    }

    // 自动登录
    private void autoLoginCheck() {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, MODE_PRIVATE);
        String token = sp.getString("token", null);
        boolean remember = sp.getBoolean("remember", false);

        Log.d(TAG, "自动登录检查 - token: " + token + ", remember: " + remember);

        if (token != null && !token.isEmpty() && remember) {
            jumpToHome();
        }
    }

    // 加载保存的账号密码
    private void loadLoginInfo() {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, MODE_PRIVATE);
        boolean remember = sp.getBoolean("remember", false);
        cbRememberMe.setChecked(remember);

        if (remember) {
            etUsername.setText(sp.getString("username", ""));
            etPassword.setText(sp.getString("password", ""));
        }
    }

    // 登录
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (USE_MOCK_LOGIN) {
            mockLogin(username, password);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(HttpUrlConnectConfig.APP_URL + "user/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络错误: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        UserResponse resp = gson.fromJson(respBody, UserResponse.class);

                        if (resp.code == 200) {
                            saveLoginInfo(username, password, resp.data.token);
                            Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            jumpToHome();
                        } else {
                            Toast.makeText(LoginActivity.this, resp.msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "解析错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                return ;
            }
        });
    }

    // 本地 mock 登录：仅用于演示，无需网络
    private void mockLogin(String username, String password) {
        if ("gds".equals(username) && "123456".equals(password)) {
            String token = "mock-token-gds";
            saveLoginInfo(username, password, token);
            Toast.makeText(this, "登录成功！（本地演示）", Toast.LENGTH_SHORT).show();
            jumpToHome();
        } else {
            Toast.makeText(this, "演示账号或密码不正确", Toast.LENGTH_SHORT).show();
        }
    }

    // 保存登录信息
    private void saveLoginInfo(String username, String password, String token) {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (cbRememberMe.isChecked()) {
            editor.putString("username", username);
            editor.putString("password", password);
            editor.putBoolean("remember", true);
        } else {
            editor.remove("username");
            editor.remove("password");
            editor.putBoolean("remember", false);
        }

        editor.putString("token", token);
        editor.apply();

        Log.d(TAG, "保存登录信息 - token: " + token + ", remember: " + cbRememberMe.isChecked());
    }

    // 注册
    private void performRegister() {
        // 同上，略（可复制 performLogin 逻辑）
    }

    // 统一跳转主页
    private void jumpToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
