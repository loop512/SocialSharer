package com.example.socialsharer.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.example.socialsharer.ForgotPasswordActivity;
import com.example.socialsharer.MainActivity;
import com.example.socialsharer.R;
import com.example.socialsharer.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private String userEmail;
    private long userNumber;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView createAccountTextView;
    private Button loginButton;
    private TextView forgotPasswordTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getUserNumber();

        getSupportActionBar().setTitle(R.string.title_activity_login);
        setContentView(R.layout.activity_login);
//        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
//                .get(LoginViewModel.class);

        mEmailField = findViewById(R.id.username);
        mPasswordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        createAccountTextView = findViewById(R.id.createAccount);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
//        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

//        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
//            @Override
//            public void onChanged(@Nullable LoginFormState loginFormState) {
//                if (loginFormState == null) {
//                    return;
//                }
//                loginButton.setEnabled(loginFormState.isDataValid());
//                if (loginFormState.getUsernameError() != null) {
//                    mEmailField.setError(getString(loginFormState.getUsernameError()));
//                }
//                if (loginFormState.getPasswordError() != null) {
//                    mPasswordField.setError(getString(loginFormState.getPasswordError()));
//                }
//            }
//        });

//        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
//            @Override
//            public void onChanged(@Nullable LoginResult loginResult) {
//                if (loginResult == null) {
//                    return;
//                }
//                loadingProgressBar.setVisibility(View.GONE);
//                if (loginResult.getError() != null) {
//                    showLoginFailed(loginResult.getError());
//                }
//                if (loginResult.getSuccess() != null) {
//                    updateUiWithUser(loginResult.getSuccess());
//                }
//                setResult(Activity.RESULT_OK);
//
//                //Complete and destroy login activity once successful
//                finish();
//            }
//        });

//        TextWatcher afterTextChangedListener = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // ignore
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // ignore
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                loginViewModel.loginDataChanged(mEmailField.getText().toString(),
//                        mPasswordField.getText().toString());
//            }
//        };
//        mEmailField.addTextChangedListener(afterTextChangedListener);
//        mPasswordField.addTextChangedListener(afterTextChangedListener);
//        mPasswordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    loginViewModel.login(mEmailField.getText().toString(),
//                            mPasswordField.getText().toString());
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    return true;
//                }
//                return false;
//            }
//        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn(mEmailField.getText().toString(), mPasswordField.getText().toString());

//                loadingProgressBar.setVisibility(View.VISIBLE);
//                loginViewModel.login(mEmailField.getText().toString(),
//                        mPasswordField.getText().toString());
                // If success, pass user email and user number to next activity
//                userEmail = mEmailField.getText().toString();
//                Intent startNext = new Intent(LoginActivity.this,
//                        MainActivity.class);
//                startNext.putExtra("userNumber", userNumber);
//                startNext.putExtra("email", userEmail);
//                startActivity(startNext);
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If create account, pass user number to next activity
                Intent startNext = new Intent(LoginActivity.this,
                        ForgotPasswordActivity.class);
                startActivity(startNext);
            }
        });

        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If create account, pass user number to next activity
                Intent startNext = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startNext.putExtra("userNumber", userNumber);
                startActivity(startNext);
            }
        });
    }

//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
//        // TODO : initiate successful logged in experience
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }
//
//    private void showLoginFailed(@StringRes Integer errorString) {
//        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
//    }

    private void getUserNumber(){
        DocumentReference docRef = db.collection("users").
                document("record");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userNumber = (long) document.get("total_user");
                        Log.d(TAG, "User number:" + userNumber);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void logIn(String email, String password){
        if(!validateForm()){
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.i(TAG, "welcome " + user.getDisplayName());
                            Toast.makeText(LoginActivity.this, "hello", Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this,"welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            SharedPreferences shared = getSharedPreferences("userInformation", MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString("uname", user.getDisplayName()).commit();
                            editor.putString("email", user.getEmail()).commit();
                            Intent startNext = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            startNext.putExtra("userNumber", userNumber);
                            startNext.putExtra("email", user.getEmail());
                            startNext.putExtra("uname", user.getDisplayName());
                            startActivity(startNext);
                            finish();
                        } else {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            switch (errorCode) {

                                case "ERROR_INVALID_CUSTOM_TOKEN":
                                    Toast.makeText(LoginActivity.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                    Toast.makeText(LoginActivity.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_CREDENTIAL":
                                    Toast.makeText(LoginActivity.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_EMAIL":
                                    Toast.makeText(LoginActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                                    mEmailField.setError("The email address is badly formatted.");
                                    mEmailField.requestFocus();
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    Toast.makeText(LoginActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                    mPasswordField.setError("password is incorrect ");
                                    mPasswordField.requestFocus();
                                    mPasswordField.setText("");
                                    break;

                                case "ERROR_USER_MISMATCH":
                                    Toast.makeText(LoginActivity.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_REQUIRES_RECENT_LOGIN":
                                    Toast.makeText(LoginActivity.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    Toast.makeText(LoginActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    Toast.makeText(LoginActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                    mEmailField.setError("The email address is already in use by another account.");
                                    mEmailField.requestFocus();
                                    break;

                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                    Toast.makeText(LoginActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_DISABLED":
                                    Toast.makeText(LoginActivity.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_TOKEN_EXPIRED":
                                    Toast.makeText(LoginActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_NOT_FOUND":
                                    Toast.makeText(LoginActivity.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_USER_TOKEN":
                                    Toast.makeText(LoginActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_OPERATION_NOT_ALLOWED":
                                    Toast.makeText(LoginActivity.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    Toast.makeText(LoginActivity.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                                    mPasswordField.setError("The password is invalid it must 6 characters at least");
                                    mPasswordField.requestFocus();
                                    break;

                            }
//                            Toast.makeText(LoginActivity.this, "Authentication failed." + task.getException(),Toast.LENGTH_SHORT).show();
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
            mEmailField.setError("Emailfield is not email format");
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
        return valid;
    }


    private void resetPassword(String email){
        Pattern pattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
        Matcher matcher = pattern.matcher(email);
        boolean emailFormat = matcher.matches();
        if(TextUtils.isEmpty(email)){
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Opps")
                    .setMessage("Please enter an email address")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();

        }  else if (emailFormat == false){
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Opps")
                    .setMessage("The email field you enter is not email format")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Reset Email has been sent",Toast.LENGTH_SHORT).show();
                            } else {
                                try{
                                    throw task.getException();
                                } catch(FirebaseAuthInvalidUserException e){
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle("Opps")
                                            .setMessage(e.getMessage())
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            }).show();
                                } catch (Exception e){
                                    Toast.makeText(LoginActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
        }
    }
}
