package com.linjiu.recognize.layout.bottomNav.person.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;

public class AiManagement extends Fragment {

    private RecyclerView modelManagementList;
    private ModelManagementAdapter adapter;
    private List<ModelItem> modelList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ai_management_fragment, container, false);

        initViews(view);
        setupRecyclerView();
        loadModelData();

        return view;
    }

    private void initViews(View view) {
        modelManagementList = view.findViewById(R.id.model_management_list);
    }

    private void setupRecyclerView() {
        modelList = new ArrayList<>();
        adapter = new ModelManagementAdapter(modelList);
        modelManagementList.setLayoutManager(new LinearLayoutManager(getContext()));
        modelManagementList.setAdapter(adapter);
    }

    private void loadModelData() {
        // 植物识别模型
        modelList.add(new ModelItem("植物识别模型", "识别作物种类、杂草类型", "v2.1.0", 92, "active"));
        modelList.add(new ModelItem("病虫害识别模型", "检测植物病虫害类型", "v1.8.5", 88, "active"));
        modelList.add(new ModelItem("生长阶段分析模型", "分析作物生长阶段", "v1.5.2", 95, "active"));
        modelList.add(new ModelItem("产量预测模型", "预测作物产量", "v1.2.0", 85, "inactive"));
        modelList.add(new ModelItem("营养状况分析模型", "评估植物营养状况", "v1.0.3", 89, "active"));
        modelList.add(new ModelItem("环境适应性模型", "分析作物环境适应性", "v0.9.8", 82, "inactive"));
        modelList.add(new ModelItem("成熟度检测模型", "检测作物成熟度", "v1.3.7", 91, "active"));
        modelList.add(new ModelItem("品种优化建议模型", "提供品种优化建议", "v1.1.1", 87, "inactive"));

        adapter.notifyDataSetChanged();
    }

    // AI模型数据模型
    public static class ModelItem {
        private String name;
        private String description;
        private String version;
        private int accuracy;
        private String status; // active, inactive, updating

        public ModelItem(String name, String description, String version, int accuracy, String status) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.accuracy = accuracy;
            this.status = status;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getVersion() { return version; }
        public int getAccuracy() { return accuracy; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // AI模型管理适配器
    public class ModelManagementAdapter extends RecyclerView.Adapter<ModelManagementAdapter.ViewHolder> {
        private List<ModelItem> modelList;

        public ModelManagementAdapter(List<ModelItem> modelList) {
            this.modelList = modelList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai_model, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModelItem model = modelList.get(position);
            holder.modelName.setText(model.getName());
            holder.modelDescription.setText(model.getDescription());
            holder.modelVersion.setText(model.getVersion());
            holder.modelAccuracy.setText(model.getAccuracy() + "%");

            // 设置模型状态
            if ("active".equals(model.getStatus())) {
                holder.modelStatus.setText("运行中");
                holder.modelStatus.setTextColor(0xFF4CAF50); // 绿色
                holder.statusContainer.setBackgroundResource(R.drawable.status_active_bg);
            } else if ("inactive".equals(model.getStatus())) {
                holder.modelStatus.setText("已停用");
                holder.modelStatus.setTextColor(0xFF757575); // 灰色
                holder.statusContainer.setBackgroundResource(R.drawable.status_inactive_bg);
            } else {
                holder.modelStatus.setText("更新中");
                holder.modelStatus.setTextColor(0xFF2196F3); // 蓝色
                holder.statusContainer.setBackgroundResource(R.drawable.status_updating_bg);
            }

            // 按钮事件处理 - 注意：现在点击的是LinearLayout容器
            holder.activateButton.setOnClickListener(v -> {
                if ("inactive".equals(model.getStatus())) {
                    model.setStatus("active");
                    holder.modelStatus.setText("运行中");
                    holder.modelStatus.setTextColor(0xFF4CAF50);
                    holder.statusContainer.setBackgroundResource(R.drawable.status_active_bg);
                    holder.activateButtonText.setText("停用");
                } else if ("active".equals(model.getStatus())) {
                    model.setStatus("inactive");
                    holder.modelStatus.setText("已停用");
                    holder.modelStatus.setTextColor(0xFF757575);
                    holder.statusContainer.setBackgroundResource(R.drawable.status_inactive_bg);
                    holder.activateButtonText.setText("启用");
                }
            });

            holder.updateButton.setOnClickListener(v -> {
                model.setStatus("updating");
                holder.modelStatus.setText("更新中");
                holder.modelStatus.setTextColor(0xFF2196F3);
                holder.statusContainer.setBackgroundResource(R.drawable.status_updating_bg);
                holder.updateProgressContainer.setVisibility(View.VISIBLE);

                // 模拟更新过程
                simulateModelUpdate(holder.updateProgress, holder.updateProgressText, () -> {
                    model.setStatus("active");
                    holder.modelStatus.setText("运行中");
                    holder.modelStatus.setTextColor(0xFF4CAF50);
                    holder.statusContainer.setBackgroundResource(R.drawable.status_active_bg);
                    holder.updateProgressContainer.setVisibility(View.GONE);
                });
            });

            // 根据状态设置按钮文本
            if ("active".equals(model.getStatus())) {
                holder.activateButtonText.setText("停用");
            } else {
                holder.activateButtonText.setText("启用");
            }
        }

        private void simulateModelUpdate(ProgressBar progressBar, TextView progressText, Runnable onComplete) {
            // 模拟模型更新进度
            new Thread(() -> {
                for (int i = 0; i <= 100; i += 10) {
                    try {
                        Thread.sleep(200); // 模拟更新延迟
                        if (getContext() != null) {
                            int finalI = i;
                            getActivity().runOnUiThread(() -> {
                                progressBar.setProgress(finalI);
                                progressText.setText(finalI + "%");
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (getContext() != null) {
                    getActivity().runOnUiThread(onComplete);
                }
            }).start();
        }

        @Override
        public int getItemCount() {
            return modelList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView modelName;
            TextView modelDescription;
            TextView modelVersion;
            TextView modelAccuracy;
            TextView modelStatus;
            LinearLayout statusContainer;
            LinearLayout activateButton;  // 改为LinearLayout
            LinearLayout updateButton;     // 改为LinearLayout
            TextView activateButtonText;   // 新增：按钮内的文本
            LinearLayout updateProgressContainer;  // 新增：进度条容器
            ProgressBar updateProgress;
            TextView updateProgressText;   // 新增：进度百分比文本

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                modelName = itemView.findViewById(R.id.model_name);
                modelDescription = itemView.findViewById(R.id.model_description);
                modelVersion = itemView.findViewById(R.id.model_version);
                modelAccuracy = itemView.findViewById(R.id.model_accuracy);
                modelStatus = itemView.findViewById(R.id.model_status);
                statusContainer = (LinearLayout) itemView.findViewById(R.id.model_status).getParent();
                activateButton = itemView.findViewById(R.id.activate_button);
                updateButton = itemView.findViewById(R.id.update_button);
                activateButtonText = itemView.findViewById(R.id.activate_button_text);
                updateProgressContainer = itemView.findViewById(R.id.update_progress_container);
                updateProgress = itemView.findViewById(R.id.update_progress);
                updateProgressText = itemView.findViewById(R.id.update_progress_text);
            }
        }
    }
}