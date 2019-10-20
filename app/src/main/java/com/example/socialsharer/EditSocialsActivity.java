package com.example.socialsharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class EditSocialsActivity extends AppCompatActivity {

    private static final String FACEBOOK = "Facebook";
    private static final String TWITTER = "Twitter";
    private static final String INSTAGRAM = "Instagram";
    private static final String WECHAT = "Wechat";
    private static final String LINKEDIN = "Linkedin";

    private static final String TAG = "EditSocialActivity";
    private EditText facebookField;
    private EditText twitterField;
    private EditText instagramField;
    private EditText wechatField;
    private EditText linkedinFiled;
    private Button saveButton;
    private Button cancelButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference profileRef;
    private ListenerRegistration socialsListener;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_socials);
        getSupportActionBar().setTitle(R.string.title_activity_edit_socials);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();
        profileRef = db.collection("users").document(email);
        facebookField = findViewById(R.id.edit_facebook);
        twitterField = findViewById(R.id.edit_twitter);
        instagramField = findViewById(R.id.edit_instagram);
        wechatField = findViewById(R.id.edit_wechat);
        linkedinFiled = findViewById(R.id.edit_linkedin);
        saveButton = findViewById(R.id.edit_save_socials);
        cancelButton = findViewById(R.id.edit_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String facebookID = facebookField.getText().toString();
                String twitterID = twitterField.getText().toString();
                String instagramID = instagramField.getText().toString();
                String wechatID = wechatField.getText().toString();
                String linkedinID = linkedinFiled.getText().toString();

                Map<String, Object> change = new HashMap<>();
                change.put(FACEBOOK, facebookID);
                change.put(TWITTER, twitterID);
                change.put(INSTAGRAM, instagramID);
                change.put(WECHAT, wechatID);
                change.put(LINKEDIN, linkedinID);

                db.collection("users").document(email).update(change)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditSocialsActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditSocialsActivity.this, "Error Happened", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.toString());
                            }
                        });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        socialsListener = profileRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d(TAG, e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String facebook = "", twitter = "", instagram = "", wechat = "", linkedin = "";
                    if(documentSnapshot.contains(FACEBOOK))
                        facebook = documentSnapshot.getString(FACEBOOK);
                    if(documentSnapshot.contains(TWITTER))
                        twitter = documentSnapshot.getString(TWITTER);
                    if(documentSnapshot.contains(INSTAGRAM))
                        instagram = documentSnapshot.getString(INSTAGRAM);
                    if(documentSnapshot.contains(WECHAT))
                        wechat = documentSnapshot.getString(WECHAT);
                    if(documentSnapshot.contains(LINKEDIN))
                        linkedin = documentSnapshot.getString(LINKEDIN);

                    facebookField.setText(facebook);
                    twitterField.setText(twitter);
                    instagramField.setText(instagram);
                    wechatField.setText(wechat);
                    linkedinFiled.setText(linkedin);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        socialsListener.remove();
    }
}
