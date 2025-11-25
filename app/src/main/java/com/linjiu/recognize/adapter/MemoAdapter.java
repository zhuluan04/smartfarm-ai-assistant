package com.linjiu.recognize.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.memo.MemoBean;
import com.linjiu.recognize.helper.MyDbHelper;

import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {
    private final Context mContext;
    private final List<MemoBean> mMemoList;
    private final OnMemoClickListener mListener;

    public interface OnMemoClickListener {
        void onMemoClick(MemoBean memo);
    }

    public MemoAdapter(Context context, List<MemoBean> memoList, OnMemoClickListener listener) {
        this.mContext = context;
        this.mMemoList = memoList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 recy_item 布局 => 这里是备忘录卡片的位置
        View view = LayoutInflater.from(mContext).inflate(R.layout.recy_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }

        MemoBean memo = mMemoList.get(adapterPosition);

        holder.item_title.setText(memo.getTitle() != null ? memo.getTitle() : "");
        holder.item_content.setText(memo.getContent() != null ? memo.getContent() : "");
        holder.item_time.setText(memo.getTime() != null ? memo.getTime() : "");

        // 图片加载
        if (!TextUtils.isEmpty(memo.getImgPath())) {
            holder.item_img.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(memo.getImgPath())
                    .placeholder(R.drawable.memo_default)
                    .error(R.drawable.memo_default)
                    .centerCrop()
                    .into(holder.item_img);
        } else {
            holder.item_img.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(R.drawable.memo_default)
                    .centerCrop()
                    .into(holder.item_img);
        }

        // 背景色
        int color = generateColorFromTitle(memo.getTitle());
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(16f);
        bg.setColor(color);
        holder.item_layout.setBackground(bg);

        // 点击事件
        holder.item_layout.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (mListener != null) {
                mListener.onMemoClick(mMemoList.get(pos));
            }
        });

        // 长按删除
        holder.item_layout.setOnLongClickListener(v -> {
            if (!(mContext instanceof Activity) || ((Activity) mContext).isFinishing()) {
                return true;
            }

            new AlertDialog.Builder(mContext)
                    .setTitle("删除备忘录")
                    .setMessage("确定要删除这条记录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        int pos = holder.getAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        MemoBean toDelete = mMemoList.get(pos);
                        SQLiteDatabase db = null;
                        try {
                            MyDbHelper helper = new MyDbHelper(mContext);
                            db = helper.getWritableDatabase();
                            db.delete("tb_memory", "_id=?", new String[]{String.valueOf(toDelete.getId())});
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (db != null && db.isOpen()) {
                                db.close();
                            }
                        }

                        mMemoList.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, mMemoList.size() - pos);
                    })
                    .setNegativeButton("取消", null)
                    .show();

            return true;
        });
    }

    private int generateColorFromTitle(String title) {
        if (title == null || title.isEmpty()) {
            return Color.parseColor("#FFF3E0");
        }
        int hash = title.hashCode();
        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = hash & 0xFF;
        r = Math.max(120, r);
        g = Math.max(120, g);
        b = Math.max(120, b);
        return Color.rgb(r, g, b);
    }

    @Override
    public int getItemCount() {
        return mMemoList == null ? 0 : mMemoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_title, item_content, item_time;
        ImageView item_img;
        CardView item_layout; // ✅ 关键修改点：从 LinearLayout 改为 CardView

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_content = itemView.findViewById(R.id.item_content);
            item_time = itemView.findViewById(R.id.item_time);
            item_img = itemView.findViewById(R.id.item_image);
            item_layout = itemView.findViewById(R.id.item_layout);
        }
    }
}
