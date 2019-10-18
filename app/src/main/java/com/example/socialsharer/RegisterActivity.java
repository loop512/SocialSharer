package com.example.socialsharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import com.example.socialsharer.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String RECORD = "total_user";
    private static final String INDEX = "index";

    private long userNumber;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mRePasswordField;
    private EditText mFullnameField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Intent previous = getIntent();
        Bundle bundle = previous.getExtras();
        if (bundle != null){
            userNumber = (long) bundle.get("userNumber");
        }

        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle(R.string.title_activity_register);

        mEmailField = findViewById(R.id.register_email);
        mPasswordField = findViewById(R.id.register_password);
        mRePasswordField = findViewById(R.id.register_confirm_password);
        mFullnameField = findViewById(R.id.register_name);

        final Button registerButton = findViewById(R.id.register_create_account);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
//                Intent startNext = new Intent(RegisterActivity.this,
//                        EditProfileActivity.class);
//                startNext.putExtra("userNumber", userNumber);
//                startNext.putExtra("email", userEmail);
//                startActivity(startNext);
            }
        });
    }

    private void createAccount(String email, String password){

        if(!validateForm()){
            return;
        }
        // Create new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(mFullnameField.getText().toString())
                                    .build();
                            user.updateProfile(profileUpdates);
                            Intent startNext = new Intent(RegisterActivity.this,
                                    EditProfileActivity.class);
                            startNext.putExtra("email", mEmailField.getText().toString());
                            startNext.putExtra("name", mFullnameField.getText().toString());
                            startNext.putExtra("userNumber", userNumber);
                            startActivity(startNext);
                            finish();
                        } else{
                            try{
                                throw task.getException();
                            } catch(Exception e){
                                new AlertDialog.Builder(RegisterActivity.this)
                                        .setTitle("Opps")
                                        .setMessage(e.getMessage())
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                            }
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        Pattern pattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
        String email = mEmailField.getText().toString();
        Matcher matcher = pattern.matcher(email);
        boolean emailFormat = matcher.matches();
        if(TextUtils.isEmpty(email)){
            mEmailField.setError("Required.");
            valid = false;
        } else if(emailFormat == false){
            mEmailField.setError("Email field is not email format");
            valid = false;
        }
        else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if(TextUtils.isEmpty(password)){
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String rePassword = mRePasswordField.getText().toString();
        if(!password.equals(rePassword)){
            mRePasswordField.setError("Password is not the same");
            valid = false;
        } else {
            mRePasswordField.setError(null);
        }
        return valid;
    }

    public void saveChanges(View v){
        Map<String, Object> change = new HashMap<>();
        change.put(INDEX, userNumber+1);

        Map<String, Object> record = new HashMap<>();
        record.put(RECORD, userNumber+1);
        db.collection("users").document("record").set(record)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Success update to database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Fail update to database");
                    }
                });

        db.collection("users").document(mEmailField.getText().toString()).update(change)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Success update to database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Fail update to database");
                    }
                });
    }
}
