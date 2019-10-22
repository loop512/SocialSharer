package com.example.socialsharer.Fragments;

/**
 *  This class change a bit map object created from an image
 *  into a image circled with a blue circle.
 *  Modified by CHENG, ZHANG 16/10/2019
 *  Original author Daniel Nugent on stackoverflow
 *
 *  This class only contains paint image,
 *  the plot image function is different from original author's solution
 *  Our plot image function is in MapShare fragment
 *  which support dynamic plot images instead of just once.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

public class CircleImage implements com.squareup.picasso.Transformation {

    /**
     * This method takes a bit map object, and do following adjustments:
     * 1. resize the image
     * 2. paint a blue circle around the image
     * 3. paint a blue triangle at the button of the image
     * @param source a bit map object converted from the user's image
     * @return a round image circled by blue outer edge
     */
    @Override
    public Bitmap transform(final Bitmap source) {
        int imageSize = Math.min(source.getWidth(), source.getHeight());
        float r = imageSize/2f;
        int triangleMargin = 7;
        int margin = 8;
        int photoMargin = 15;

        // Change the size of the marker
        Bitmap paintedImage = Bitmap.createBitmap(imageSize + triangleMargin,
                imageSize + triangleMargin, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(paintedImage);

        // paint a circle outside the image with color #00FFE4 light blue
        Paint circlePainter = new Paint();
        circlePainter.setAntiAlias(true);
        circlePainter.setColor(Color.parseColor("#00FFE4"));
        circlePainter.setStrokeWidth(margin);
        canvas.drawCircle(r, r, r- margin, circlePainter);

        // paint a triangle below the image with color #00FFE4 light blue
        Paint trianglePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePainter.setStrokeWidth(2);
        trianglePainter.setColor(Color.parseColor("#00FFE4"));
        trianglePainter.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePainter.setAntiAlias(true);
        Path triangle = new Path();
        // set the location of the triangle
        triangle.setFillType(Path.FillType.EVEN_ODD);
        triangle.moveTo(imageSize- margin, imageSize / 2);
        triangle.lineTo(imageSize/2, imageSize+ triangleMargin);
        triangle.lineTo(margin, imageSize/2);
        triangle.close();
        canvas.drawPath(triangle, trianglePainter);

        // paint shade on the image
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(r, r, r- photoMargin, paint);

        if (source != paintedImage) {
            source.recycle();
        }
        return paintedImage;
    }

    @Override
    public String key() {
        return "circleimage";
    }
}

