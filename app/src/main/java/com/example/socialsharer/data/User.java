package com.example.socialsharer.data;

public class User {

    private String email;
    private String nickName;
    private String introduction;
    private Double latitude;
    private Double longitude;
    private String occupation;
    private String imagePath;
    private String contactNumber;

    public User(String email, String nickName, String introduction,
                Double latitude, Double longitude, String occupation,
                String imagePath, String contactNumber) {
        this.email = email;
        this.nickName = nickName;
        this.introduction = introduction;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occupation = occupation;
        this.imagePath = imagePath;
        this.contactNumber = contactNumber;
    }

    public void setImagePath(String path){
        this.imagePath = path;
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

    public String getOccupation() {
        return occupation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getContactNumber(){
        return contactNumber;
    }
}