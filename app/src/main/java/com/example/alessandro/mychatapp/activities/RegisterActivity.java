package com.example.alessandro.mychatapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.alessandro.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private ConstraintLayout constraintLayout;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        constraintLayout = findViewById(R.id.constraintRegisterLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),constraintLayout);
        }

        //Toolbar Set
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.create_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Android Fields
        mDisplayName = findViewById(R.id.register_display_name);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mCreateBtn = findViewById(R.id.register_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    mRegProgress.setTitle(getString(R.string.registering_user));
                    mRegProgress.setMessage(getString(R.string.registering_user_message));
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name, email, password);

                }else{
                    Toast.makeText(RegisterActivity.this, R.string.Toast_register_form_fields_empty, Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, ConstraintLayout constraintLayout) {
        snackbar = Snackbar
                .make(constraintLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction(R.string.snackbar_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(getString(R.string.RegisterNameSaved), mDisplayName.getEditText().getText().toString());
        outState.putString(getString(R.string.RegisterEmailSaved), mEmail.getEditText().getText().toString());
        outState.putString(getString(R.string.RegisterPasswordSaved), mPassword.getEditText().getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){

            String display_name = savedInstanceState.getString(getString(R.string.RegisterNameSaved));
            mDisplayName.getEditText().setText(display_name);

            String email = savedInstanceState.getString(getString(R.string.RegisterEmailSaved));
            mEmail.getEditText().setText(email);

            String password = savedInstanceState.getString(getString(R.string.RegisterPasswordSaved));
            mPassword.getEditText().setText(password);

        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(uid);

                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(getString(R.string.FB_name_field), display_name);
                    userMap.put(getString(R.string.FB_status_field), getString(R.string.default_status));
                    userMap.put(getString(R.string.FB_image_field), getString(R.string.default_image));
                    userMap.put(getString(R.string.FB_thumb_image_field), getString(R.string.default_thumb_image));
                    userMap.put(getString(R.string.FB_device_token_field), device_token);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }

                        }
                    });

                } else {

                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, R.string.registration_error_toast, Toast.LENGTH_LONG).show();

                }

            }
        });

    }
}