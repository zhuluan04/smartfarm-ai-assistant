package com.linjiu.recognize.layout.bottomNav.person.items;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.linjiu.recognize.R;
import android.widget.TextView;

public class AccountSettingsFragment extends Fragment {

    private TextView usernameText;
    private TextView emailText;
    private TextView phoneText;
    private TextView accountTypeText;
    private MaterialButton logoutButton;
    private MaterialButton deleteAccountButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners(view);
        loadAccountInfo();
    }

    private void initViews(View view) {
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        phoneText = view.findViewById(R.id.phone_text);
        accountTypeText = view.findViewById(R.id.account_type_text);
        logoutButton = view.findViewById(R.id.logout_button);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
    }

    private void setupClickListeners(View view) {
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmation());

        view.findViewById(R.id.item_nickname).setOnClickListener(v ->
                showEditDialog("用户名", usernameText.getText().toString(),
                        value -> usernameText.setText(value)));

        view.findViewById(R.id.item_email).setOnClickListener(v ->
                showEditDialog("邮箱", emailText.getText().toString(),
                        value -> emailText.setText(value)));

        view.findViewById(R.id.item_phone).setOnClickListener(v ->
                showEditDialog("手机号", phoneText.getText().toString(),
                        value -> phoneText.setText(value)));
    }

    private void loadAccountInfo() {
        // TODO 未来可以改成从服务器/本地账号数据里读取
        usernameText.setText("张三");
        emailText.setText("zhangsan@example.com");
        phoneText.setText("138****8888");
        accountTypeText.setText("普通用户");
    }

    private void showEditDialog(String title, String currentValue, OnValueChangeListener listener) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_text, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText editText = dialogView.findViewById(R.id.edit_text);

        if (editText == null) {
            Toast.makeText(requireContext(), "编辑框加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        editText.setText(currentValue);
        editText.setSelection(currentValue.length());

        new AlertDialog.Builder(requireContext())
                .setTitle("编辑" + title)
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newValue = editText.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        listener.onValueChanged(newValue);
                        Toast.makeText(requireContext(), title + "已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "请输入有效的" + title, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出当前账户吗？")
                .setPositiveButton("确定", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除账户警告")
                .setMessage("删除账户将永久清除您的所有数据，此操作不可恢复！\n\n请输入您的密码以确认删除：")
                .setView(R.layout.dialog_edit_text)
                .setPositiveButton("确认删除", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        // TODO: 清除token + 跳转到登录页
    }

    private void performDeleteAccount() {
        Toast.makeText(requireContext(), "账户已删除", Toast.LENGTH_SHORT).show();
        // TODO: 调用后端删除接口，跳转到欢迎页
    }

    public interface OnValueChangeListener {
        void onValueChanged(String newValue);
    }
}
