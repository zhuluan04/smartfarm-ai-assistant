package com.linjiu.recognize.layout.bottomNav.person.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettings extends Fragment {

    private RecyclerView notificationSettingsList;
    private NotificationSettingsAdapter adapter;
    private List<NotificationSettingItem> settingsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_settings_fragment, container, false);

        initViews(view);
        setupRecyclerView();
        loadNotificationSettings();

        return view;
    }

    private void initViews(View view) {
        notificationSettingsList = view.findViewById(R.id.notification_settings_list);
    }

    private void setupRecyclerView() {
        settingsList = new ArrayList<>();
        adapter = new NotificationSettingsAdapter(settingsList);
        notificationSettingsList.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationSettingsList.setAdapter(adapter);
    }

    private void loadNotificationSettings() {
        // 传感器异常警报设置
        settingsList.add(new NotificationSettingItem("传感器异常警报", "当传感器数据异常时发送通知", true));
        settingsList.add(new NotificationSettingItem("土壤湿度异常", "土壤湿度过高或过低时提醒", true));
        settingsList.add(new NotificationSettingItem("温度异常", "温度超出正常范围时提醒", true));
        settingsList.add(new NotificationSettingItem("湿度异常", "空气湿度异常时提醒", true));
        settingsList.add(new NotificationSettingItem("PH值异常", "PH值不在正常范围内时提醒", true));
        settingsList.add(new NotificationSettingItem("EC值异常", "电导率异常时提醒", true));

        // 设备状态提醒
        settingsList.add(new NotificationSettingItem("设备离线提醒", "设备断线时发送通知", true));
        settingsList.add(new NotificationSettingItem("设备故障提醒", "设备故障时发送通知", true));

        // 定时提醒设置
        settingsList.add(new NotificationSettingItem("定时数据报告", "每日定时发送数据汇总报告", true));
        settingsList.add(new NotificationSettingItem("灌溉提醒", "根据土壤湿度定时提醒灌溉", true));
        settingsList.add(new NotificationSettingItem("施肥提醒", "根据土壤养分数据提醒施肥", true));
        settingsList.add(new NotificationSettingItem("病虫害预警", "根据环境数据预测病虫害风险", true));

        // 通知方式设置
        settingsList.add(new NotificationSettingItem("推送通知", "在手机上显示推送通知", true));
        settingsList.add(new NotificationSettingItem("短信通知", "通过短信发送重要提醒", false));
        settingsList.add(new NotificationSettingItem("邮件通知", "通过邮件发送详细报告", true));

        adapter.notifyDataSetChanged();
    }

    // 通知设置项模型
    public static class NotificationSettingItem {
        private String title;
        private String description;
        private boolean enabled;

        public NotificationSettingItem(String title, String description, boolean enabled) {
            this.title = title;
            this.description = description;
            this.enabled = enabled;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    // 通知设置适配器
    public class NotificationSettingsAdapter extends RecyclerView.Adapter<NotificationSettingsAdapter.ViewHolder> {
        private List<NotificationSettingItem> settingsList;

        public NotificationSettingsAdapter(List<NotificationSettingItem> settingsList) {
            this.settingsList = settingsList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_setting, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationSettingItem item = settingsList.get(position);
            holder.titleText.setText(item.getTitle());
            holder.descriptionText.setText(item.getDescription());
            holder.switchButton.setChecked(item.isEnabled());

            holder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.setEnabled(isChecked);
                    // 这里可以添加保存设置的逻辑
                    saveNotificationSetting(item.getTitle(), isChecked);
                }
            });
        }

        private void saveNotificationSetting(String title, boolean enabled) {
            // 实现保存通知设置的逻辑
            // 例如：保存到SharedPreferences或数据库
        }

        @Override
        public int getItemCount() {
            return settingsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView descriptionText;
            Switch switchButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.setting_title);
                descriptionText = itemView.findViewById(R.id.setting_description);
                switchButton = itemView.findViewById(R.id.setting_switch);
            }
        }
    }
}



