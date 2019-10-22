package com.example.socialsharer.data;

import java.io.Serializable;

/**
 * This is a basic class to store all information for each user
 * Also provides functions to retrieve these values.
 */
public class User implements Serializable {

    // nickName is the display name,
    // latitude and longitude are used to store user's last known location,
    // imagePath is used to store the user's image if exist.
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

    // Used for set user's image path
    public void setImagePath(String path){
        this.imagePath = path;
    }

    // Set user's calculated value, used for ranking algorithm
    public void setRankValue(int value){ rankValue = value; }

    // Rest are necessary functions to retrieve user information.
    public int getRankValue(){ return rankValue ;}

    public String getEmail() { return email; }

    public String getNickName() { return nickName; }

    public String getIntroduction() { return introduction; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getOccupation() { return occupation; }

    public String getImagePath() { return imagePath; }

    public String getContactNumber(){ return contactNumber; }

    public String getFacebook(){ return facebook; }

    public String getTwitter() { return twitter; }

    public String getInstagram() { return instagram; }

    public String getWechat() { return wechat; }

    public String getLinkedin(){ return linkedin; }
}