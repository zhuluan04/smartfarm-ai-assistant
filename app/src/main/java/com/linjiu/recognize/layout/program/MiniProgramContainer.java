package com.linjiu.recognize.layout.program;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.R;
import com.linjiu.recognize.adapter.MiniProgramAdapter;
import com.linjiu.recognize.domain.program.MiniProgram;
import com.linjiu.recognize.layout.bottomNav.module.work.ConstellationModuleFragment;
import com.linjiu.recognize.layout.program.classswork.memo.MemoClassworkFragment;
import com.linjiu.recognize.layout.program.classswork.menu.MenuClassworkFragment;
import com.linjiu.recognize.layout.program.education.EducationEnglishFragment;
import com.linjiu.recognize.layout.program.education.EducationQuestionFragment;
import com.linjiu.recognize.layout.program.entertainment.module.Entertainment3DModuleFragment;
import com.linjiu.recognize.layout.program.entertainment.music.EntertainmentMusicFragment;
import com.linjiu.recognize.layout.program.game.MouseGameFragment;
import com.linjiu.recognize.layout.program.game.mario.GameMarioFragment;
import com.linjiu.recognize.layout.program.general.CalculatorFragment;
import com.linjiu.recognize.layout.program.general.MapFragment;
import com.linjiu.recognize.layout.program.general.NoteFragment;
import com.linjiu.recognize.layout.program.general.TranslateFragment;
import com.linjiu.recognize.layout.program.general.WeatherFragment;
import com.linjiu.recognize.layout.program.game.Game2048Fragment;
import com.linjiu.recognize.layout.program.game.snake.GameSnakeFragment;
import com.linjiu.recognize.layout.program.game.tetris.GameTetrisFragment;
import com.linjiu.recognize.layout.program.game.tower.GameTowerDefenseFragment;
import com.linjiu.recognize.layout.program.work.catcare.CatCareWorkFragment;

import org.tensorflow.lite.schema.MaximumMinimumOptions;

import java.util.ArrayList;
import java.util.List;

// 小程序页面容器
public class MiniProgramContainer extends Fragment {

    private RecyclerView recyclerView;

    private List<MiniProgram> miniProgramList;

    private MiniProgramAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_program_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        // 初始化数据
        initDummyData();

        // 设置 Grid 布局，每行3列
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // 或者使用 NavController.popBackStack()
            }
        });

        // 设置适配器
        adapter = new MiniProgramAdapter(miniProgramList);
        recyclerView.setAdapter(adapter);

        // 设置点击监听
        adapter.setOnItemClickListener(position -> {
            MiniProgram program = miniProgramList.get(position);
            Class<? extends Fragment> targetClass = program.getTargetFragmentClass();

            if (targetClass != null && getActivity() != null) {
                try {
                    Fragment targetFragment = targetClass.newInstance();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, targetFragment)
                            .addToBackStack(null) // 可返回
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 网格数据
    private void initDummyData() {
        miniProgramList = new ArrayList<>();
        // 通用工具
        miniProgramList.add(new MiniProgram("天气", R.drawable.ic_weather, WeatherFragment.class));
        miniProgramList.add(new MiniProgram("地图", R.drawable.ic_map, MapFragment.class));
        miniProgramList.add(new MiniProgram("翻译", R.drawable.ic_translate, TranslateFragment.class));
        miniProgramList.add(new MiniProgram("计算器", R.drawable.ic_calculator, CalculatorFragment.class));
        miniProgramList.add(new MiniProgram("日历", R.drawable.ic_calendar, CalculatorFragment.class));
        miniProgramList.add(new MiniProgram("笔记", R.drawable.ic_note, NoteFragment.class));
        miniProgramList.add(new MiniProgram("星座预测", R.drawable.ic_constellation, ConstellationModuleFragment.class));
        // 游戏部分
        miniProgramList.add(new MiniProgram("2048", R.drawable.ic_2048, Game2048Fragment.class));
        miniProgramList.add(new MiniProgram("俄罗斯方块", R.drawable.ic_tetris, GameTetrisFragment.class));
        miniProgramList.add(new MiniProgram("贪吃蛇", R.drawable.ic_snake, GameSnakeFragment.class));
        miniProgramList.add(new MiniProgram("塔防大作战", R.drawable.ic_tower_defense, GameTowerDefenseFragment.class));
        miniProgramList.add(new MiniProgram("旺仔大战", R.drawable.ic_mario, GameMarioFragment.class));
        miniProgramList.add(new MiniProgram("打地鼠", R.drawable.ic_mouse, MouseGameFragment.class));
        miniProgramList.add(new MiniProgram("菜谱", R.drawable.ic_menu, MenuClassworkFragment.class));
        // 教育学习部分
        miniProgramList.add(new MiniProgram("英语", R.drawable.ic_english, EducationEnglishFragment.class));
        miniProgramList.add(new MiniProgram("拍题", R.drawable.ic_question, EducationQuestionFragment.class));
        // 娱乐
        miniProgramList.add(new MiniProgram("3D模型",R.drawable.ic_3d_module, Entertainment3DModuleFragment.class));
        miniProgramList.add(new MiniProgram("音乐", R.drawable.ic_music, EntertainmentMusicFragment.class));
        // 课程
        miniProgramList.add(new MiniProgram("备忘录", R.drawable.ic_memo, MemoClassworkFragment.class));
        // 横向项目
        miniProgramList.add(new MiniProgram("顾猫", R.drawable.ic_cat_care, CatCareWorkFragment.class));
    }
}