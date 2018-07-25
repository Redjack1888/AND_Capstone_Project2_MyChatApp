package com.example.alessandro.mychatapp.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.models.Users;
import com.example.alessandro.mychatapp.utils.SimpleDividerItemDecoration;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mSearchList;

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    FirebaseRecyclerAdapter<Users, SearchViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Search...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchField = findViewById(R.id.search_field);

        mSearchBtn = findViewById(R.id.search_btn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();

                firebaseUserSearch(searchText);

            }
        });

        mSearchList = findViewById(R.id.result_list);
        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new LinearLayoutManager(this));

    }


    private void firebaseUserSearch(String searchText) {
        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText.toUpperCase()).endAt(searchText.toLowerCase() + "\uf8ff");
        FirebaseRecyclerOptions searchOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(firebaseSearchQuery, Users.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, SearchViewHolder>(searchOptions) {
            @NonNull
            @Override
            public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_item, parent, false);

                return new SearchViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull Users model) {
                holder.setDisplayName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }

        };

        mSearchList.setAdapter(firebaseRecyclerAdapter);
        mSearchList.addItemDecoration(new SimpleDividerItemDecoration(this));
        firebaseRecyclerAdapter.startListening();

    }

    // View Holder Class

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SearchViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDisplayName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserStatus(String status) {

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        public void setUserImage(final String thumb_image) {

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get()
                    .load(thumb_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_avatar)
                    .into(userImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(thumb_image)
                                    .placeholder(R.drawable.default_avatar)
                                    .into(userImageView);

                        }
                    });

        }

    }
}
