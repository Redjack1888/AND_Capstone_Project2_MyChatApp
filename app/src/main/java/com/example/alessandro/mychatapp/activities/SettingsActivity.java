package com.example.alessandro.mychatapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alessandro.mychatapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;

    // Firebase
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    //Android Layout
    private CircleImageView mDisplayImage;

    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    Bitmap thumb_bitmap = null;

    private Snackbar snackbar;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        relativeLayout = findViewById(R.id.relativeSettingsLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection), relativeLayout);
        }

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.FB_Users_field)).child(current_uid);
        mUserDatabase.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mDisplayImage = findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_name);
        mStatus = findViewById(R.id.settings_status);

        mStatusBtn = findViewById(R.id.settings_status_btn);
        mImageBtn = findViewById(R.id.settings_image_btn);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child(getString(R.string.FB_name_field)).getValue().toString();
                final String image = dataSnapshot.child(getString(R.string.FB_image_field)).getValue().toString();
                String status = dataSnapshot.child(getString(R.string.FB_status_field)).getValue().toString();
                String thumb_image = dataSnapshot.child(getString(R.string.FB_thumb_image_field)).getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals(getString(R.string.default_image))) {

                    Picasso.get()
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar)
                            .into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(image)
                                            .placeholder(R.drawable.default_avatar)
                                            .into(mDisplayImage);

                                }
                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra(getString(R.string.status_value), status_value);
                startActivity(statusIntent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType(getString(R.string.gallery_intent_setType));
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.settings_select_image)), GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle(getString(R.string.progress_uploading_image));
                mProgressDialog.setMessage(getString(R.string.progress_uploading_image_message));
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePathUri = new File(resultUri.getPath());

                String current_user_id = mCurrentUser.getUid();

                // Compress Profile Image
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                final byte[] thumb_byte = stream.toByteArray();


                final StorageReference filepath = mImageStorage.child(getString(R.string.FB_storage_profile_images_field)).child(current_user_id + getString(R.string.jpg_extension));

                // Profile Image
                filepath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            final Uri downloadUri = task.getResult();
                            String downloadUrl = downloadUri.toString();

                            mUserDatabase.child(getString(R.string.FB_image_field)).setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, R.string.Toast_profile_image_update_success, Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        } else {

                            Toast.makeText(SettingsActivity.this, R.string.Toast_profile_image_update_error, Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }

                    }
                });
                // Thumb image
                final StorageReference thumb_filepath = mImageStorage.child(getString(R.string.FB_storage_profile_images_field)).child(getString(R.string.FB_storage_thumbs_field)).child(current_user_id + getString(R.string.jpg_extension));
                thumb_filepath.putBytes(thumb_byte).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) throws Exception {
                        if (!thumb_task.isSuccessful()) {
                            throw Objects.requireNonNull(thumb_task.getException());
                        }
                        return thumb_filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> thumb_task) {
                        if (thumb_task.isSuccessful()) {
                            final Uri thumb_downloadUri = thumb_task.getResult();
                            String thumb_downloadUrl = thumb_downloadUri.toString();

                            mUserDatabase.child(getString(R.string.FB_thumb_image_field)).setValue(thumb_downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, R.string.Toast_thumbnail_success_upload, Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        } else {

                            Toast.makeText(SettingsActivity.this, R.string.Toast_error_thumbnail_upload, Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, RelativeLayout relativeLayout)
    {
        snackbar = Snackbar
                .make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction(R.string.snackbar_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }

}