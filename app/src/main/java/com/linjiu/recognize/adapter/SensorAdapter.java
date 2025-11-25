package com.linjiu.recognize.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.sensor.SensorItem;

import java.util.function.Consumer;

public class SensorAdapter extends ListAdapter<SensorItem, SensorAdapter.ViewHolder> {

    private final Consumer<SensorItem> onRefreshClick;

    public SensorAdapter(Consumer<SensorItem> onRefreshClick) {
        super(DIFF_CALLBACK);
        this.onRefreshClick = onRefreshClick;
    }

    private static final DiffUtil.ItemCallback<SensorItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SensorItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull SensorItem oldItem, @NonNull SensorItem newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull SensorItem oldItem, @NonNull SensorItem newItem) {
                    return oldItem.getValue().equals(newItem.getValue()) &&
                            oldItem.getStatus() == newItem.getStatus();
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SensorItem item = getItem(position);
        holder.bind(item, onRefreshClick);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View statusIndicator;
        private final LinearLayout iconContainer;
        private final ImageView sensorIcon;
        private final TextView sensorName;
        private final View statusDot;
        private final TextView sensorStatusText;
        private final TextView sensorValue;
        private final TextView sensorUnit;
        private final TextView sensorSecondaryValue;
        private final TextView lastUpdateTime;
        private final LinearLayout btnDetail;
        private final LinearLayout btnRefresh;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            iconContainer = itemView.findViewById(R.id.icon_container);
            sensorIcon = itemView.findViewById(R.id.sensor_icon);
            sensorName = itemView.findViewById(R.id.sensor_name);
            statusDot = itemView.findViewById(R.id.status_dot);
            sensorStatusText = itemView.findViewById(R.id.sensor_status_text);
            sensorValue = itemView.findViewById(R.id.sensor_value);
            sensorUnit = itemView.findViewById(R.id.sensor_unit);
            sensorSecondaryValue = itemView.findViewById(R.id.sensor_secondary_value);
            lastUpdateTime = itemView.findViewById(R.id.last_update_time);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            btnRefresh = itemView.findViewById(R.id.btn_refresh);
        }

        public void bind(SensorItem item, Consumer<SensorItem> onRefreshClick) {
            // 设置基本信息
            sensorName.setText(item.getName());
            sensorIcon.setImageResource(item.getIconRes());

            // 处理数值显示
            String valueStr = item.getValue();
            if (item.getId() == 1) { // 温湿度传感器特殊处理
                String[] parts = valueStr.split("/");
                if (parts.length == 2) {
                    sensorValue.setText(parts[0].trim());
                    sensorUnit.setText("");
                    sensorSecondaryValue.setText("湿度: " + parts[1].trim());
                    sensorSecondaryValue.setVisibility(View.VISIBLE);
                } else {
                    sensorValue.setText(valueStr);
                    sensorUnit.setText(item.getUnit());
                    sensorSecondaryValue.setVisibility(View.GONE);
                }
            } else {
                sensorValue.setText(valueStr);
                sensorUnit.setText(item.getUnit());
                sensorSecondaryValue.setVisibility(View.GONE);
            }

            // 设置状态样式
            switch (item.getStatus()) {
                case ONLINE:
                    statusIndicator.setBackgroundResource(R.drawable.status_gradient_normal);
                    iconContainer.setBackgroundResource(R.drawable.sensor_icon_bg_normal);
                    statusDot.setBackgroundResource(R.drawable.status_dot_normal);
                    sensorStatusText.setText("正常");
                    sensorStatusText.setTextColor(0xFF4CAF50);
                    // 设置状态标签背景 - 需要获取父容器
                    ((ViewGroup)statusDot.getParent()).setBackgroundResource(R.drawable.status_bg_normal);
                    break;

                case WARNING:
                    statusIndicator.setBackgroundResource(R.drawable.status_gradient_warning);
                    iconContainer.setBackgroundResource(R.drawable.sensor_icon_bg_warning);
                    statusDot.setBackgroundResource(R.drawable.status_dot_warning);
                    sensorStatusText.setText("告警");
                    sensorStatusText.setTextColor(0xFFFF6F00);
                    ((ViewGroup)statusDot.getParent()).setBackgroundResource(R.drawable.status_bg_warning);
                    break;

                case OFFLINE:
                    statusIndicator.setBackgroundResource(R.drawable.status_gradient_offline);
                    iconContainer.setBackgroundResource(R.drawable.sensor_icon_bg_offline);
                    statusDot.setBackgroundResource(R.drawable.status_dot_offline);
                    sensorStatusText.setText("离线");
                    sensorStatusText.setTextColor(0xFF757575);
                    ((ViewGroup)statusDot.getParent()).setBackgroundResource(R.drawable.status_bg_offline);
                    break;
            }

            // 设置更新时间（这里简单显示固定文本，实际应用中应该是真实时间）
            lastUpdateTime.setText("刚刚");

            // 设置按钮点击事件
            btnRefresh.setOnClickListener(v -> {
                if (onRefreshClick != null) {
                    onRefreshClick.accept(item);
                    lastUpdateTime.setText("刚刚");
                }
            });

            btnDetail.setOnClickListener(v -> {
                // 详情点击事件 - 可以跳转到详情页面
                // Toast.makeText(v.getContext(), "查看" + item.getName() + "详情", Toast.LENGTH_SHORT).show();
            });
        }
    }
}