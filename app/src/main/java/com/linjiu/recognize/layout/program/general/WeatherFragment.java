package com.linjiu.recognize.layout.program.general;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.linjiu.recognize.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 显示天气信息并提供有利于农作物生长的建议的 Fragment。
 * 此版本使用模拟数据，实际应用中应替换为从天气 API 获取的实时数据。
 */
public class WeatherFragment extends Fragment {

    // --- 模拟天气数据 (在实际应用中应从API获取) ---
    private static final String MOCK_CITY = "阳光农场";
    private static final String MOCK_WEATHER_CONDITION = "晴"; // 可选: 晴, 多云, 阴, 小雨, 中雨, 大雨
    private static final int MOCK_TEMPERATURE = 26; // 摄氏度
    private static final int MOCK_HUMIDITY = 65; // %
    private static final int MOCK_RAINFALL = 0; // mm
    private static final int MOCK_WIND_SPEED = 3; // m/s
    private static final String MOCK_SUNRISE = "06:15";
    private static final String MOCK_SUNSET = "18:45";

    /**
     * 创建 Fragment 的视图层次结构。
     *
     * @param inflater           用于 inflate 视图的 LayoutInflater。
     * @param container          视图将被附加到的父 ViewGroup。
     * @param savedInstanceState 如果 Fragment 正在被重新创建，则此 Bundle 包含其先前状态。
     * @return 返回 Fragment 的根视图。
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    /**
     * 在 onCreateView 返回的视图被创建后调用。
     * 在这里可以进行视图查找和设置监听器等操作。
     *
     * @param view               onCreateView 返回的根视图。
     * @param savedInstanceState 如果 Fragment 正在被重新创建，则此 Bundle 包含其先前状态。
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 查找布局中的视图组件
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView tvLocation = view.findViewById(R.id.tv_location);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvWeatherIcon = view.findViewById(R.id.tv_weather_icon);
        TextView tvTemperature = view.findViewById(R.id.tv_temperature);
        TextView tvCondition = view.findViewById(R.id.tv_condition);
        TextView tvHumidity = view.findViewById(R.id.tv_humidity);
        TextView tvRainfall = view.findViewById(R.id.tv_rainfall);
        TextView tvWindSpeed = view.findViewById(R.id.tv_wind_speed);
        TextView tvSunrise = view.findViewById(R.id.tv_sunrise);
        TextView tvSunset = view.findViewById(R.id.tv_sunset);
        TextView tvCropAdvice = view.findViewById(R.id.tv_crop_advice);

        // 设置 Toolbar 标题和返回按钮
        toolbar.setTitle("农作物天气");
        // 确保 R.drawable.ic_arrow_back 图标存在于 res/drawable 目录下
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // 点击返回按钮时，返回上一页
            }
        });

        // 调用方法更新UI显示的天气信息和建议
        updateWeatherInfo(tvLocation, tvDate, tvWeatherIcon, tvTemperature, tvCondition,
                tvHumidity, tvRainfall, tvWindSpeed, tvSunrise, tvSunset, tvCropAdvice);
    }

    /**
     * 根据模拟的天气数据更新UI组件。
     *
     * @param tvLocation     显示地点的 TextView
     * @param tvDate         显示日期的 TextView
     * @param tvWeatherIcon  显示天气图标的 TextView
     * @param tvTemperature  显示温度的 TextView
     * @param tvCondition    显示天气状况的 TextView
     * @param tvHumidity     显示湿度的 TextView
     * @param tvRainfall     显示降水量的 TextView
     * @param tvWindSpeed    显示风速的 TextView
     * @param tvSunrise      显示日出时间的 TextView
     * @param tvSunset       显示日落时间的 TextView
     * @param tvCropAdvice   显示农作物建议的 TextView
     */
    private void updateWeatherInfo(TextView tvLocation, TextView tvDate, TextView tvWeatherIcon,
                                   TextView tvTemperature, TextView tvCondition, TextView tvHumidity,
                                   TextView tvRainfall, TextView tvWindSpeed, TextView tvSunrise,
                                   TextView tvSunset, TextView tvCropAdvice) {

        // --- 设置地点和日期 ---
        tvLocation.setText(MOCK_CITY);
        String currentDate = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);

        // --- 设置天气图标和状况 ---
        // 根据模拟天气条件设置相应的 Emoji 图标
        switch (MOCK_WEATHER_CONDITION) {
            case "晴":
                tvWeatherIcon.setText("☀️");
                break;
            case "多云":
                tvWeatherIcon.setText("⛅");
                break;
            case "阴":
                tvWeatherIcon.setText("☁️");
                break;
            case "小雨":
                tvWeatherIcon.setText("🌦️");
                break;
            case "中雨":
                tvWeatherIcon.setText("🌧️");
                break;
            case "大雨":
                tvWeatherIcon.setText("⛈️");
                break;
            default:
                tvWeatherIcon.setText("🌤️"); // 默认图标
        }
        tvCondition.setText(MOCK_WEATHER_CONDITION);

