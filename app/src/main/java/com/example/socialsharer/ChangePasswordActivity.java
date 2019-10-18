package com.example.socialsharer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setTitle(R.string.title_activity_change_password);
    }
}
