package com.linjiu.recognize.layout.bottomNav.person.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;

public class SystemOverview extends Fragment {

    private TextView systemStatusText;
    private TextView deviceCountText;
    private TextView sensorCountText;
    private TextView alertCountText;
    private RecyclerView sensorDataList;
    private SensorDataAdapter adapter;
    private List<SensorData> sensorDataListData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_overview_fragment, container, false);

        initViews(view);
        setupRecyclerView();
        updateSystemStatus();
        loadSensorData();

        return view;
    }

    private void initViews(View view) {
        systemStatusText = view.findViewById(R.id.system_status_text);
        deviceCountText = view.findViewById(R.id.device_count_text);
        sensorCountText = view.findViewById(R.id.sensor_count_text);
        alertCountText = view.findViewById(R.id.alert_count_text);
        sensorDataList = view.findViewById(R.id.sensor_data_list);
    }

    private void setupRecyclerView() {
        sensorDataListData = new ArrayList<>();
        adapter = new SensorDataAdapter(sensorDataListData);
        sensorDataList.setLayoutManager(new LinearLayoutManager(getContext()));
        sensorDataList.setAdapter(adapter);
    }

    private void updateSystemStatus() {
        // 模拟系统状态更新
        systemStatusText.setText("运行正常");
        deviceCountText.setText("12");
        sensorCountText.setText("24");
        alertCountText.setText("2");
    }

    private void loadSensorData() {
        // 模拟传感器数据
        sensorDataListData.add(new SensorData("土壤湿度", "45%", "正常", R.drawable.ic_water_drop));
        sensorDataListData.add(new SensorData("空气温度", "23.5°C", "正常", R.drawable.ic_thermometer));
        sensorDataListData.add(new SensorData("光照强度", "8500 Lux", "正常", R.drawable.ic_sun));
        sensorDataListData.add(new SensorData("空气湿度", "65%", "正常", R.drawable.ic_humidity));
        sensorDataListData.add(new SensorData("PH值", "6.8", "正常", R.drawable.ic_ph));
        sensorDataListData.add(new SensorData("EC值", "1.2 mS/cm", "正常", R.drawable.ic_ec));

        adapter.notifyDataSetChanged();
    }

    // 传感器数据模型
    public static class SensorData {
        private String name;
        private String value;
        private String status;
        private int iconRes;

        public SensorData(String name, String value, String status, int iconRes) {
            this.name = name;
            this.value = value;
            this.status = status;
            this.iconRes = iconRes;
        }

        public String getName() { return name; }
        public String getValue() { return value; }
        public String getStatus() { return status; }
        public int getIconRes() { return iconRes; }
    }

    // 传感器数据适配器
    public class SensorDataAdapter extends RecyclerView.Adapter<SensorDataAdapter.ViewHolder> {
        private List<SensorData> sensorDataList;

        public SensorDataAdapter(List<SensorData> sensorDataList) {
            this.sensorDataList = sensorDataList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sensor_data, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SensorData data = sensorDataList.get(position);
            holder.sensorName.setText(data.getName());
            holder.sensorValue.setText(data.getValue());
            holder.sensorStatus.setText(data.getStatus());

            // 根据状态设置颜色和背景
            if ("正常".equals(data.getStatus())) {
                holder.sensorStatus.setTextColor(0xFFFFFFFF); // 白色
                holder.sensorStatus.setBackgroundResource(R.drawable.status_badge_background);
            } else if ("警告".equals(data.getStatus())) {
                holder.sensorStatus.setTextColor(0xFFFFFFFF); // 白色
                holder.sensorStatus.setBackgroundResource(R.drawable.status_badge_warning);
            } else {
                holder.sensorStatus.setTextColor(0xFFFFFFFF); // 白色
                holder.sensorStatus.setBackgroundResource(R.drawable.status_badge_error);
            }

            holder.sensorIcon.setImageResource(data.getIconRes());

            // 添加点击动画效果
            holder.itemView.setOnClickListener(v -> {
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();
            });
        }

        @Override
        public int getItemCount() {
            return sensorDataList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView sensorName;
            TextView sensorValue;
            TextView sensorStatus;
            android.widget.ImageView sensorIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                sensorName = itemView.findViewById(R.id.sensor_name);
                sensorValue = itemView.findViewById(R.id.sensor_value);
                sensorStatus = itemView.findViewById(R.id.sensor_status);
                sensorIcon = itemView.findViewById(R.id.sensor_icon);
            }
        }
    }
}



