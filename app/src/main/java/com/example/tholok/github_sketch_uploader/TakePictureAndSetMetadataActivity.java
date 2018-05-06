package com.example.tholok.github_sketch_uploader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePictureAndSetMetadataActivity extends AppCompatActivity {

    private static String LOG_TAG = "TakePictureAndSetMetadataActivity";

    private ImageView imageView;
    private SeekBar sbCompressionLevel;

    String mCurrentPhotoPath;

    private SharedPreferences prefs;

    private EditText etBranch;
    private EditText etRepository;
    private EditText etPath;
    private EditText etCommitMessage;
    private TextView twFilesize;

    private boolean hasTakenPicture = false;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture_and_set_metadata);

        // prepare prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        // prepare components
        imageView = (ImageView) findViewById(R.id.iw_preview);
        etBranch = (EditText) findViewById(R.id.et_branch);
        etRepository = (EditText) findViewById(R.id.et_repository);
        etPath = (EditText) findViewById(R.id.et_path);
        etCommitMessage = (EditText) findViewById(R.id.et_commitmessage);
        sbCompressionLevel = (SeekBar) findViewById(R.id.sb_compression_level);
        twFilesize = (TextView) findViewById(R.id.tw_filesize);

        // make seekbar update preview on change
        sbCompressionLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                // update only if has taken picture
                if (hasTakenPicture) {
                    updatePreview();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


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

            // (try and) create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.d(LOG_TAG, "Couldn't create file to put image in!");
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
     * Handle returned values from image capture intent intent
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            /*
            NOTE that there is a bug somewhere in the next few lines. Sometimes 'imageBitmap' ends
            up as null when 'updatePreview()' gets called on my physical device. The problem does
            not appear in the emulator. It might be related to file size.
             */

            // read the taken pic
            readAndPrepareTakenPicture();

            // rotate if necessary
            rotateTakenPicIfNecessary();

            // write bitmap back to file to store any scaling and/or rotation done
            saveBitmapToFile(imageBitmap, mCurrentPhotoPath);

            // set the preview pic
            updatePreview();

            // store that has taken picture
            hasTakenPicture = true;
        } else {
            Log.d(LOG_TAG, "Picture was not taken correctly...");
        }
    }

    /**
     * Gets current chosen level of compression based on seekbar
     */
    private int getLevelOfCompression() {

        // 100 - X because seekbar is inverted
        return 100 - sbCompressionLevel.getProgress();
    }

    /**
     * Show the picture as it will look after upload (with compression applied), and update filesize
     */
    private void updatePreview() {

        // FIRST CONVERT INTO BASE64 STRING WITH CHOSEN LEVEL OF COMPRESSION

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, getLevelOfCompression(), byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64PictureString = Base64.encodeToString(byteArray, Base64.DEFAULT);


        // NOW CONVERT BACK TO BITMAP

        byte[] decodedString = Base64.decode(base64PictureString, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        imageView.setImageBitmap(decodedBitmap);


        // UPDATE FILESIZE ESTIMATE

        float kilobytes = byteArray.length / 1000;
        twFilesize.setText(kilobytes + " kb");

    }


    /**
     * Writes a given bitmap to file with quality 100
     *
     * @param bitmap
     * @param filePath
     */
    private static void saveBitmapToFile(Bitmap bitmap, String filePath) {

        try {
            File file = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // 100 because compression happens elsewhere
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(LOG_TAG, "Couldn't write the image to file");
        }

    }

    /**
     * Rotate a given image by a given angle
     *
     *taken from: https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * fetch the pic taken with the camera intent, scale it to fit the preview,
     * and return it
     *
     * Inspired by: https://developer.android.com/training/camera/photobasics.html
     */
    private void readAndPrepareTakenPicture() {

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




        // store bitmap in property
        imageBitmap = bitmap;
    }


    /**
     * Use exif info (hopefully) generated by phone while taking picture to orientate the
     * picture properly.
     *
     * taken from: https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
     */
    private void rotateTakenPicIfNecessary() {

        // try and rotate picture based on orientation stored in exif
        try {

            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    imageBitmap = rotateImage(imageBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    imageBitmap = rotateImage(imageBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    imageBitmap = rotateImage(imageBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    // orientation is correct. NOP
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(LOG_TAG, "Was unable to set orientation of picture from exif");
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

        // prepare intent to launch upload activity
        Intent intent = new Intent(this, UploadActivity.class);

        // put extra data for github upload
        intent.putExtra(UploadActivity.EXTRA_IMAGE_URI,         mCurrentPhotoPath);
        intent.putExtra(UploadActivity.EXTRA_COMPRESSION_LEVEL, getLevelOfCompression());
        intent.putExtra(UploadActivity.EXTRA_USERNAME,          "tholok97");                        // HARDCODED FOR NOW. SHOULD BE GOTTEN BASED ON TOKEN
        intent.putExtra(UploadActivity.EXTRA_REPOSITORY,        etRepository.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_TOKEN,             token);
        intent.putExtra(UploadActivity.EXTRA_PATH,              etPath.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_BRANCH,            etBranch.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_COMMIT_MESSAGE,    etCommitMessage.getText().toString());
        intent.putExtra(UploadActivity.EXTRA_EMAIL,             "thomahl@stud.ntnu.no");            // HARDCODED FOR NOW. SHOULD BE GOTTEN BASED ON TOKEN


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
