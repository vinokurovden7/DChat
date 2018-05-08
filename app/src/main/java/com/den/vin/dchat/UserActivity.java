package com.den.vin.dchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class UserActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUserlist;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Все пользователи");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRegProgress = new ProgressDialog(this);
        mRegProgress.setTitle("Загрузка данных");
        mRegProgress.setMessage("Загрузка списка пользователей, пожалуйста подождите...");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();

        mUserlist = (RecyclerView) findViewById(R.id.users_list);
        mUserlist.setHasFixedSize(true);
        mUserlist.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users users, int position) {

               viewHolder.setName(users.getName());
               viewHolder.setUserStatus(users.getStatus());
                mRegProgress.dismiss();

            }
        };

        mUserlist.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserStatus(String status){

            TextView userStatusView =  mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }


    }

}
