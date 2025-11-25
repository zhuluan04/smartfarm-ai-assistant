package com.linjiu.recognize.layout.program.classswork.memo;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.linjiu.recognize.R;
import java.util.*;

public class MemoClassworkDetail extends Fragment {

    private TextView tvTitle, tvTime, tvContent, tvAiSuggestion, tvAiTitle;
    private ImageView ivImage;
    private LinearLayout aiCard;
    private Button btnSummary, btnTTS;
    private TextToSpeech tts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.memo_classwork_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tv_title);
        tvTime = view.findViewById(R.id.tv_time);
        tvContent = view.findViewById(R.id.tv_content);
        tvAiSuggestion = view.findViewById(R.id.tv_ai_suggestion);
        tvAiTitle = view.findViewById(R.id.tv_ai_title);
        ivImage = view.findViewById(R.id.iv_image);
        aiCard = view.findViewById(R.id.ai_card);
        btnSummary = view.findViewById(R.id.btn_ai_summary);
        btnTTS = view.findViewById(R.id.btn_tts);

        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.CHINESE);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title", "");
            String content = args.getString("content", "");
            String imgPath = args.getString("imgPath", "");
            String time = args.getString("time", "");

            tvTitle.setText(title);
            tvTime.setText(time);
            tvContent.setText(content);

            if (!imgPath.isEmpty()) {
                Glide.with(this)
                        .load(imgPath)
                        .placeholder(R.drawable.sunshine)
                        .error(R.drawable.ic_image_broken)
                        .into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
            }

            btnSummary.setOnClickListener(v -> showAISuggestion(title, content));
            btnTTS.setOnClickListener(v -> {
                if (tts != null) {
                    tts.speak(title + "。" + content, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });
        }
    }

    private void showAISuggestion(String title, String content) {
        aiCard.setVisibility(View.VISIBLE);
        tvAiSuggestion.setText("🤖 正在分析内容…");

        // 模拟 AI 逻辑（后期可替换为在线模型）
        new Thread(() -> {
            try {
                Thread.sleep(800); // 模拟延迟
            } catch (InterruptedException ignored) {}

            String suggestion = generateAISuggestion(content);

            requireActivity().runOnUiThread(() -> {
                tvAiSuggestion.setText(suggestion);
                tvAiTitle.setText("🤖 智能分析结果");
            });
        }).start();
    }

    // 简易智能分析逻辑
    private String generateAISuggestion(String content) {
        if (content.contains("作业") || content.contains("任务"))
            return "检测到学习任务 📚，建议添加到待办清单，并设置提醒。";
        if (content.contains("会议") || content.contains("讨论"))
            return "检测到会议安排 📅，建议创建日历事件，提前10分钟提醒。";
        if (content.contains("购物") || content.contains("买"))
            return "检测到购物计划 🛒，可添加到购物清单。";
        if (content.contains("生日") || content.contains("纪念日"))
            return "检测到特殊日期 🎉，建议设置自动提醒。";
        if (content.length() > 80)
            return "内容较长 ✍️，建议你将重点摘要整理为清单以便复习。";
        return "暂无特别建议，已为你保存此条备忘录 ✅";
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
