package com.example.socialsharer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialsharer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    static String TAG = "infoWindowAdaper";
    private static String NULLSTRING = "null";

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
        ImageView facebook_view = view.findViewById(R.id.icon_facebook);
        ImageView linkedin_view = view.findViewById(R.id.icon_link);
        ImageView wechat_view = view.findViewById(R.id.icon_wechat);
        ImageView ins_view = view.findViewById(R.id.icon_ins);
        ImageView twitter_view = view.findViewById(R.id.icon_twitter);
        TextView user_real_occupation = view.findViewById(R.id.user_occupation_real);

        user_name.setText(marker.getTitle());
        String displayedMessage = message + marker.getTitle();
        user_occupation.setText(displayedMessage);
        String[] informations = marker.getSnippet().split(" ");

        Log.i(TAG, "all information :" + marker.getSnippet());
        Log.i(TAG, "Infolist length: " + informations.length);
        for (String info: informations){
            Log.i(TAG, "passed :" + info);
        }
        int index = 0;
        String occupation = null;
        if (informations[0].equals("1")){
            //This user has occupation.
            index = 1;
            occupation = informations[index];
            index += 1;
        }

        String facebook = informations[index];
        index += 1;
        String linkedin = informations[index];
        index += 1;
        String wechat = informations[index];
        index += 1;
        String ins = informations[index];
        index += 1;
        String twitter = informations[index];
        index += 1;
        String path = informations[index];
        index += 1;
        String information;
        if (informations.length == index){
            information = informations[index];
        } else {
            information = "";
            for (int i = index; i < informations.length; i++){
                information += informations[i] + " ";
            }
        }

        if (occupation == null){
            if (!information.equals(NULLSTRING)) {
                user_real_occupation.setText(information);
            } else {
                user_real_occupation.setVisibility(View.INVISIBLE);
                user_introduction.setVisibility(TextView.INVISIBLE);
            }
        } else {
            user_real_occupation.setText(occupation);
            if (!information.equals(NULLSTRING)) {
                user_introduction.setText(information);
            } else {
                user_introduction.setVisibility(TextView.INVISIBLE);
            }
        }

        if (!path.equals(NULLSTRING)) {
            File file = new File(path);
            Log.i(TAG, "file exist : " + file.exists());
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null){
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap, 100, 90, false);
                user_image.setImageBitmap(scaledBitmap);
            }
        }

        int id;
        if (facebook.equals(NULLSTRING)){
            id = R.drawable.facebook_grey;
            setIcon(facebook, id, facebook_view);
        }
        if (linkedin.equals(NULLSTRING)){
            id = R.drawable.linkedin_grey;
            setIcon(facebook, id, linkedin_view);
        }
        if (wechat.equals(NULLSTRING)){
            id = R.drawable.wechat_grey;
            setIcon(facebook, id, wechat_view);
        }
        if (ins.equals(NULLSTRING)){
            id = R.drawable.instagram_grey;
            setIcon(facebook, id, ins_view);
        }
        if (twitter.equals(NULLSTRING)){
            id = R.drawable.twitter_grey;
            setIcon(facebook, id, twitter_view);
        }
        return view;
    }

    private void setIcon(String check, int id, ImageView view){
        Bitmap bitmap = BitmapFactory.decodeResource(myContext.getResources(), id);
        view.setImageBitmap(bitmap);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
