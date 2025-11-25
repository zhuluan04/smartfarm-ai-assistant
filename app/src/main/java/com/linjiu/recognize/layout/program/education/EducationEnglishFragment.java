package com.linjiu.recognize.layout.program.education;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// 英语学习程序 (优化版)
public class EducationEnglishFragment extends Fragment {

    // 单词数据类 (增强)
    public static class Word {
        private String english;
        private String chinese;
        private String examTag;
        private int imageResId; // 图片资源ID
        private String exampleSentence; // 例句

        public Word(String english, String chinese, String examTag, int imageResId, String exampleSentence) {
            this.english = english;
            this.chinese = chinese;
            this.examTag = examTag;
            this.imageResId = imageResId;
            this.exampleSentence = exampleSentence;
        }

        // Getters
        public String getEnglish() { return english; }
        public String getChinese() { return chinese; }
        public String getExamTag() { return examTag; }
        public int getImageResId() { return imageResId; }
        public String getExampleSentence() { return exampleSentence; }
    }

    private List<Word> masterWordList; // 原始单词库
    private List<Word> studyQueue; // 当前学习队列
    private int currentWordIndex = 0;
    private Word currentWord;

    // 记录用户对每个单词的掌握情况: 0=未学, 1=认识, -1=不认识
    private Map<String, Integer> wordMastery = new HashMap<>();

