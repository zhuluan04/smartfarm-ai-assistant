package com.linjiu.recognize.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;

import java.util.List;

public class PlantHistoryAdapter extends RecyclerView.Adapter<PlantHistoryAdapter.ViewHolder> {

    private List<String> data;

    public PlantHistoryAdapter(List<String> data) {
        // 防御性编程：防止传入 null
        this.data = data != null ? data : new java.util.ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = data.get(position);
        holder.bind(text);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }

        void bind(String text) {
            // 防止 setText(null) 崩溃
            textView.setText(text != null ? text : "数据异常");
        }
    }
}