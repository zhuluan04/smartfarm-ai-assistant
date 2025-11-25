package com.linjiu.recognize.domain.ai;

public class AIAnalysisData {
    public String growthStatus = "分析中";   // 植物生长状态描述
    public int growthScore;               // 成长评分（0-100）
    public boolean pestDetected;          // 是否检测到害虫
    public String pestStatus = "检测中";    // 害虫检测状态
    public String lastScanTime;           // 最近一次扫描时间
}
