package com.linjiu.recognize.layout.bottomNav.module;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

public class PlantEnvironmentFragment extends Fragment {

    // 传感器数据显示
    private TextView tvTemperatureValue, tvHumidityValue, tvLightValue, tvSoilMoistureValue;

    // 设备控制卡片和图标
    private CardView cardWaterPump, cardFan, cardLed, cardHeater;
    private ImageView ivWaterPump, ivFan, ivLed, ivHeater;

    // 参数设定
    private SeekBar seekbarTargetTemp, seekbarTargetHumidity, seekbarLightIntensity;
    private TextView tvTargetTemp, tvTargetHumidity, tvLightIntensity;

    // 设备状态
    private boolean isWaterPumpOn = false;
    private boolean isFanOn = false;
    private boolean isLedOn = false;
    private boolean isHeaterOn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modules_environment, container, false);

        initViews(view);
        setupDeviceCardListeners();
        setupSeekBars();

        return view;
    }

    private void initViews(View view) {
        // 传感器数据
        tvTemperatureValue = view.findViewById(R.id.tv_temperature_value);
        tvHumidityValue = view.findViewById(R.id.tv_humidity_value);
        tvLightValue = view.findViewById(R.id.tv_light_value);
        tvSoilMoistureValue = view.findViewById(R.id.tv_soil_moisture_value);

        // 设备控制卡片
        cardWaterPump = view.findViewById(R.id.card_water_pump);
        cardFan = view.findViewById(R.id.card_fan);
        cardLed = view.findViewById(R.id.card_led);
        cardHeater = view.findViewById(R.id.card_heater);

        ivWaterPump = view.findViewById(R.id.iv_water_pump);
        ivFan = view.findViewById(R.id.iv_fan);
        ivLed = view.findViewById(R.id.iv_led);
        ivHeater = view.findViewById(R.id.iv_heater);

        // 参数设定
        seekbarTargetTemp = view.findViewById(R.id.seekbar_target_temp);
        seekbarTargetHumidity = view.findViewById(R.id.seekbar_target_humidity);
        seekbarLightIntensity = view.findViewById(R.id.seekbar_light_intensity);

        tvTargetTemp = view.findViewById(R.id.tv_target_temp);
        tvTargetHumidity = view.findViewById(R.id.tv_target_humidity);
        tvLightIntensity = view.findViewById(R.id.tv_light_intensity);
    }

    private void setupDeviceCardListeners() {
        cardWaterPump.setOnClickListener(v -> {
            isWaterPumpOn = !isWaterPumpOn;
            ivWaterPump.setImageResource(isWaterPumpOn ? R.drawable.ic_water_pump_on : R.drawable.ic_water_pump_off);
            Toast.makeText(getContext(), "水泵 " + (isWaterPumpOn ? "已开启" : "已关闭"), Toast.LENGTH_SHORT).show();
        });

        cardFan.setOnClickListener(v -> {
            isFanOn = !isFanOn;
            ivFan.setImageResource(isFanOn ? R.drawable.ic_fan_on : R.drawable.ic_fan_off);
            Toast.makeText(getContext(), "风扇 " + (isFanOn ? "已开启" : "已关闭"), Toast.LENGTH_SHORT).show();
        });

        cardLed.setOnClickListener(v -> {
            isLedOn = !isLedOn;
            ivLed.setImageResource(isLedOn ? R.drawable.ic_led_on : R.drawable.ic_led_off);
            Toast.makeText(getContext(), "LED灯 " + (isLedOn ? "已开启" : "已关闭"), Toast.LENGTH_SHORT).show();
        });

        cardHeater.setOnClickListener(v -> {
            isHeaterOn = !isHeaterOn;
            ivHeater.setImageResource(isHeaterOn ? R.drawable.ic_heater_on : R.drawable.ic_heater_off);
            Toast.makeText(getContext(), "加热器 " + (isHeaterOn ? "已开启" : "已关闭"), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSeekBars() {
        seekbarTargetTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int targetTemp = progress + 10; // 匹配刻度范围 10-40
                tvTargetTemp.setText(targetTemp + "°C");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarTargetHumidity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTargetHumidity.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarLightIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int lux = progress * 10; // 0-100 → 0-1000 lux
                tvLightIntensity.setText(lux + " lux");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
