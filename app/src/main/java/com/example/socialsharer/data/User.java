package com.example.socialsharer.data;

import java.io.Serializable;

public class User implements Serializable {

    private String email;
    private String nickName;
    private String introduction;
    private Double latitude;
    private Double longitude;
    private String occupation;
    private String imagePath;
    private String contactNumber;
    private String facebook;
    private String twitter;
    private String instagram;
    private String wechat;
    private String linkedin;
    private int rankValue;

    public User(String email, String nickName, String introduction,
                Double latitude, Double longitude, String occupation,
                String imagePath, String contactNumber, String facebook,
                String twitter, String instagram, String wechat, String linkedin) {
        this.email = email;
        this.nickName = nickName;
        this.introduction = introduction;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occupation = occupation;
        this.imagePath = imagePath;
        this.contactNumber = contactNumber;
        this.facebook = facebook;
        this.twitter = twitter;
        this.instagram = instagram;
        this.wechat = wechat;
        this.linkedin = linkedin;
        rankValue = 0;
    }

    public void setImagePath(String path){
        this.imagePath = path;
    }

    public void setRankValue(int value){ rankValue = value; }

    public int getRankValue(){ return rankValue ;}

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

    public String getOccupation() {
        return occupation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getContactNumber(){
        return contactNumber;
    }

    public String getFacebook(){ return facebook; }

    public String getTwitter() { return twitter; }

    public String getInstagram() { return instagram; }

    public String getWechat() { return wechat; }

    public String getLinkedin(){ return linkedin; }
}