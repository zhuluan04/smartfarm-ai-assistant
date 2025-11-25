package com.linjiu.recognize.layout.home.agent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;
import com.linjiu.recognize.adapter.ChatAdapter;
import com.linjiu.recognize.config.HttpUrlConnectConfig;
import com.linjiu.recognize.domain.ai.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private OkHttpClient client = new OkHttpClient();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("小农助手");
        }

        initViews();
        setupRecyclerView();
        setupSendButton();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_chat);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSend = findViewById(R.id.button_send);
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        chatList.add(new ChatMessage("您好！我是您的智能农业助手，有什么可以帮您？", false));

        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> sendMessage());
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            // 1. 添加用户消息
            chatList.add(new ChatMessage(message, true));
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.scrollToPosition(chatList.size() - 1);
            editTextMessage.setText("");

            // 2. 添加“AI正在思考...”占位消息
            ChatMessage placeholder = new ChatMessage("小农正在思考🤔...", false);
            chatList.add(placeholder);
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.scrollToPosition(chatList.size() - 1);

            // 3. 发起网络请求
            callAiApi(message, chatList.size() - 1); // 传入占位消息的位置
        }
    }

    private void callAiApi(String userMessage, int placeholderIndex) {
        try {
            JSONObject json = new JSONObject();
            json.put("msg", userMessage);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(HttpUrlConnectConfig.AI_CHAT_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> {
                        Toast.makeText(AiChatActivity.this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // 替换占位符为错误提示
                        chatList.set(placeholderIndex, new ChatMessage("❌ 网络错误，请重试", false));
                        chatAdapter.notifyItemChanged(placeholderIndex);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        mainHandler.post(() -> {
                            chatList.set(placeholderIndex, new ChatMessage("⚠️ 服务器返回错误", false));
                            chatAdapter.notifyItemChanged(placeholderIndex);
                        });
                        return ;
                    }

                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String aiReply = jsonResponse.getString("response"); // 根据你的后端返回结构调整
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            mainHandler.post(() -> {
                                // 替换占位消息为真实回复
                                chatList.set(placeholderIndex, new ChatMessage(aiReply, false));
                                chatAdapter.notifyItemChanged(placeholderIndex);
                                recyclerView.scrollToPosition(chatList.size() - 1);
                            });
                        } else {
                            mainHandler.post(() -> {
                                chatList.set(placeholderIndex, new ChatMessage("⚠️ AI 服务异常", false));
                                chatAdapter.notifyItemChanged(placeholderIndex);
                            });
                        }
                    } catch (JSONException e) {
                        mainHandler.post(() -> {
                            chatList.set(placeholderIndex, new ChatMessage("⚠️ 解析失败", false));
                            chatAdapter.notifyItemChanged(placeholderIndex);
                        });
                    }
                    return ;
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            mainHandler.post(() -> Toast.makeText(this, "构造请求失败", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}