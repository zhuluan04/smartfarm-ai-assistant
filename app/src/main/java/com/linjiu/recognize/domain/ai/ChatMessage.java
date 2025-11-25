package com.linjiu.recognize.domain.ai;

// AI回答类
public class ChatMessage {
    private String message;
    private boolean isUser; // true=用户发送，false=AI发送

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}