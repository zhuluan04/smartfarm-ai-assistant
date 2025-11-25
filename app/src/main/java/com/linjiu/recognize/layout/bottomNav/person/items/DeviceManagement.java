package com.linjiu.recognize.layout.bottomNav.person.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linjiu.recognize.R;
import com.linjiu.recognize.adapter.SensorAdapter;
import com.linjiu.recognize.domain.sensor.SensorItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeviceManagement extends Fragment {

    private RecyclerView recyclerSensors;
    private SensorAdapter adapter;
    private List<SensorItem> sensorList;

    // 统计显示
    private TextView onlineCount;
    private TextView warningCount;
    private TextView offlineCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.device_management_fragment, container, false);

        // 初始化视图
        initViews(root);

        // 设置Toolbar
        MaterialToolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // 设置RecyclerView
        recyclerSensors = root.findViewById(R.id.recyclerSensors);
        recyclerSensors.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SensorAdapter(this::refreshSensor);
        recyclerSensors.setAdapter(adapter);

        // 设置悬浮按钮
        FloatingActionButton fabScan = root.findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(v -> {
            // 扫描设备功能
            // Toast.makeText(getContext(), "扫描新设备", Toast.LENGTH_SHORT).show();
        });

        loadMockData();
        updateStatistics();

        return root;
    }

    private void initViews(View root) {
        onlineCount = root.findViewById(R.id.online_count);
        warningCount = root.findViewById(R.id.warning_count);
        offlineCount = root.findViewById(R.id.offline_count);
    }

    private void loadMockData() {
        sensorList = new ArrayList<>();
        sensorList.add(new SensorItem(1, "温湿度传感器", R.drawable.ic_thermostat, "23.5 °C / 58 %", ""));
        sensorList.add(new SensorItem(2, "光照传感器", R.drawable.ic_light, "680", "lux"));
        sensorList.add(new SensorItem(3, "土壤湿度传感器", R.drawable.ic_soil, "42", "%"));
        sensorList.add(new SensorItem(4, "pH 传感器", R.drawable.ic_ph, "6.8", ""));
        sensorList.add(new SensorItem(5, "CO₂ 传感器", R.drawable.ic_co2, "420", "ppm",
                SensorItem.Status.WARNING));
        sensorList.add(new SensorItem(6, "风速传感器", R.drawable.ic_wind, "3.2", "m/s",
                SensorItem.Status.OFFLINE));

        adapter.submitList(sensorList);
    }

    private void updateStatistics() {
        int online = 0;
        int warning = 0;
        int offline = 0;

        for (SensorItem item : sensorList) {
            switch (item.getStatus()) {
                case ONLINE:
                    online++;
                    break;
                case WARNING:
                    warning++;
                    break;
                case OFFLINE:
                    offline++;
                    break;
            }
        }

        onlineCount.setText(String.valueOf(online));
        warningCount.setText(String.valueOf(warning));
        offlineCount.setText(String.valueOf(offline));
    }

    private void refreshSensor(SensorItem item) {
        Random random = new Random();
        String newValue = "";

        switch (item.getId()) {
            case 1:
                newValue = String.format("%.1f °C / %d %%",
                        18 + random.nextInt(12) + random.nextDouble(),
                        30 + random.nextInt(50));
                break;
            case 2:
                newValue = String.valueOf(100 + random.nextInt(1100));
                break;
            case 3:
                newValue = String.valueOf(20 + random.nextInt(60));
                break;
            case 4:
                newValue = String.format("%.1f", 4.5 + random.nextDouble() * 4.0);
                break;
            case 5:
                newValue = String.valueOf(300 + random.nextInt(500));
                break;
            case 6:
                newValue = String.format("%.1f", random.nextDouble() * 15);
                break;
        }

        // 更新数据
        for (int i = 0; i < sensorList.size(); i++) {
            if (sensorList.get(i).getId() == item.getId()) {
                SensorItem updated = new SensorItem(
                        item.getId(),
                        item.getName(),
                        item.getIconRes(),
                        newValue,
                        item.getUnit(),
                        item.getStatus()
                );
                sensorList.set(i, updated);
                adapter.submitList(new ArrayList<>(sensorList));
                break;
            }
        }
    }
}