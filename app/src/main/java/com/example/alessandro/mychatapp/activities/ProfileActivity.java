package com.example.alessandro.mychatapp.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alessandro.mychatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra(getString(R.string.intent_stringExtra_user_id));

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Friend_req_field));
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Friends_field));
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_notifications_field));
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_btn);

        mCurrent_state = getString(R.string.Friendship_status_notFriends);

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.progress_dialog_user_data_title);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_user_data_message));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child(getString(R.string.FB_name_field)).getValue().toString();
                String status = dataSnapshot.child(getString(R.string.FB_status_field)).getValue().toString();
                final String image = dataSnapshot.child(getString(R.string.FB_image_field)).getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                    }
                });

                if (mCurrent_user.getUid().equals(user_id)) {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }

                // FRIENDS LIST / REQUEST FEATURE

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child(getString(R.string.FB_friendship_request_type_field)).getValue().toString();

                            if (req_type.equals(getString(R.string.FB_notifications_type_received_value))) {

                                mCurrent_state = getString(R.string.Friendship_request_status_req_received);
                                mProfileSendReqBtn.setText(getString(R.string.accept_friend_request_text));

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);


                            } else if (req_type.equals(getString(R.string.FB_notifications_type_sent_value))) {

                                mCurrent_state = getString(R.string.Friendship_request_status_req_sent);
                                mProfileSendReqBtn.setText(getString(R.string.cancel_friend_request_text));

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = getString(R.string.Friendship_status_friends);
                                        mProfileSendReqBtn.setText(getString(R.string.Unfriend_this_person_text));

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);

                // NOT FRIENDS STATE

                if (mCurrent_state.equals(getString(R.string.Friendship_status_notFriends))) {

                    DatabaseReference newNotificationRef = mRootRef.child(getString(R.string.FB_notifications_field)).child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put(getString(R.string.FB_notifications_from_field), mCurrent_user.getUid());
                    notificationData.put(getString(R.string.FB_notifications_type_field), getString(R.string.FB_notifications_type_request_value));

                    Map requestMap = new HashMap();
                    requestMap.put(getString(R.string.FB_Friend_req_field) + "/" + mCurrent_user.getUid() + "/" + user_id + "/" + getString(R.string.FB_friendship_request_type_field), getString(R.string.FB_notifications_type_sent_value));
                    requestMap.put(getString(R.string.FB_Friend_req_field) + "/" + user_id + "/" + mCurrent_user.getUid() + "/" + getString(R.string.FB_friendship_request_type_field), getString(R.string.FB_notifications_type_received_value));
                    requestMap.put(getString(R.string.Notifications) + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, R.string.Toast_error_request_sending, Toast.LENGTH_SHORT).show();

                            } else {

                                mCurrent_state = getString(R.string.Friendship_request_status_req_sent);
                                mProfileSendReqBtn.setText(getString(R.string.cancel_friend_request_text));

                            }

                            mProfileSendReqBtn.setEnabled(true);


                        }
                    });

                }

                // CANCEL REQUEST STATE

                if (mCurrent_state.equals(getString(R.string.Friendship_request_status_req_sent))) {

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = getString(R.string.Friendship_status_notFriends);
                                    mProfileSendReqBtn.setText(getString(R.string.send_friend_request_text));

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                // REQUEST RECEIVED STATE

                if (mCurrent_state.equals(getString(R.string.Friendship_request_status_req_received))) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put(getString(R.string.FB_Friends_field) + "/" + mCurrent_user.getUid() + "/" + user_id + "/" + getString(R.string.FB_Friends_date_field), currentDate);
                    friendsMap.put(getString(R.string.FB_Friends_field) + "/" + user_id + "/" + mCurrent_user.getUid() + "/" + getString(R.string.FB_Friends_date_field), currentDate);

                    friendsMap.put(getString(R.string.FB_Friend_req_field) + "/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put(getString(R.string.FB_Friend_req_field) + "/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = getString(R.string.Friendship_status_friends);
                                mProfileSendReqBtn.setText(getString(R.string.Unfriend_this_person_text));

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                // UNFRIENDS

                if (mCurrent_state.equals(getString(R.string.Friendship_status_friends))) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put(getString(R.string.FB_Friends_field) + "/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put(getString(R.string.FB_Friends_field) + "/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mCurrent_state = getString(R.string.Friendship_status_notFriends);
                                mProfileSendReqBtn.setText(getString(R.string.send_friend_request_text));

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }
            }
        });
    }
}