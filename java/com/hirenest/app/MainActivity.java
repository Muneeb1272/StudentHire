package com.hirenest.app;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "diw978rxl");
        config.put("api_key", "771768792759179");
        config.put("api_secret", "T9E-pZD9px6u0dboQnXTMEQdl3M");
        MediaManager.init(this, config);
    }
}