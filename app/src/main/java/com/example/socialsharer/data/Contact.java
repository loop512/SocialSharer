package com.example.socialsharer.data;

import android.widget.ImageView;

public class Contact {
    String name;
    int imageId;

    public Contact(String name, int imageId) {
        this.name=name;
        this.imageId=imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageID() {
        return imageId;
    }
}
