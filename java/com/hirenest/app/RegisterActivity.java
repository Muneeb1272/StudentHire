package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to Role Selection
        startActivity(new Intent(this,
                RoleSelectionActivity.class));
        finish();
    }
}