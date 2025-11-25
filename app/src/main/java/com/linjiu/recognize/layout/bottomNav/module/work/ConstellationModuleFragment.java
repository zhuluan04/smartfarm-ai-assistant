package com.linjiu.recognize.layout.bottomNav.module.work;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// 猜猜我的星座 -> 当作是一个小程序
public class ConstellationModuleFragment extends Fragment {

    private Button buttonSubmit;

    private TextView textViewResult;

    private TextView textViewConstellationInfo;

    private EditText editTextDate;

    private DatePicker datePicker;

    private int selectedMonth = -1;

    private int selectedDay = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载布局
        View view = inflater.inflate(R.layout.fragment_module_work_constellation, container, false);

        // 绑定控件
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        textViewResult = view.findViewById(R.id.textViewResult);
        textViewConstellationInfo = view.findViewById(R.id.textViewConstellationInfo);
        editTextDate = view.findViewById(R.id.editTextDate);
        datePicker = view.findViewById(R.id.datePicker);

        // 初始化底部日期选择器
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year1, int monthOfYear, int dayOfMonth) {
                selectedMonth = monthOfYear + 1;
                selectedDay = dayOfMonth;

                String dateStr = String.format("%02d/%02d", selectedMonth, selectedDay);
                editTextDate.setText(dateStr);
            }
        });

        // 点击输入框 → 弹出日期选择器
        editTextDate.setOnClickListener(v -> showDatePicker());

        // 点击按钮时，根据输入框里的日期计算星座
        buttonSubmit.setOnClickListener(v -> {
            if (selectedMonth != -1 && selectedDay != -1) {
                String constellation = getConstellation(selectedMonth, selectedDay);
                textViewResult.setText("你的星座是：" + constellation + "（生日：" + selectedMonth + "月" + selectedDay + "日）");
                textViewConstellationInfo.setText(getConstellationInfo(constellation));
            } else {
                textViewResult.setText("请先选择生日日期！");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 这里去获取id如果为null则崩溃
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // 设置返回箭头点击事件
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // 返回上一页
            }
        });
    }

    // 弹出日期选择器对话框
    private void showDatePicker() {
        if (editTextDate == null) {
            Log.e("ConstellationFragment", "editTextDate is null!");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            selectedMonth = monthOfYear + 1;
            selectedDay = dayOfMonth;

            String dateStr = String.format("%02d/%02d", selectedMonth, selectedDay);
            editTextDate.setText(dateStr);

            // 同步到底部的DatePicker
            if (datePicker != null) {
                datePicker.updateDate(year1, monthOfYear, dayOfMonth);
            }

        }, year, month, day);

        dialog.show();
    }

    // 🌟 根据月/日判断星座
    private String getConstellation(int month, int day) {
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";

        return "未知星座";
    }

    // 星座简介
    private String getConstellationInfo(String constellation) {
        switch (constellation) {
            case "水瓶座":
                return "♒ 水瓶座（1月20日 - 2月18日）\n" +
                        "性格：独立、理性、创新、反传统。你总是走在时代前沿，讨厌被束缚。\n" +
                        "爱情：追求精神共鸣，需要空间与自由。伴侣需理解你的“忽冷忽热”。\n" +
                        "事业：适合科技、创意、公益领域。你总能提出颠覆性点子。\n" +
                        "幸运物：蓝色水晶、科技产品、风铃。\n" +
                        "悄悄话：别人说你古怪？那是他们跟不上你的脑洞！";

            case "双鱼座":
                return "♓ 双鱼座（2月19日 - 3月20日）\n" +
                        "性格：浪漫、敏感、富有同情心。你是天生的艺术家和梦想家。\n" +
                        "爱情：渴望灵魂伴侣，容易为爱牺牲。小心别被情绪牵着走哦。\n" +
                        "事业：适合艺术、心理咨询、写作。你的共情力是最大优势。\n" +
                        "幸运物：海蓝宝、薰衣草、音乐盒。\n" +
                        "悄悄话：眼泪不是软弱，是你感受世界的深度。";

            case "白羊座":
                return "♈ 白羊座（3月21日 - 4月19日）\n" +
                        "性格：热情、冲动、勇敢、直率。你的人生字典里没有“等一下”。\n" +
                        "爱情：爱得轰轰烈烈，但容易三分钟热度。学会耐心是你的课题。\n" +
                        "事业：适合创业、销售、运动竞技。冲劲就是你的超能力！\n" +
                        "幸运物：红玛瑙、运动手环、辣椒。\n" +
                        "悄悄话：别总用头撞墙，有时候绕个弯更快到终点。";

            case "金牛座":
                return "♉ 金牛座（4月20日 - 5月20日）\n" +
                        "性格：稳重、务实、固执、享受生活。你对美食和舒适有天生的鉴赏力。\n" +
                        "爱情：慢热但专一，一旦认定就是一辈子。讨厌被催促或改变计划。\n" +
                        "事业：适合金融、餐饮、地产。你擅长把资源变成财富。\n" +
                        "幸运物：绿幽灵、丝绒抱枕、巧克力。\n" +
                        "悄悄话：偶尔打破routine，世界不会塌，反而可能更精彩。";

            case "双子座":
                return "♊ 双子座（5月21日 - 6月21日）\n" +
                        "性格：聪明、善变、好奇心爆棚。你的大脑永远在加载新话题。\n" +
                        "爱情：需要新鲜感和对话，怕无聊胜过怕分手。沟通是你的氧气。\n" +
                        "事业：适合媒体、教育、公关。你一个人能顶一个团队。\n" +
                        "幸运物：书籍、蓝牙耳机、薄荷糖。\n" +
                        "悄悄话：别让‘下一个更有趣’害你错过真正重要的事。";

            case "巨蟹座":
                return "♋ 巨蟹座（6月22日 - 7月22日）\n" +
                        "性格：温柔、念旧、情绪化、保护欲强。家是你力量的源泉。\n" +
                        "爱情：付出型人格，渴望安全感。记得先爱自己，再爱别人。\n" +
                        "事业：适合育儿、餐饮、心理咨询。你天生会照顾人。\n" +
                        "幸运物：珍珠、毛绒拖鞋、家庭相册。\n" +
                        "悄悄话：你的敏感不是负担，是雷达——只是别总扫描负面信号。";

            case "狮子座":
                return "♌ 狮子座（7月23日 - 8月22日）\n" +
                        "性格：自信、慷慨、爱表现、领导欲强。聚光灯下才是你的主场。\n" +
                        "爱情：需要崇拜和赞美，也愿意为爱人倾尽所有。讨厌被忽视。\n" +
                        "事业：适合管理、演艺、品牌营销。你天生是人群中的C位。\n" +
                        "幸运物：黄金饰品、红色口红、聚光灯。\n" +
                        "悄悄话：真正的王者不是被簇拥，而是能照亮他人。";

            case "处女座":
                return "♍ 处女座（8月23日 - 9月22日）\n" +
                        "性格：细致、完美主义、逻辑控、有点龟毛。你的Excel表比谁都整齐。\n" +
                        "爱情：挑剔但忠诚，用行动表达爱。别总纠正伴侣的小毛病啦！\n" +
                        "事业：适合医疗、编辑、数据分析。细节控是你的超能力。\n" +
                        "幸运物：笔记本、消毒湿巾、无印良品。\n" +
                        "悄悄话：世界不需要完美，需要的是你放松后的笑容。";

            case "天秤座":
                return "♎ 天秤座（9月23日 - 10月23日）\n" +
                        "性格：优雅、犹豫、追求和谐、讨厌冲突。你连点奶茶都要纠结半小时。\n" +
                        "爱情：浪漫至上，需要平等与美感。别为了和平委屈自己哦。\n" +
                        "事业：适合法律、设计、外交。你天生是矛盾调解大师。\n" +
                        "幸运物：玫瑰石英、香薰蜡烛、艺术画册。\n" +
                        "悄悄话：选择困难？抛硬币吧——硬币在空中时，你就知道自己想要什么了。";

            case "天蝎座":
                return "♏ 天蝎座（10月24日 - 11月22日）\n" +
                        "性格：神秘、执着、洞察力强、爱恨极端。你的眼睛能看穿人心。\n" +
                        "爱情：全心全意，不容背叛。信任一旦破碎，很难重建。\n" +
                        "事业：适合侦探、心理、投资。你擅长在暗处发现真相。\n" +
                        "幸运物：黑曜石、深红酒、密码本。\n" +
                        "悄悄话：不是所有秘密都值得深挖，有些谜题，放过自己也放过他人。";

            case "射手座":
                return "♐ 射手座（11月23日 - 12月21日）\n" +
                        "性格：自由、乐观、爱冒险、讨厌束缚。你的灵魂属于旷野和远方。\n" +
                        "爱情：害怕承诺，但一旦认定就忠贞不渝。带TA去旅行吧！\n" +
                        "事业：适合旅游、教育、自媒体。世界是你的教室。\n" +
                        "幸运物：弓箭饰品、护照、登山包。\n" +
                        "悄悄话：真正的自由不是无拘无束，而是知道自己为何出发。";

            case "摩羯座":
                return "♑ 摩羯座（12月22日 - 1月19日）\n" +
                        "性格：沉稳、自律、野心勃勃、外冷内热。你的人生是步步为营的棋局。\n" +
                        "爱情：慢热但可靠，用行动代替甜言蜜语。记得偶尔也要浪漫一下！\n" +
                        "事业：适合管理、建筑、金融。你注定站在山顶。\n" +
                        "幸运物：黑玛瑙、计划本、登山靴。\n" +
                        "悄悄话：别总把‘以后’挂在嘴边，山顶的风景，也要记得和身边人分享。";

            default:
                return "🌟 未知星座\n" +
                        "也许你是来自银河系外的神秘访客？\n" +
                        "请确认生日日期是否正确，或联系宇宙管理员更新星图。";
        }
    }
}