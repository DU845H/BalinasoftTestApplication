package com.egor_dubovik.testapplication.api.model;

//POJO класс для работы с JSON
public class User {

    private String login;

    private String password;

    private Integer status;
    private String error;
    private Data data;
    private Valid[] valid;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getError() {
        return error;
    }

    public Integer getStatus() {
        return status;
    }

    public Valid getValid(int i) {
        return valid[i];
    }

    public Data getData() {
        return data;
    }
}
