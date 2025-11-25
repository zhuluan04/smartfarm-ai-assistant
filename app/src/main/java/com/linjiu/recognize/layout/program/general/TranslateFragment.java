package com.linjiu.recognize.layout.program.general;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;

public class TranslateFragment extends Fragment {

    private Toolbar toolbar;
    private EditText sourceEditText;
    private TextView targetTextView;
    private Spinner sourceLanguageSpinner;
    private Spinner targetLanguageSpinner;

    private MaterialButton swapButton;
    private MaterialButton translateButton;
    private MaterialButton voiceInputButton;
    private MaterialButton voiceOutputButton;
    private MaterialButton clearButton;
    private MaterialButton copyButton;
    private MaterialButton historyButton;

    private List<String> languageCodes;
    private List<String> languageNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_translate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initLanguages();
        setupSpinners();
        setupClickListeners();
    }

    @SuppressLint("WrongViewCast")
    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);

        sourceEditText = view.findViewById(R.id.sourceEditText);
        targetTextView = view.findViewById(R.id.targetTextView);
        sourceLanguageSpinner = view.findViewById(R.id.sourceLanguageSpinner);
        targetLanguageSpinner = view.findViewById(R.id.targetLanguageSpinner);

        swapButton = view.findViewById(R.id.swapButton);
        translateButton = view.findViewById(R.id.translateButton);
        voiceInputButton = view.findViewById(R.id.voiceInputButton);
        voiceOutputButton = view.findViewById(R.id.voiceOutputButton);
        clearButton = view.findViewById(R.id.clearButton);
        copyButton = view.findViewById(R.id.copyButton);
        historyButton = view.findViewById(R.id.historyButton);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        copyButton.setOnClickListener(v -> {
            String text = targetTextView.getText().toString();
            if (!text.isEmpty()) {
                android.content.ClipboardManager cm =
                        (android.content.ClipboardManager) requireActivity().getSystemService(requireContext().CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText(null, text));
                Toast.makeText(getContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        historyButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "历史记录功能待开发", Toast.LENGTH_SHORT).show();
        });
    }

    private void initLanguages() {
        languageCodes = new ArrayList<>();
        languageNames = new ArrayList<>();

        languageCodes.add("auto"); languageNames.add("自动检测");
        languageCodes.add("zh"); languageNames.add("中文");
        languageCodes.add("en"); languageNames.add("英语");
        languageCodes.add("ja"); languageNames.add("日语");
        languageCodes.add("ko"); languageNames.add("韩语");
        languageCodes.add("fr"); languageNames.add("法语");
        languageCodes.add("es"); languageNames.add("西班牙语");
        languageCodes.add("ru"); languageNames.add("俄语");
        languageCodes.add("de"); languageNames.add("德语");
        languageCodes.add("it"); languageNames.add("意大利语");
        languageCodes.add("pt"); languageNames.add("葡萄牙语");
        languageCodes.add("th"); languageNames.add("泰语");
        languageCodes.add("ar"); languageNames.add("阿拉伯语");
        languageCodes.add("hi"); languageNames.add("印地语");
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sourceLanguageSpinner.setAdapter(adapter);
        targetLanguageSpinner.setAdapter(adapter);

        sourceLanguageSpinner.setSelection(1);
        targetLanguageSpinner.setSelection(2);
    }

    private void setupClickListeners() {
        translateButton.setOnClickListener(v -> performTranslation());
        swapButton.setOnClickListener(v -> swapLanguages());

        voiceInputButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "语音输入功能待实现", Toast.LENGTH_SHORT).show());

        voiceOutputButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "语音朗读功能待实现", Toast.LENGTH_SHORT).show());

        clearButton.setOnClickListener(v -> {
            sourceEditText.setText("");
            targetTextView.setText("");
        });

        sourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void performTranslation() {
        String sourceText = sourceEditText.getText().toString().trim();
        if (sourceText.isEmpty()) {
            Toast.makeText(getContext(), "请输入要翻译的文本", Toast.LENGTH_SHORT).show();
            return;
        }

        String sourceLang = languageCodes.get(sourceLanguageSpinner.getSelectedItemPosition());
        String targetLang = languageCodes.get(targetLanguageSpinner.getSelectedItemPosition());

        String translatedText = mockTranslation(sourceText, sourceLang, targetLang);
        targetTextView.setText(translatedText);
    }

    private String mockTranslation(String text, String sourceLang, String targetLang) {
        if ("zh".equals(sourceLang) && "en".equals(targetLang)) {
            return switch (text) {
                case "你好" -> "Hello";
                case "世界" -> "World";
                case "翻译" -> "Translate";
                default -> text + " (模拟翻译)";
            };
        } else if ("en".equals(sourceLang) && "zh".equals(targetLang)) {
            return switch (text) {
                case "Hello" -> "你好";
                case "World" -> "世界";
                case "Translate" -> "翻译";
                default -> text + " (Simulated Translation)";
            };
        }
        return text + " (Mock Translation)";
    }

    private void swapLanguages() {
        int sourcePos = sourceLanguageSpinner.getSelectedItemPosition();
        int targetPos = targetLanguageSpinner.getSelectedItemPosition();

        sourceLanguageSpinner.setSelection(targetPos);
        targetLanguageSpinner.setSelection(sourcePos);

        String sourceText = sourceEditText.getText().toString();
        String targetText = targetTextView.getText().toString();

        sourceEditText.setText(targetText);
        targetTextView.setText(sourceText);
    }
}
