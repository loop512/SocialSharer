package com.example.socialsharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final String HOME_ADDRESS = "Home Address";
    private static final String DOB = "Data Of Birth";
    private static final String CONTACT_NUMBER = "Contact Number";
    private static final String OCCUPATION = "Occupation";
    private static final String INTRODUCE = "Introduce";

    private EditText editAddress, editDob, editNumber, editJob, editIntroduce;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = auth.getCurrentUser().getEmail().toString();
    private long userNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setTitle(R.string.title_activity_edit_profile);

        editAddress = findViewById(R.id.edit_address);
        editDob = findViewById(R.id.edit_dob);
        editNumber = findViewById(R.id.edit_number);
        editJob = findViewById(R.id.edit_profession);
        editIntroduce = findViewById(R.id.edit_intro);

        Intent previous = getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null){
            userNumber = (long) bundle.get("userNumber");
        }

        final Button skipBtn = findViewById(R.id.edit_skip);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startNext = new Intent(EditProfileActivity.this,
                        MainActivity.class);
                startNext.putExtra("userNumber", userNumber);
                startNext.putExtra("email", email);
                startActivity(startNext);
                finish();
            }
        });
    }

//    public void uploadImage(){
//        String storePath = userEmail + ".jpg";
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageReference = storage.getReference();
//        StorageReference imageRef = storageReference.child(storePath);
//
//        File file = new File(".R");
//    }
    public void saveChanges(View v){
        String address = editAddress.getText().toString();
        String dob = editDob.getText().toString();
        String number = editNumber.getText().toString();
        String job = editJob.getText().toString();
        String introduce = editIntroduce.getText().toString();

        Map<String, Object> change = new HashMap<>();
        change.put(HOME_ADDRESS,address);
        change.put(DOB,dob);
        change.put(CONTACT_NUMBER,number);
        change.put(OCCUPATION,job);


        db.collection("users").document(email).update(change)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfileActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                        Intent startNext = new Intent(EditProfileActivity.this,
                                MainActivity.class);
                        startNext.putExtra("userNumber", userNumber);
                        startActivity(startNext);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Error Happened", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }
}
