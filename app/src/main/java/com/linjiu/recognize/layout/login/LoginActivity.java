package com.linjiu.recognize.layout.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    // true 时允许演示账号本地登录；发布版本务必关闭
    private static final boolean USE_MOCK_LOGIN = false;

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private CheckBox cbRememberMe;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
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

    private void autoLoginCheck() {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, MODE_PRIVATE);
        String token = sp.getString("token", null);
        if (token != null && !token.isEmpty()) {
            jumpToHome();
        }
    }

    private void loadLoginInfo() {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, MODE_PRIVATE);
        boolean remember = sp.getBoolean("remember", false);
        cbRememberMe.setChecked(remember);

        if (remember) {
            etUsername.setText(sp.getString("username", ""));
        }
    }

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
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败：服务器异常", Toast.LENGTH_SHORT).show());
                    return;
                }

                final String respBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        UserResponse resp = gson.fromJson(respBody, UserResponse.class);

                        if (resp == null || resp.data == null) {
                            Toast.makeText(LoginActivity.this, "登录失败：返回数据异常", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (resp.code == 200 && resp.data.token != null && !resp.data.token.isEmpty()) {
                            saveLoginInfo(username, resp.data.token);
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            jumpToHome();
                        } else {
                            String msg = resp.msg == null || resp.msg.isEmpty() ? "账号或密码错误" : resp.msg;
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "解析错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void mockLogin(String username, String password) {
        if ("gds".equals(username) && "123456".equals(password)) {
            String token = "mock-token-gds";
            saveLoginInfo(username, token);
            Toast.makeText(this, "登录成功（本地演示）", Toast.LENGTH_SHORT).show();
            jumpToHome();
        } else {
            Toast.makeText(this, "演示账号或密码不正确", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginInfo(String username, String token) {
        SharedPreferences sp = getSharedPreferences(AppConfig.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (cbRememberMe.isChecked()) {
            editor.putString("username", username);
            editor.putBoolean("remember", true);
        } else {
            editor.remove("username");
            editor.putBoolean("remember", false);
        }

        // 清理历史遗留的明文密码字段
        editor.remove("password");
        editor.putString("token", token);
        editor.apply();
    }

    private void performRegister() {
        // TODO: implement register flow
    }

    private void jumpToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
