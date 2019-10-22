package com.example.socialsharer.data;

import android.graphics.Bitmap;

/**
 * This class stores each user's image and name used for dynamic list view in
 * contacts, received request and sent request.
 * Images are stored using bitmap.
 */
public class Contact {
    private String name;
    private int imageId = 0;
    private Bitmap bitmap;

    // Not really used, but leave here in case in the future image ID is required
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
