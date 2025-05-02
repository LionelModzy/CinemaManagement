package ai.movie.modzy.Cloudiary;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map config = new HashMap();
        config.put("cloud_name", "dhejopg9q");
        config.put("api_key", "193285184664555");
        config.put("api_secret", "aqTlPAVHQKyEaJxIHoMxZcyOdDE");

        MediaManager.init(this, config);
    }
}

