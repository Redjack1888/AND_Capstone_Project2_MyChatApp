package com.example.alessandro.mychatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alessandro.mychatapp.utils.adapters.MessageAdapter;
import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.models.Messages;
import com.example.alessandro.mychatapp.utils.GetTime;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chat_user_id;
    private String chat_user_name;
    private String current_user_id;
    private Toolbar mChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    private DatabaseReference mRootRef;
    private TextView chatUserName;
    private TextView chatUserLastSeen;
    private CircleImageView chatUserImage;
    private EditText chatMessageEditText;
    private ImageButton chatMessageAddBtn;
    private ImageButton chatMessageSendBtn;
    private RecyclerView mMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private LinearLayout mMessageLinearLayout;
    //    private boolean exist ;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private RelativeLayout relativeLayout;
//    Context mContext;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Getting application context
//        mContext = getApplicationContext();

        chat_user_id = getIntent().getStringExtra(getString(R.string.intent_stringExtra_user_id));
        chat_user_name = getIntent().getStringExtra(getString(R.string.intent_stringExtra_chatUserName));
        mChatToolbar = findViewById(R.id.chat_bar_layout);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        relativeLayout = findViewById(R.id.relativeChatLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),relativeLayout);
        }

        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            current_user_id = mAuth.getCurrentUser().getUid();
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View custom_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(custom_view);

        chatUserName = findViewById(R.id.custom_bar_display_name);
        chatUserLastSeen = findViewById(R.id.custom_bar_last_seen);
        chatUserImage = findViewById(R.id.custom_bar_image);
        if (chat_user_name.length() < 20) {
            chatUserName.setText(chat_user_name);
        } else {
            chat_user_name = chat_user_name.substring(0, 17) + getString(R.string.ellipsis);
            chatUserName.setText(chat_user_name);
        }

        mAdapter = new MessageAdapter(messagesList, this);
        mMessageList = findViewById(R.id.message_list_recycler_view);
        mMessageLinearLayout = findViewById(R.id.chat_message_linear_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));
        mRootRef.keepSynced(true);

        mUsersRef.keepSynced(true);
        loadMessages();
        mRootRef.child(getString(R.string.FB_Friends_field)).child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //  if (dataSnapshot.child(chat_user_id).exists())
                //  {
                mMessageLinearLayout.setVisibility(View.VISIBLE);
                int optionId = dataSnapshot.child(chat_user_id).exists() ? R.layout.send_message : R.layout.unable_to_send_message;

                View C = findViewById(R.id.chat_message_linear_layout);
                ViewGroup parent = (ViewGroup) C.getParent();
                int index = parent.indexOfChild(C);
                parent.removeView(C);
                C = getLayoutInflater().inflate(optionId, parent, false);
                parent.addView(C, index);

                if (optionId == R.layout.send_message) {
                    chatMessageEditText = C.findViewById(R.id.chat_message_edit_text);
                    chatMessageAddBtn = C.findViewById(R.id.chat_message_add_btn);
                    chatMessageSendBtn = C.findViewById(R.id.chat_message_send_btn);

                    chatMessageSendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage();
                        }
                    });


                    chatMessageAddBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent galleryIntent = new Intent();
                            galleryIntent.setType(getString(R.string.gallery_intent_setType));
                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.gallery_intent_title)), GALLERY_PICK);
                        }
                    });
                }
                //  }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsersRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thumbImage = dataSnapshot.child(getString(R.string.FB_thumb_image_field)).getValue().toString();
                Picasso.get().load(thumbImage).placeholder(R.drawable.square_image_placeholder).into(chatUserImage);
                String online = dataSnapshot.child(getString(R.string.FB_users_online_field)).getValue().toString();

                if (online.equals(getString(R.string.boolean_true_string))) {
                    chatUserLastSeen.setText(getString(R.string.status_online_text));
                } else {
                    GetTime getTimeAgo = new GetTime();
                    long last_seen = Long.parseLong(online);
                    String last_seen_time = getTimeAgo.getTimeAgo(last_seen, getApplicationContext());
                    chatUserLastSeen.setText(last_seen_time);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            final String currentUserRef = getString(R.string.FB_Messages_field) + "/" + current_user_id + "/" + chat_user_id;
            final String chatUserRef = getString(R.string.FB_Messages_field) + "/" + chat_user_id + "/" + current_user_id;

            final String currentUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + current_user_id + "/" + chat_user_id;
            final String chatUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + chat_user_id + "/" + current_user_id;

            DatabaseReference messageUserRef = mRootRef.child(getString(R.string.FB_Messages_field)).child(current_user_id).child(chat_user_id).push();
            final String pushId = messageUserRef.getKey();

            final StorageReference filePath = mImageStorage.child(getString(R.string.FB_storage_message_images_field)).child(pushId + getString(R.string.jpg_extension));

            filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        String downloadUrl = downloadUri.toString();

                        Map messageMap = new HashMap();
                        messageMap.put(getString(R.string.FB_message_field), downloadUrl);
                        messageMap.put(getString(R.string.FB_message_seen_field), false);
                        messageMap.put(getString(R.string.FB_message_type_field), getString(R.string.FB_message_type_image_field));
                        messageMap.put(getString(R.string.FB_message_time_field), ServerValue.TIMESTAMP);
                        messageMap.put(getString(R.string.FB_message_from_field), current_user_id);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                        messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                        Map lastMessageMap = new HashMap();
                        lastMessageMap.put(getString(R.string.FB_lastMessageKey_field), pushId);

                        Map lastMessageUserMap = new HashMap();
                        lastMessageUserMap.put(currentUserMessageRef, lastMessageMap);
                        lastMessageUserMap.put(chatUserMessageRef, lastMessageMap);

                        chatMessageEditText.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    loadMessages();
                                }
                            }
                        });

                        mRootRef.updateChildren(lastMessageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMessages() {
        //
        DatabaseReference messagesRef = mRootRef.child(getString(R.string.FB_Messages_field)).child(current_user_id).child(chat_user_id);
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //  }

    }

    private void sendMessage() {
        String message = chatMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String currentUserRef = getString(R.string.FB_Messages_field) + "/" + current_user_id + "/" + chat_user_id;
            String chatUserRef = getString(R.string.FB_Messages_field) + "/" + chat_user_id + "/" + current_user_id;

            String currentUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + current_user_id + "/" + chat_user_id;
            String chatUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + chat_user_id + "/" + current_user_id;

            DatabaseReference messageUserRef = mRootRef.child(getString(R.string.FB_Messages_field)).child(current_user_id).child(chat_user_id).push();
            String pushId = messageUserRef.getKey();

            Map messageMap = new HashMap();
            messageMap.put(getString(R.string.FB_message_field), message);
            messageMap.put(getString(R.string.FB_message_seen_field), false);
            messageMap.put(getString(R.string.FB_message_type_field), getString(R.string.FB_message_type_text_field));
            messageMap.put(getString(R.string.FB_message_time_field), ServerValue.TIMESTAMP);
            messageMap.put(getString(R.string.FB_message_from_field), current_user_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

            Map lastMessageMap = new HashMap();
            lastMessageMap.put(getString(R.string.FB_lastMessageKey_field), pushId);

            Map lastMessageUserMap = new HashMap();
            lastMessageUserMap.put(currentUserMessageRef, lastMessageMap);
            lastMessageUserMap.put(chatUserMessageRef, lastMessageMap);

            chatMessageEditText.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        loadMessages();
                    }
                }
            });

            mRootRef.updateChildren(lastMessageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        } else {
            mUsersRef.child(currentUser.getUid()).child(getString(R.string.FB_users_online_field)).setValue(getString(R.string.boolean_true_string));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUsersRef.child(currentUser.getUid()).child(getString(R.string.FB_users_online_field)).setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {

        Intent startIntent = new Intent(ChatActivity.this, HomeActivity.class);
        startActivity(startIntent);
        finish();
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
                        setAction((R.string.snackbar_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }


}