package com.example.socialsharer.data;

public class User {

    private String email;
    private String nickName;
    private String introduction;
    private Double latitude;
    private Double longitude;

    public User() {}

    public User(String email, String nickName, String introduction, Double latitude, Double longitude) {
        this.email = email;
        this.nickName = nickName;
        this.introduction = introduction;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public String getNickName() {
        return nickName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}