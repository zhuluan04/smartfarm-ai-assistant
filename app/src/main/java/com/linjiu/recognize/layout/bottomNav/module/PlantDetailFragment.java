package com.linjiu.recognize.layout.bottomNav.module;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

// 点击查看植物详情页面
public class PlantDetailFragment extends Fragment {

    private static final String ARG_PLANT_NAME = "plant_name";
    private static final String ARG_PLANT_IMAGE = "plant_image";

    private String plantName;
    private int plantImageRes;

    public static PlantDetailFragment newInstance(String name, int imageRes) {
        PlantDetailFragment fragment = new PlantDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLANT_NAME, name);
        args.putInt(ARG_PLANT_IMAGE, imageRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plantName = getArguments().getString(ARG_PLANT_NAME);
            plantImageRes = getArguments().getInt(ARG_PLANT_IMAGE, R.drawable.ic_launcher_foreground);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_detail, container, false);

        ImageView backButton = view.findViewById(R.id.backButton);
        ImageView plantImage = view.findViewById(R.id.detailPlantImage);
        TextView nameText = view.findViewById(R.id.detailPlantName);
        TextView descText = view.findViewById(R.id.detailPlantDescription);

        // 设置植物数据
        nameText.setText(plantName != null ? plantName : "未知植物");
        plantImage.setImageResource(plantImageRes);

        String description = "关于 " + plantName + " 的养护信息：\n\n" +
                "- 喜欢阳光，避免过度浇水\n" +
                "- 保持土壤湿润\n" +
                "- 定期修剪枯枝";
        descText.setText(description);

        // 返回按钮
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }
}
