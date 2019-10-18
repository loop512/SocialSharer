package com.example.socialsharer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialsharer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    static String TAG = "infoWindowAdaper";

    private Context myContext;
    private String message = "Click to send friend request to ";

    CustomInfoWindowAdapter(Context context){
        myContext = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = ((Activity) myContext).getLayoutInflater().inflate(
                R.layout.info_window, null);

        TextView user_name = view.findViewById(R.id.user_name);
        TextView user_introduction = view.findViewById(R.id.user_introduction);
        TextView user_occupation = view.findViewById(R.id.user_occupation);
        ImageView user_image = view.findViewById(R.id.user_image);

        user_name.setText(marker.getTitle());
        String[] informations = marker.getSnippet().split("&");
        String path;

        Log.i(TAG, marker.getSnippet());

        for (String information: informations){
            String[] command_context = information.split("\\|");
            if(command_context[0].equals("introduction")){
                if(!command_context[1].equals("*null*")) {
                    user_introduction.setText(command_context[1]);
                } else {
                    user_introduction.setVisibility(TextView.INVISIBLE);
                }
            } else if (command_context[0].equals("path")){
                path = command_context[1];
                Log.i(TAG,"Path : " + path);
                File file = new File(path);
                Log.i(TAG,"file exist : " + file.exists());
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            bitmap, 100, 90, false);
                user_image.setImageBitmap(scaledBitmap);
            }
        }

        user_occupation.setText(message + marker.getTitle());
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
