package com.linjiu.recognize.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.plant.Plant;
import com.linjiu.recognize.layout.bottomNav.module.PlantDetailFragment;

import java.util.ArrayList;
import java.util.List;

// 植物适配器
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private List<Plant> plantListFull;

    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
        this.plantListFull = new ArrayList<>(plantList);
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    /**
     * 绑定Holder
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.plantName.setText(plant.getName());
        holder.plantImage.setImageResource(plant.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            // 根据植物名称和图片资源创建一个详情页面
            Fragment fragment = PlantDetailFragment.newInstance(plant.getName(), plant.getImageResId());
            ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment) // fragment_container 是你 Activity 的容器布局
                    .addToBackStack(null) // 加入返回栈
                    .commit();
        });

    }


    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public void filter(String text) {
        plantList.clear();
        if (text.isEmpty()) {
            plantList.addAll(plantListFull);
        } else {
            text = text.toLowerCase();
            for (Plant item : plantListFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    plantList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView plantName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            plantName = itemView.findViewById(R.id.plantName);
        }
    }
}
