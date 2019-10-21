package com.example.socialsharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.grpc.Context;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final String HOME_ADDRESS = "Home Address";
    private static final String DOB = "Data Of Birth";
    private static final String CONTACT_NUMBER = "Contact Number";
    private static final String OCCUPATION = "Occupation";
    private static final String INTRODUCE = "Introduction";

    private EditText editAddress, editDob, editNumber, editJob, editIntroduce;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = auth.getCurrentUser().getEmail().toString();
    private DocumentReference profileRef = db.collection("users").document(email);
    private ListenerRegistration profileListener;
    private boolean updateStatus = false;

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

        final Button skipBtn = findViewById(R.id.edit_skip);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        updateStatus = true;
                    } else {
                        updateStatus = false;
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

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
        change.put(INTRODUCE, introduce);
        if (updateStatus) {
            db.collection("users").document(email).update(change)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                            Intent startNext = new Intent(EditProfileActivity.this,
                                    MainActivity.class);
                            startActivity(startNext);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Error Happened", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
        } else {
            db.collection("users").document(email).set(change)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                            Intent startNext = new Intent(EditProfileActivity.this,
                                    MainActivity.class);
                            startActivity(startNext);
                            finish();
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

    @Override
    public void onStart() {
        super.onStart();
        profileListener = profileRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d(TAG, e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String address = "", job = "", number = "", introduce = "", dob = "";
                    if(documentSnapshot.contains(HOME_ADDRESS))
                        address = documentSnapshot.getString(HOME_ADDRESS);
                    if(documentSnapshot.contains(OCCUPATION))
                        job = documentSnapshot.getString(OCCUPATION);
                    if(documentSnapshot.contains(CONTACT_NUMBER))
                        number = documentSnapshot.getString(CONTACT_NUMBER);
                    if(documentSnapshot.contains(INTRODUCE))
                        introduce = documentSnapshot.getString(INTRODUCE);
                    if(documentSnapshot.contains(DOB))
                        dob = documentSnapshot.getString(DOB);

                    editAddress.setText(address);
                    editJob.setText(job);
                    editNumber.setText(number);
                    editIntroduce.setText(introduce);
                    editDob.setText(dob);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        profileListener.remove();
    }
}
