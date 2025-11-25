package com.linjiu.recognize.domain.image;


public class ImageAnalysisRequest {
    private String base64_image; // 确保字段名为 'base64_image'
    private String question;

    public ImageAnalysisRequest(String base64Image, String question) {
        this.base64_image = base64Image;
        this.question = question;
    }

    // Getter 和 Setter 方法
    public String getBase64_image() { return base64_image; }
    public void setBase64_image(String base64_image) { this.base64_image = base64_image; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}