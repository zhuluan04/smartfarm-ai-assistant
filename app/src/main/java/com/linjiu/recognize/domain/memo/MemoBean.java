package com.linjiu.recognize.domain.memo;

public class MemoBean {
    private long id;
    private String title;
    private String content;
    private String imgPath;
    private String time;

    // 构造函数
    public MemoBean(long id, String title, String content, String imgPath, String time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imgPath = imgPath;
        this.time = time;
    }

    // Getter 方法
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getTime() {
        return time;
    }
}