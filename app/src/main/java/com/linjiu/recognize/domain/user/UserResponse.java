package com.linjiu.recognize.domain.user;

// 用户信息返回
public class UserResponse {
    public int code;

    public String msg;

    public Data data;

    public static class Data {
        public int id;

        public String username;

        public String email;
        public String token;
    }
}
