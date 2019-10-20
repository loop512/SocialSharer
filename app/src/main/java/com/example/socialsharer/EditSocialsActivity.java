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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

public class EditSocialsActivity extends AppCompatActivity {

    private static final String TAG = "EditSocialsActivity";
    private static final String FACEBOOK = "Facebook";
    private static final String TWITTER = "Twitter";
    private static final String INSTAGRAM = "Instagram";
    private static final String WECHAT = "Wechat";
    private static final String LINKEDIN = "LinkedIn";
    private EditText edit_facebook, edit_twitter, edit_ins, edit_wechat, edit_linkedin;
    private Button saveSocials, cancel;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = auth.getCurrentUser().getEmail().toString();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private long userNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_socials);
        getSupportActionBar().setTitle(R.string.title_activity_edit_socials);

        edit_facebook = findViewById(R.id.edit_facebook);
        edit_twitter = findViewById(R.id.edit_twitter);
        edit_ins = findViewById(R.id.edit_instagram);
        edit_wechat = findViewById(R.id.edit_wechat);
        edit_linkedin = findViewById(R.id.edit_linkedin);
        cancel = findViewById(R.id.edit_cancel);

        Intent previous = getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null){
            userNumber = (long) bundle.get("userNumber");
        }
        
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startNext = new Intent(EditSocialsActivity.this,
                        MainActivity.class);
                startNext.putExtra("userNumber", userNumber);
                startNext.putExtra("email", email);
                startActivity(startNext);
                finish();
            }
        });
    }
    
    public void saveSocials(View v){
        String facebook = edit_facebook.getText().toString();
        String twitter = edit_twitter.getText().toString();
        String ins = edit_ins.getText().toString();
        String wechat = edit_wechat.getText().toString();
        String linkedin = edit_linkedin.getText().toString();

        HashMap<String, Object> social_data = new HashMap<>();
        social_data.put(FACEBOOK,facebook);
        social_data.put(TWITTER,twitter);
        social_data.put(INSTAGRAM,ins);
        social_data.put(WECHAT,wechat);
        social_data.put(LINKEDIN,linkedin);
        
        db.collection("users").document(email).update(social_data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EditSocialsActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                Intent startNext = new Intent(EditSocialsActivity.this,
                        MainActivity.class);
                startNext.putExtra("userNumber", userNumber);
                startActivity(startNext);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditSocialsActivity.this, "Error Happened", Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });
    }
}
