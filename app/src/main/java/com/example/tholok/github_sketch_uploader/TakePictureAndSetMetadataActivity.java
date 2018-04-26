package com.example.tholok.github_sketch_uploader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePictureAndSetMetadataActivity extends AppCompatActivity {

    private static String LOG_TAG = "TakePictureAndSetMetadataActivity";

    private ImageView imageView;

    String mCurrentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture_and_set_metadata);

        // set component properties
        imageView = (ImageView) findViewById(R.id.iw_preview);

        // set placeholder pic
        imageView.setImageResource(R.drawable.ic_launcher_background);
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
            setPic();
        }
    }

    public void onButtonClick(View view) {

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

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64PictureString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Intent intent = new Intent(this, UploadActivity.class);

        // put extra data for github upload
        intent.putExtra(UploadActivity.EXTRA_BITMAP_BASE64,     base64PictureString);
        intent.putExtra(UploadActivity.EXTRA_USERNAME,          "tholok97");
        intent.putExtra(UploadActivity.EXTRA_REPOSITORY,        "test");
        intent.putExtra(UploadActivity.EXTRA_TOKEN,             "REMOVEDFORPUSH");
        intent.putExtra(UploadActivity.EXTRA_PATH,              "data/test.jpg");
        intent.putExtra(UploadActivity.EXTRA_BRANCH,            "branchtest");
        intent.putExtra(UploadActivity.EXTRA_COMMIT_MESSAGE,    "a commit message ");
        intent.putExtra(UploadActivity.EXTRA_EMAIL,             "thomahl@stud.ntnu.no");


        startActivity(intent);
    }


}
