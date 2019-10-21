package com.example.socialsharer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialsharer.data.User;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactProfileActivity extends AppCompatActivity {

    private static String TAG = "ContactProfileActivity";
    private static String NOTPROVIDED = "Not provided";
    private static String REQUIREACCEPT = "Accept the request to view this information";
    private static String NOTACCEPTED = "Can not access without permission";

    private String userEmail;
    private User user;
    private String state;
    private TextView profileName, profileJob, profileNumber, profileEmail;
    private TextView socialFacebook, socialTwitter, socialInstagram, socialWechat, socialLinkedin;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);
        final Button backButton = findViewById(R.id.backButton_contact_activity);
        final Button addButton = findViewById(R.id.add_contact_button);
        final Button deleteButton = findViewById(R.id.delete_contact_button);

        Intent previous = getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null){
            // Required information, please make sure pass this information
            userEmail = (String) bundle.get("userEmail");
            user = (User) bundle.getSerializable("contact");
            state = bundle.getString("state");
        }

        profileEmail = findViewById(R.id.contact_profile_email);
        profileJob =  findViewById(R.id.contact_profile_profession);
        profileName = findViewById(R.id.contact_profile_name);
        profileNumber = findViewById(R.id.contact_profile_number);
        profileImage = findViewById(R.id.contact_profile_image);

        socialFacebook = findViewById(R.id.contact_profile_facebook_link);
        socialTwitter = findViewById(R.id.contact_profile_twitter_link);
        socialInstagram = findViewById(R.id.contact_profile_instagram_link);
        socialWechat = findViewById(R.id.contact_profile_wechat_link);
        socialLinkedin = findViewById(R.id.contact_profile_linkedin_link);

        if(state.equals("contact_fragment")){
            addButton.setVisibility(View.INVISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setUserInfo(user, 1);
                    processRequest(deleteButton, "Deleted", "delete", v);
                }
            });
        } else if (state.equals("sent_request_fragment")) {
            addButton.setVisibility(View.INVISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processRequest(deleteButton, "Deleted", "remove", v);
                }
            });
        } else if (state.equals("request_fragment")) {
            // Set delete button text with reject
            deleteButton.setText(R.string.reject);

            // Accept the current request
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processRequest(addButton, "Accepted", "accept", v);
                }
            });

            // if message accepted, display information for the user.
            addButton.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().equals("Added")){
                        setUserInfo(user, 1);
                        Log.i(TAG, "Request accepted");
                    }
                }
            });
            // Reject the current request
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processRequest(deleteButton, "Rejected", "reject", v);
                }
            });
            // if message rejected, disable accept button
            deleteButton.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().equals("Rejected")){
                        addButton.setVisibility(View.INVISIBLE);
                        addButton.setClickable(false);
                    }
                }
            });
        }

        // Set go back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserInfo(user, 0);
    }

    private void processRequest(Button btn, String btnChange,
                                String state, View v){
        // Create success and fail feedback for accept/reject request
        String successFeedback = "Successfully " + state + " user " + profileName.getText()
                + "'s request.";
        if(state.equals("remove")){
            successFeedback = "Successfully delete your request sent to user "
                    + profileName.getText() + ".";
        }
        String failFeedback = "Fail to " + state + " user " + profileName
                + "'s request, check your internet connection or permission.";
        int my;
        int request;

        if(state.equals("accept")){
            my = 3;
            request = 3;
        } else if(state.equals("delete")) {
            my = 5;
            request = 2;
        } else {
            my = 6;
            request = 6;
        }

        // Create update map for fire base
        Map<String, Object> myAccept = new HashMap<>();
        myAccept.put(user.getEmail(), my);
        Map<String, Object> informRequest = new HashMap<>();
        informRequest.put(userEmail, request);

        // Update my request list
        CommonFunctions.setRequestState(v.getContext(), userEmail,
                myAccept, successFeedback, failFeedback, false, TAG,
                null, null);

        // Update request user's request list
        CommonFunctions.setRequestState(v.getContext(), user.getEmail(),
                informRequest, successFeedback, failFeedback, true, TAG,
                btn, btnChange);
    }

    private void setUserInfo(User user, int state){
        // Set user image
        if (state == 0){
            if (user.getImagePath() != null){
                Bitmap bitmap = BitmapFactory.decodeFile(user.getImagePath());
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                        120,120, false);
                profileImage.setImageBitmap(scaledBitmap);
            } else {
                profileImage.setImageResource(R.drawable.unknown);
            }
            setTextView(profileName, user.getNickName(), user.getEmail());
            setTextView(profileJob, user.getIntroduction(), "");
        } else {
            if(this.state.equals("request_fragment")){
                this.state = "contact_fragment";
            } else {
                this.state = "request_fragment";
            }
        }

        // Set user occupation, mail,
        setTextView(profileEmail, user.getEmail(), NOTPROVIDED);
        setTextView(profileNumber, user.getContactNumber(), NOTPROVIDED);
        setTextView(socialFacebook, user.getFacebook(), NOTPROVIDED);
        setTextView(socialInstagram, user.getInstagram(), NOTPROVIDED);
        setTextView(socialLinkedin, user.getLinkedin(), NOTPROVIDED);
        setTextView(socialTwitter, user.getTwitter(), NOTPROVIDED);
        setTextView(socialWechat, user.getWechat(), NOTPROVIDED);
    }

    private void setTextView(final TextView textView, String context, String defaultValue){
        if (state.equals("request_fragment")){
            if (textView != profileName && textView != profileJob && context != null) {
                context = REQUIREACCEPT;
            }
        } else if (state.equals("sent_request_fragment")){
            if (textView != profileName && textView != profileJob && context != null) {
                context = NOTACCEPTED;
            }
        }
        if (context != null){
            textView.setText(context);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = textView.getText().toString();
                    CommonFunctions.setClipboard(v.getContext(), text);
                }
            });
        } else {
            textView.setText(defaultValue);
        }
    }
}
