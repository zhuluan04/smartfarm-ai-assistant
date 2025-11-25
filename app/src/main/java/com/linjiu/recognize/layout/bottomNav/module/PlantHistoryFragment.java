package com.linjiu.recognize.layout.bottomNav.module;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linjiu.recognize.R;
import com.linjiu.recognize.adapter.PlantHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

// 历史分析
public class PlantHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantHistoryAdapter adapter;
    private List<String> historyData;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddEntry;
    private TextView emptyView; // 新增：空状态提示
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化UI组件
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        progressBar = view.findViewById(R.id.loadingProgressBar);
        fabAddEntry = view.findViewById(R.id.fabAddEntry);
        emptyView = view.findViewById(R.id.emptyView); // 绑定空状态视图
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // 初始化数据容器（防御性初始化）
        historyData = new ArrayList<>();

        // 创建适配器（内部已处理 null 数据）
        adapter = new PlantHistoryAdapter(historyData);
        recyclerView.setAdapter(adapter);

        // 设置空数据监听器
        setupEmptyView();

        // 模拟异步加载数据（未来替换为真实数据源）
        loadData();

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadData);

        // 浮动按钮点击事件
        fabAddEntry.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "添加新记录（功能待实现）", Toast.LENGTH_SHORT).show();
            // TODO: 实现添加新记录的逻辑
        });
    }

    private void setupEmptyView() {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateEmptyViewVisibility();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateEmptyViewVisibility();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateEmptyViewVisibility();
            }
        });
    }

    private void updateEmptyViewVisibility() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        // 显示加载状态
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // 模拟异步加载（替换为你的真实数据源）
        new Thread(() -> {
            try {
                Thread.sleep(800); // 模拟网络延迟

                // ========== 模拟数据源 ==========
                final List<String>[] loadedData = new List[]{simulateFetchData()};
                // 如果 loadedData == null 或 empty，也不会崩溃

                requireActivity().runOnUiThread(() -> {
                    // 关闭刷新和加载状态
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);

                    // 更新数据（安全方式）
                    if (loadedData[0] == null) {
                        loadedData[0] = new ArrayList<>();
                        Toast.makeText(requireContext(), "数据加载失败", Toast.LENGTH_SHORT).show();
                    }

                    historyData.clear();
                    historyData.addAll(loadedData[0]);
                    adapter.notifyDataSetChanged();

                    // 自动更新空状态
                    updateEmptyViewVisibility();
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "加载异常，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    // 👇 模拟数据获取（未来替换成数据库/网络请求）
    private List<String> simulateFetchData() {
        List<String> data = new ArrayList<>();
        // 模拟“无数据”场景：注释掉下面几行，测试空列表
        data.add("2025-09-01：高度 12cm，湿度 65%，温度 23℃");
        data.add("2025-08-28：高度 10cm，湿度 62%，温度 22℃");
        data.add("2025-08-20：高度 8cm，湿度 60%，温度 21℃");

        // 可选：测试空数据
        // data.clear();

        // 可选：测试包含 null 数据（不应崩溃）
        // data.add(null);

        return data;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 避免内存泄漏（如果使用了复杂监听器，可在此移除）
    }
}