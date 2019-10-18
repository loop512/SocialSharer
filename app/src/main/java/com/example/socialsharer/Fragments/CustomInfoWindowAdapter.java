package com.example.socialsharer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.socialsharer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

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

        user_name.setText(marker.getTitle());
        if(marker.getSnippet() != null){
            user_introduction.setText(marker.getSnippet());
        } else {
            user_introduction.setVisibility(TextView.INVISIBLE);
        }

        user_occupation.setText(message + marker.getTitle());
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
