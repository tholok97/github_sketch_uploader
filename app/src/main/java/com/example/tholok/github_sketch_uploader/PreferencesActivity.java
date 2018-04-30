package com.example.tholok.github_sketch_uploader;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PreferencesActivity extends AppCompatActivity {

    private static String LOG_TAG = "PreferencesActivity";

    private EditText etToken;
    private EditText etFolder;
    private EditText etCommitMessage;
    private EditText etRepository;
    private EditText etBranch;

    public static String PREFS_TOKEN           = "token";
    public static String PREFS_FOLDER          = "folder";
    public static String PREFS_COMMIT_MESSAGE  = "commit_message";
    public static String PREFS_REPOSITORY      = "repository";
    public static String PREFS_BRANCH          = "branch";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // store fields in private properties
        etToken         = (EditText) findViewById(R.id.et_token);
        etFolder        = (EditText) findViewById(R.id.et_folder);
        etCommitMessage = (EditText) findViewById(R.id.et_commitmessage);
        etRepository    = (EditText) findViewById(R.id.et_repository);
        etBranch        = (EditText) findViewById(R.id.et_branch);

        // prepare prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        loadFieldsFromPreferences();
    }

    void onButtonClick(View view) {

        switch (view.getId()) {

            case R.id.b_savedefaults:
                saveDefaultsFromFields();
                break;

            case R.id.b_testandsave:
                testAndSaveTokenFromField();
                break;

            default:
                Log.d(LOG_TAG, "unknown button clicked: " + view.getId());
                break;

        }
    }

    /**
     * Fill fields with values stored in preferences
     */
    void loadFieldsFromPreferences() {

        etToken.setText(prefs.getString(PREFS_TOKEN,                    "token"));
        etFolder.setText(prefs.getString(PREFS_FOLDER,                  "folder"));
        etCommitMessage.setText(prefs.getString(PREFS_COMMIT_MESSAGE,   "commitMessage"));
        etRepository.setText(prefs.getString(PREFS_REPOSITORY,          "repository"));
        etBranch.setText(prefs.getString(PREFS_BRANCH,                  "branch"));
    }

    /**
     * test the inputted token and store it if it checks out
     * TODO: set defaults based on inputted token? Repository, branch, (username, email,) ++?
     */
    void testAndSaveTokenFromField() {

        // get editor
        SharedPreferences.Editor prefsEditor = prefs.edit();

        // store from fields
        prefsEditor.putString(PREFS_TOKEN, etToken.getText().toString());

        prefsEditor.apply();

        Toast.makeText(this, "Token saved! (wasn't tested...)", Toast.LENGTH_SHORT).show();
    }

    /**
     * save defaults in preferences based on input in fields
     */
    void saveDefaultsFromFields() {

        // get editor
        SharedPreferences.Editor prefsEditor = prefs.edit();

        // store from fields
        prefsEditor.putString(PREFS_FOLDER, etFolder.getText().toString());
        prefsEditor.putString(PREFS_COMMIT_MESSAGE, etCommitMessage.getText().toString());
        prefsEditor.putString(PREFS_REPOSITORY, etRepository.getText().toString());
        prefsEditor.putString(PREFS_BRANCH, etBranch.getText().toString());

        prefsEditor.apply();

        Toast.makeText(this, "Defaults saved!", Toast.LENGTH_SHORT).show();
    }
}
