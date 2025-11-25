package com.linjiu.recognize.layout.program.classswork.memo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.linjiu.recognize.R;
import com.linjiu.recognize.adapter.MemoAdapter;
import com.linjiu.recognize.domain.memo.MemoBean;
import com.linjiu.recognize.helper.MyDbHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoClassworkFragment extends Fragment {

    private ExtendedFloatingActionButton btn_add;
    private FloatingActionButton btn_sort;
    private RecyclerView recy_view;
    private MyDbHelper mhelper;
    private SQLiteDatabase db;
    private TextView memo_count;
    private EditText edit_search;
    private ImageView btn_voice_search;
    private ImageView btn_view_mode;
    private LinearLayout empty_view;

    // AI功能按钮
    private LinearLayout btn_ai_classify;
    private LinearLayout btn_ai_voice;
    private LinearLayout btn_ai_ocr;
    private LinearLayout btn_ai_reminder;

    // 筛选chips
    private ChipGroup chip_group_filter;
    private Chip chip_all, chip_important, chip_today, chip_image, chip_ai;

    // 状态变量
    private boolean isGridView = true;
    private String currentFilter = "all";
    private String searchQuery = "";
    private List<MemoBean> allMemoList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.memo_classwork_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initDatabase();
        setupListeners();
        loadAndDisplayMemo();

        // 执行动画
        recy_view.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fade_in));
    }

    private void initViews(View view) {
        btn_add = view.findViewById(R.id.button_add);
        btn_sort = view.findViewById(R.id.button_sort);
        recy_view = view.findViewById(R.id.recy_view);
        memo_count = view.findViewById(R.id.memo_count);
        edit_search = view.findViewById(R.id.edit_search);
        btn_voice_search = view.findViewById(R.id.btn_voice_search);
        btn_view_mode = view.findViewById(R.id.btn_view_mode);
        empty_view = view.findViewById(R.id.empty_view);

        // AI功能按钮
        btn_ai_classify = view.findViewById(R.id.btn_ai_classify);
        btn_ai_voice = view.findViewById(R.id.btn_ai_voice);
        btn_ai_ocr = view.findViewById(R.id.btn_ai_ocr);
        btn_ai_reminder = view.findViewById(R.id.btn_ai_reminder);

        // 筛选chips
        chip_group_filter = view.findViewById(R.id.chip_group_filter);
        chip_all = view.findViewById(R.id.chip_all);
        chip_important = view.findViewById(R.id.chip_important);
        chip_today = view.findViewById(R.id.chip_today);
        chip_image = view.findViewById(R.id.chip_image);
        chip_ai = view.findViewById(R.id.chip_ai);
    }

    private void initDatabase() {
        mhelper = new MyDbHelper(requireContext());
        db = mhelper.getWritableDatabase();
    }

    private void setupListeners() {
        // 新建备忘按钮
        btn_add.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MemoClassworkAddInfo())
                    .addToBackStack(null)
                    .commit();
        });

        // 排序按钮
        btn_sort.setOnClickListener(v -> showSortMenu());

        // 视图模式切换
        btn_view_mode.setOnClickListener(v -> toggleViewMode());

        // 搜索功能
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterAndDisplayMemos();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 语音搜索
        btn_voice_search.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "语音搜索功能开发中...", Toast.LENGTH_SHORT).show();
            // TODO: 实现语音识别功能
        });

        // 筛选chips
        chip_group_filter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
            } else if (checkedId == R.id.chip_important) {
                currentFilter = "important";
            } else if (checkedId == R.id.chip_today) {
                currentFilter = "today";
            } else if (checkedId == R.id.chip_image) {
                currentFilter = "image";
            } else if (checkedId == R.id.chip_ai) {
                currentFilter = "ai";
            }
            filterAndDisplayMemos();
        });

        // AI功能按钮
        btn_ai_classify.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "AI智能分类：正在分析您的备忘录...", Toast.LENGTH_SHORT).show();
            // TODO: 实现AI智能分类功能
            performAIClassification();
        });

        btn_ai_voice.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "语音记录：请开始说话...", Toast.LENGTH_SHORT).show();
            // TODO: 实现语音转文字功能
        });

        btn_ai_ocr.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "图片识别：请选择要识别的图片...", Toast.LENGTH_SHORT).show();
            // TODO: 实现OCR图片识别功能
        });

        btn_ai_reminder.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "智能提醒：AI将为您推荐提醒时间...", Toast.LENGTH_SHORT).show();
            // TODO: 实现AI智能提醒功能
        });
    }

    private void loadAndDisplayMemo() {
        allMemoList.clear();
        Cursor cursor = db.rawQuery("SELECT * FROM tb_memory ORDER BY mtime DESC", null);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            String imgPath = cursor.getString(cursor.getColumnIndexOrThrow("imgpath"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("mtime"));

            allMemoList.add(new MemoBean(id, title, content, imgPath, time));
        }
        cursor.close();

        filterAndDisplayMemos();
    }

    private void filterAndDisplayMemos() {
        List<MemoBean> filteredList = new ArrayList<>();

        for (MemoBean memo : allMemoList) {
            // 搜索过滤
            if (!searchQuery.isEmpty()) {
                if (!memo.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) &&
                        !memo.getContent().toLowerCase().contains(searchQuery.toLowerCase())) {
                    continue;
                }
            }

            // 分类过滤
            switch (currentFilter) {
                case "all":
                    filteredList.add(memo);
                    break;
                case "important":
                    // TODO: 添加重要标记字段判断
                    filteredList.add(memo);
                    break;
                case "today":
                    if (isToday(memo.getTime())) {
                        filteredList.add(memo);
                    }
                    break;
                case "image":
                    if (memo.getImgPath() != null && !memo.getImgPath().isEmpty()) {
                        filteredList.add(memo);
                    }
                    break;
                case "ai":
                    // TODO: 添加AI生成标记字段判断
                    filteredList.add(memo);
                    break;
            }
        }

        updateMemoDisplay(filteredList);
    }

    private void updateMemoDisplay(List<MemoBean> memoList) {
        memo_count.setText(String.valueOf(memoList.size()));

        if (memoList.isEmpty()) {
            recy_view.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
            return;
        } else {
            recy_view.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }

        MemoAdapter adapter = new MemoAdapter(requireContext(), memoList, memo -> {
            Bundle args = new Bundle();
            args.putLong("memo_id", memo.getId());
            args.putString("title", memo.getTitle());
            args.putString("content", memo.getContent());
            args.putString("imgPath", memo.getImgPath());
            args.putString("time", memo.getTime());

            MemoClassworkDetail detailFragment = new MemoClassworkDetail();
            detailFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        RecyclerView.LayoutManager layoutManager;
        if (isGridView) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            ((StaggeredGridLayoutManager) layoutManager).setGapStrategy(
                    StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        } else {
            layoutManager = new LinearLayoutManager(requireContext());
        }

        recy_view.setLayoutManager(layoutManager);
        recy_view.setAdapter(adapter);
    }

    private void toggleViewMode() {
        isGridView = !isGridView;
        if (isGridView) {
            btn_view_mode.setImageResource(R.drawable.ic_view_module);
            Toast.makeText(requireContext(), "瀑布流视图", Toast.LENGTH_SHORT).show();
        } else {
            btn_view_mode.setImageResource(R.drawable.ic_view_list);
            Toast.makeText(requireContext(), "列表视图", Toast.LENGTH_SHORT).show();
        }
        filterAndDisplayMemos();
    }

    private void showSortMenu() {
        String[] sortOptions = {"按时间排序", "按标题排序", "按重要性排序"};

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("选择排序方式")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByTime();
                            break;
                        case 1:
                            sortByTitle();
                            break;
                        case 2:
                            sortByImportance();
                            break;
                    }
                    Toast.makeText(requireContext(), "已切换到：" + sortOptions[which],
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void sortByTime() {
        allMemoList.sort((m1, m2) -> m2.getTime().compareTo(m1.getTime()));
        filterAndDisplayMemos();
    }

    private void sortByTitle() {
        allMemoList.sort((m1, m2) -> m1.getTitle().compareTo(m2.getTitle()));
        filterAndDisplayMemos();
    }

    private void sortByImportance() {
        // TODO: 实现按重要性排序
        Toast.makeText(requireContext(), "重要性排序功能开发中...", Toast.LENGTH_SHORT).show();
    }

    private void performAIClassification() {
        // 模拟AI分类过程
        new android.os.Handler().postDelayed(() -> {
            if (requireContext() != null) {
                String[] categories = {"工作", "生活", "学习", "旅行", "购物"};
                String randomCategory = categories[(int) (Math.random() * categories.length)];

                Toast.makeText(requireContext(),
                        "AI分析完成！发现 " + allMemoList.size() + " 条备忘录，" +
                                "主要分类：" + randomCategory,
                        Toast.LENGTH_LONG).show();
            }
        }, 2000);
    }

    private boolean isToday(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = sdf.format(new Date());
            return timeStr.startsWith(today);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新数据
        loadAndDisplayMemo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}