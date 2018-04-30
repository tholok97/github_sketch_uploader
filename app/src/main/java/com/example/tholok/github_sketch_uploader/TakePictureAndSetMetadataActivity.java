package com.example.tholok.github_sketch_uploader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePictureAndSetMetadataActivity extends AppCompatActivity {

    private static String LOG_TAG = "TakePictureAndSetMetadataActivity";

    private ImageView imageView;

    String mCurrentPhotoPath;

    private SharedPreferences prefs;

    private EditText etBranch;
    private EditText etRepository;
    private EditText etPath;
    private EditText etCommitMessage;

    private boolean hasTakenPicture = false;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture_and_set_metadata);

        // set component properties
        imageView = (ImageView) findViewById(R.id.iw_preview);

        // set placeholder pic
        imageView.setImageResource(R.drawable.ic_launcher_background);

        // prepare prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        // prepare components
        etBranch = (EditText) findViewById(R.id.et_branch);
        etRepository = (EditText) findViewById(R.id.et_repository);
        etPath = (EditText) findViewById(R.id.et_path);
        etCommitMessage = (EditText) findViewById(R.id.et_commitmessage);

        // load fields from defaults stored in prefs
        loadFieldsFromPrefs();
    }


    /**
     * Create file with collision resistant name to store taken image in.
     * Taken from: https://developer.android.com/training/camera/photobasics.html
     */
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Make built-in camera app take over so a picture can be taken.
     * Location of picture taken is stored in property
     * Taken from: https://developer.android.com/training/camera/photobasics.html
     */
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                // TODO
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.tholok.github_sketch_uploader.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    /**
     * fetch the pic taken with the camera intent, scale it to fit the preview,
     * and point the preview to it
     *
     * Taken from: https://developer.android.com/training/camera/photobasics.html
     */
    private void setPic() {

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Handle returned values from image capture intent intent
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // set the preview pic
            setPic();

            // store that has taken picture
            hasTakenPicture = true;
        }
    }

    public void onButtonClick(View view) {

        Log.d(LOG_TAG, "clicked");

        // react on type of button that was clicked
        switch (view.getId()) {

            // take picture
            case R.id.b_takepicture:
                dispatchTakePictureIntent();
                break;

            // upload
            case R.id.b_upload:
                dispatchUpload();
                break;

            default:
                Log.d(LOG_TAG, "Button without case pressed");
                break;
        }
    }

    public void dispatchUpload() {

        Log.d(LOG_TAG, "starting upload flow");


        // load token
        String token = prefs.getString(PreferencesActivity.PREFS_TOKEN, "");

        // EARLY RETURN IF NOT APPROPRIATE TO UPLOAD YET:
        if (token == "") {

            // no token stored
            Toast.makeText(this, "No token stored on device. Cannot upload", Toast.LENGTH_SHORT).show();
            return;
        } else if (!hasTakenPicture) {

            // hasn't taken picture yet
            Toast.makeText(this, "Please take a picture first", Toast.LENGTH_SHORT).show();
            return;
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64PictureString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Intent intent = new Intent(this, UploadActivity.class);

        // put extra data for github upload
        intent.putExtra(UploadActivity.EXTRA_BITMAP_BASE64,     base64PictureString);
        intent.putExtra(UploadActivity.EXTRA_USERNAME,          "tholok97");                    // HARDCODED FOR NOW
        intent.putExtra(UploadActivity.EXTRA_REPOSITORY,        etRepository.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_TOKEN,             token);
        intent.putExtra(UploadActivity.EXTRA_PATH,              etPath.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_BRANCH,            etBranch.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_COMMIT_MESSAGE,    etCommitMessage.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_EMAIL,             "thomahl@stud.ntnu.no");        // HARDCODED FOR NOW


        startActivity(intent);
    }

    void loadFieldsFromPrefs() {

        etBranch.setText(prefs.getString(PreferencesActivity.PREFS_BRANCH, ""));
        etRepository.setText(prefs.getString(PreferencesActivity.PREFS_REPOSITORY, ""));
        etCommitMessage.setText(prefs.getString(PreferencesActivity.PREFS_COMMIT_MESSAGE, ""));

        String filenameSuggestion = generateFileNameSuggestion();
        String path = prefs.getString(PreferencesActivity.PREFS_FOLDER, "");

        etPath.setText(path + filenameSuggestion);
    }

    String generateFileNameSuggestion() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return "sketch_" + timeStamp + ".jpg";
    }

}
