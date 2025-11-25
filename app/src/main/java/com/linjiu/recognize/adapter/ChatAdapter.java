package com.linjiu.recognize.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.ai.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 0;

    private final List<ChatMessage> chatList;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onLongClick(int position, ChatMessage message);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        Context context = holder.itemView.getContext();

        if (holder instanceof UserViewHolder) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.textViewMessage.setText(message.getMessage());
            // 无障碍：朗读“您说：xxx”
            String userDesc = context.getString(R.string.user_message, message.getMessage());
            holder.itemView.setContentDescription(userDesc);

        } else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;
            aiHolder.textViewMessage.setText(message.getMessage());
            // 无障碍：朗读“助手回复：xxx”
            String aiDesc = context.getString(R.string.ai_message, message.getMessage());
            holder.itemView.setContentDescription(aiDesc);

            // 长按 AI 回复 → 触发保存
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null && !message.isUser()) {
                    longClickListener.onLongClick(position, message);
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // 用户消息 ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
        }
    }

    // AI 消息 ViewHolder
    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
        }
    }
}