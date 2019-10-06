package com.example.socialsharer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import io.grpc.Context;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private String userEmail;
    private long userNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setTitle(R.string.title_activity_edit_profile);

//        Intent previous = getIntent();
//        Bundle bundle = previous.getExtras();
//        if (bundle != null){
//            userEmail = (String) bundle.get("email");
////            userNumber = (long) bundle.get("userNumber");
//        }
//        Log.i(TAG, "Register email: " + userEmail + " userNumber" + userNumber);

        final Button saveButton = findViewById(R.id.edit_save_profile);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startNext = new Intent(EditProfileActivity.this,
                        MainActivity.class);
                startNext.putExtra("userNumber", userNumber);
                startNext.putExtra("email", userEmail);
                startActivity(startNext);
            }
        });
    }

    public void uploadImage(){
        String storePath = userEmail + ".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference imageRef = storageReference.child(storePath);

        File file = new File(".R");
    }
}