    private ImageView ivWordImage;
    private TextView tvEnglishWord;
    private TextView tvChineseMeaning;
    private TextView tvExamTag;
    private TextView tvExampleSentence;
    private Button btnKnown;
    private Button btnUnknown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_education_english, container, false);

        initViews(view);
        initWordData();
        buildStudyQueue(); // 根据掌握情况构建学习队列
        showCurrentWord();

        btnKnown.setOnClickListener(v -> onWordKnown());
        btnUnknown.setOnClickListener(v -> onWordUnknown());

        return view;
    }

    private void initViews(View view) {
        ivWordImage = view.findViewById(R.id.iv_word_image);
        tvEnglishWord = view.findViewById(R.id.tv_english_word);
        tvChineseMeaning = view.findViewById(R.id.tv_chinese_meaning);
        tvExamTag = view.findViewById(R.id.tv_exam_tag);
        tvExampleSentence = view.findViewById(R.id.tv_example_sentence);
        btnKnown = view.findViewById(R.id.btn_known);
        btnUnknown = view.findViewById(R.id.btn_unknown);
    }


    private void initWordData() {
        masterWordList = new ArrayList<>();
        // 模拟后端数据 (需要在drawable中添加对应的图片，如 word_inauguration.png)
        // 这里用0占位，实际应替换为真实图片资源ID
        masterWordList.add(new Word("inauguration", "就职典礼、开始", "", R.drawable.word_inauguration, "The president's inauguration was a grand event."));
        masterWordList.add(new Word("stimulate", "刺激", "CET4", R.drawable.word_stimulate, "Coffee can stimulate your mind in the morning."));
        masterWordList.add(new Word("approach", "接近、方法", "高考", R.drawable.word_approach, "Winter is approaching quickly."));
        masterWordList.add(new Word("jurisdiction", "管辖权", "TOEFL", R.drawable.word_jurisdiction, "This court has jurisdiction over the case."));
        masterWordList.add(new Word("adamant", "坚定的", "SAT", R.drawable.word_adamant, "She was adamant that she would not change her mind."));
        masterWordList.add(new Word("vex", "烦恼", "SAT", R.drawable.word_vex, "It vexes me that he never listens."));
        masterWordList.add(new Word("sore", "疼痛的", "CET4", R.drawable.word_sore, "My muscles are sore after the workout."));
        masterWordList.add(new Word("pulpit", "讲坛", "TEM8", R.drawable.word_pulpit, "The priest stood at the pulpit to give his sermon."));
        masterWordList.add(new Word("altar", "祭坛", "TEM4", R.drawable.word_altar, "They exchanged vows at the altar."));
        masterWordList.add(new Word("taxidermy", "动物标本", "", R.drawable.word_taxidermy, "The museum displayed taxidermy of various birds."));
        masterWordList.add(new Word("vigorous", "有力的", "CET4", R.drawable.word_vigorous, "He gave a vigorous speech to motivate the team."));
        masterWordList.add(new Word("condolence", "哀悼", "TEM4", R.drawable.word_condolence, "Please accept my heartfelt condolences."));
        masterWordList.add(new Word("snub", "冷落", "TEM8", R.drawable.word_snub, "Being snubbed at the party made her feel unwelcome."));

        // 初始化掌握情况为未学
        for (Word word : masterWordList) {
            wordMastery.put(word.getEnglish(), 0);
        }
    }

    // 根据掌握情况构建学习队列
    private void buildStudyQueue() {
        studyQueue = new ArrayList<>();
        Random random = new Random();

        for (Word word : masterWordList) {
            int mastery = wordMastery.getOrDefault(word.getEnglish(), 0);
            // 如果不认识(-1)，有更高概率加入队列 (例如 100%)
            // 如果未学(0)，有中等概率加入队列 (例如 70%)
            // 如果认识(1)，有较低概率加入队列 (例如 30%)
            double probability = 0.7; // 默认概率
            if (mastery == -1) {
                probability = 1.0;
            } else if (mastery == 1) {
                probability = 0.3;
            }

            if (random.nextDouble() < probability) {
                studyQueue.add(word);
            }
        }

        // 如果队列为空（理论上不太可能，但以防万一），则加载全部
        if (studyQueue.isEmpty()) {
            studyQueue.addAll(masterWordList);
        }

        // 打乱当前学习队列
        Collections.shuffle(studyQueue);
        currentWordIndex = 0;
    }


    private void showCurrentWord() {
        if (studyQueue.isEmpty()) {
            Toast.makeText(getContext(), "学习队列为空", Toast.LENGTH_SHORT).show();
            return;
        }

        currentWord = studyQueue.get(currentWordIndex);
        ivWordImage.setImageResource(currentWord.getImageResId());
        tvEnglishWord.setText(currentWord.getEnglish());
        hideDetails(); // 隐藏中文和例句
        updateExamTag();
    }

    private void hideDetails() {
        tvChineseMeaning.setVisibility(View.GONE);
        tvExampleSentence.setVisibility(View.GONE);
    }

    private void updateExamTag() {
        String tagText = currentWord.getExamTag();
        if (tagText != null && !tagText.isEmpty()) {
            tvExamTag.setText(tagText);
            tvExamTag.setVisibility(View.VISIBLE);
        } else {
            tvExamTag.setVisibility(View.GONE);
        }
    }

    // 显示中文意思和例句
    private void revealDetails() {
        if (currentWord != null) {
            tvChineseMeaning.setText(currentWord.getChinese());
            tvChineseMeaning.setVisibility(View.VISIBLE);
            tvExampleSentence.setText(currentWord.getExampleSentence());
            tvExampleSentence.setVisibility(View.VISIBLE);
        }
    }

    private void onWordKnown() {
        if (currentWord != null) {
            wordMastery.put(currentWord.getEnglish(), 1); // 标记为认识
            // Toast.makeText(getContext(), "标记为认识: " + currentWord.getEnglish(), Toast.LENGTH_SHORT).show();
        }
        moveToNextWord();
    }

    private void onWordUnknown() {
        if (currentWord != null) {
            wordMastery.put(currentWord.getEnglish(), -1); // 标记为不认识
            // Toast.makeText(getContext(), "标记为不认识: " + currentWord.getEnglish(), Toast.LENGTH_SHORT).show();
            revealDetails(); // 点击不认识也揭示详情
        }
        moveToNextWord();
    }

    private void moveToNextWord() {
        if (studyQueue.isEmpty()) return;

        currentWordIndex++;
        // 如果一轮学完，重新构建队列
        if (currentWordIndex >= studyQueue.size()) {
            buildStudyQueue(); // 重新根据掌握情况构建队列
        }
        // 再次检查队列是否为空（构建后）
        if (!studyQueue.isEmpty()) {
            // 确保索引在有效范围内
            currentWordIndex = currentWordIndex % studyQueue.size();
            showCurrentWord();
        } else {
            Toast.makeText(getContext(), "没有更多单词了", Toast.LENGTH_SHORT).show();
            // 可以在这里处理队列为空的情况，比如显示完成信息
        }
    }
}