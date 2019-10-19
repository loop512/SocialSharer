package com.example.socialsharer.data;

import android.graphics.Bitmap;

public class Contact {
    private String name;
    private int imageId = 0;
    private Bitmap bitmap;

    public Contact(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
        this.bitmap = null;
    }

    public Contact(String name, Bitmap bitmap) {
        this.name = name;
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public int getImageID() {
        return imageId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