        // --- 设置温度 ---
        tvTemperature.setText(String.format(Locale.getDefault(), "%d°C", MOCK_TEMPERATURE));

        // --- 设置详细天气信息 ---
        tvHumidity.setText(String.format(Locale.getDefault(), "湿度: %d%%", MOCK_HUMIDITY));
        tvRainfall.setText(String.format(Locale.getDefault(), "降水量: %d mm", MOCK_RAINFALL));
        tvWindSpeed.setText(String.format(Locale.getDefault(), "风速: %d m/s", MOCK_WIND_SPEED));
        tvSunrise.setText(String.format("日出: %s", MOCK_SUNRISE));
        tvSunset.setText(String.format("日落: %s", MOCK_SUNSET));

        // --- 生成并设置农作物生长建议 ---
        StringBuilder advice = new StringBuilder();
        advice.append("🌱 农作物生长建议:\n\n");

        // 根据天气状况提供建议
        if (MOCK_WEATHER_CONDITION.contains("晴") || MOCK_WEATHER_CONDITION.contains("多云")) {
            advice.append("✅ 天气晴朗或多云，光照充足，有利于光合作用。\n");
            advice.append("   - 适宜进行灌溉，促进作物生长。\n");
            advice.append("   - 可进行叶面施肥，提高肥料利用率。\n");
        } else if (MOCK_WEATHER_CONDITION.contains("雨")) {
            advice.append("🌧️ 正在降雨，空气湿润。\n");
            advice.append("   - 暂停灌溉，避免田间积水。\n");
            advice.append("   - 注意排水，防止作物根部腐烂。\n");
            if (MOCK_RAINFALL > 10) {
                advice.append("   - 强降雨可能引发涝灾，请及时排查沟渠。\n");
            }
        } else if (MOCK_WEATHER_CONDITION.contains("阴")) {
            advice.append("⛅ 天气阴沉，光照较弱。\n");
            advice.append("   - 作物光合作用减弱，生长可能放缓。\n");
            advice.append("   - 注意田间通风，降低病害风险。\n");
        }

        // 根据温度提供建议
        if (MOCK_TEMPERATURE > 30) {
            advice.append("🌡️ 温度偏高 (>30°C)，注意防暑降温。\n");
            advice.append("   - 可进行早晚灌溉，为作物降温。\n");
            advice.append("   - 注意防范日灼病。\n");
        } else if (MOCK_TEMPERATURE < 10) {
            advice.append("🧊 温度偏低 (<10°C)，注意防寒保暖。\n");
            advice.append("   - 可采取覆盖、熏烟等措施保温。\n");
            advice.append("   - 注意防范霜冻害。\n");
        } else {
            advice.append("🌡️ 温度适宜 (10°C - 30°C)，作物生长良好。\n");
        }

        // 根据湿度提供建议
        if (MOCK_HUMIDITY > 80) {
            advice.append("💦 湿度较高 (>80%)，病虫害易发。\n");
            advice.append("   - 注意田间排水和通风。\n");
            advice.append("   - 可适时喷洒杀菌剂预防病害。\n");
        } else if (MOCK_HUMIDITY < 40) {
            advice.append("💨 湿度较低 (<40%)，作物易失水。\n");
            advice.append("   - 增加灌溉频率，保持土壤湿润。\n");
            advice.append("   - 可在田间洒水增湿。\n");
        } else {
            advice.append("💧 湿度适中 (40% - 80%)，有利于作物生长。\n");
        }

        // 根据风速提供建议
        if (MOCK_WIND_SPEED > 10) {
            advice.append("💨 风力较大 (>10 m/s)，注意防风。\n");
            advice.append("   - 加固大棚、支架等设施。\n");
            advice.append("   - 注意防范作物倒伏。\n");
        } else if (MOCK_WIND_SPEED > 5) {
            advice.append("🌬️ 风力中等 (5-10 m/s)，注意观察作物状态。\n");
        } else {
            advice.append("🍃 风力较小 (<5 m/s)，环境相对稳定。\n");
        }

        // 日出日落信息（可用于安排农事活动）
        advice.append(String.format("\uD83D\uDD5B 日出: %s, 日落: %s。", MOCK_SUNRISE, MOCK_SUNSET));
        advice.append("\n   - 可根据日照时长合理安排农事活动时间。");

        // 设置建议文本到 TextView
        tvCropAdvice.setText(advice.toString());
    }
}



