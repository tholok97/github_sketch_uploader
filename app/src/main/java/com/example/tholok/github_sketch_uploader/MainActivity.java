package com.example.tholok.github_sketch_uploader;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View view) {

        switch (view.getId()) {
            case R.id.b_upload:
                Intent uploadIntent = new Intent(this, TakePictureAndSetMetadataActivity.class);
                startActivity(uploadIntent);
                break;

            case R.id.b_preferences:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                break;

            case R.id.b_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

            default:
                Log.d(LOG_TAG, "unknown button pressed: " + view.getId());
                break;
        }
    }
}
