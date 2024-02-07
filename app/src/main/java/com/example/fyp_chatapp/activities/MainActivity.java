package com.example.fyp_chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.fyp_chatapp.R;
import com.example.fyp_chatapp.utilities.Constants;
import com.example.fyp_chatapp.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        loginBtn = findViewById(R.id.main_loginBtn);
        registerBtn = findViewById(R.id.main_registerBtn);
        initView();
    }

    private void initView() {
        loginBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignInActivity.class)));
        registerBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
    }
}