package com.linjiu.recognize.layout.bottomNav.module;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.plant.Plant;
import com.linjiu.recognize.adapter.PlantAdapter;

import java.util.ArrayList;
import java.util.List;

// 植物库
public class PlantBotanyFragment extends Fragment {

    private RecyclerView recyclerView;

    private EditText searchBar;

    private ImageView backButton;

    private PlantAdapter adapter;

    private List<Plant> plantList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modules_botany, container, false);

        recyclerView = view.findViewById(R.id.plantGrid);
        searchBar = view.findViewById(R.id.searchBar);
        backButton = view.findViewById(R.id.backButton);

        // 模拟植物数据
        plantList = new ArrayList<>();
        plantList.add(new Plant("玫瑰", R.drawable.ic_rose));
        plantList.add(new Plant("向日葵", R.drawable.ic_sunflower));
        plantList.add(new Plant("仙人掌", R.drawable.ic_cactus));
        plantList.add(new Plant("薄荷", R.drawable.ic_mint));

        adapter = new PlantAdapter(plantList);
        // 设置网格布局
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        // 返回按钮点击
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // 搜索功能
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        return view;
    }
}
