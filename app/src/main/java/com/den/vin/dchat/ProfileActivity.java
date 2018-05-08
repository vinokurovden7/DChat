package com.den.vin.dchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileStatus, profileFriendsCount;
    private ImageView profileImage;
    private Button profileBtn, deleteBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrent_user;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.profile_displayName);
        profileStatus = (TextView) findViewById(R.id.profile_status);
        profileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        profileBtn = (Button) findViewById(R.id.profile_send_cont_btn);
        deleteBtn = (Button) findViewById(R.id.delete_friend_btn);

        current_state = "not_friends";

        mRegProgress = new ProgressDialog(this);
        mRegProgress.setTitle("Загрузка данных");
        mRegProgress.setMessage("Загрузка данных пользователя, пожалуйста подождите...");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileName.setText(display_name);
                profileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_account_icon).into(profileImage);

                //------------------------------ FRIENDS LIST / REQUEST FEATURE --------------------------------------

                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        if(dataSnapshot.hasChild(user_id)){

                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){

                                current_state = "req_received";
                                profileBtn.setText("Принять запрос на добавление");

                            } else if(request_type.equals("sent")) {

                                current_state = "req_sent";
                                profileBtn.setText("Отменить запрос на добавление");

                            }

                            mRegProgress.dismiss();

                        } else {

                           mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {

                                   if(dataSnapshot.hasChild(user_id)){

                                       current_state = "friends";
                                       profileBtn.setText("Удалить контакт");

                                   }

                                   mRegProgress.dismiss();

                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                                   mRegProgress.dismiss();

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

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profileBtn.setEnabled(false);

                //------------------------ Не в друзьях -----------------------

                if(current_state.equals("not_friends")){

                   mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {

                           if(task.isSuccessful()){

                               mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {

                                       profileBtn.setEnabled(true);
                                       current_state = "req_sent";
                                       profileBtn.setText("Отменить запрос на добавление");

                                       Toast.makeText(ProfileActivity.this, "Запрос успешно отправлен", Toast.LENGTH_LONG).show();

                                   }
                               });

                           }else {

                               Toast.makeText(ProfileActivity.this, "Ошибка: "+task.getException().toString(), Toast.LENGTH_LONG).show();

                           }

                       }
                   });

                }

                //----------------------- Отмена заявки в друзья -------------

                if(current_state.equals("req_sent")){

                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    profileBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    profileBtn.setText("Добавить в контакты");

                                }
                            });

                        }
                    });


                }

                //------------------------------ КОГДА АККАУНТ В ДРУЗЬЯХ -----------------

                if(current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    profileBtn.setEnabled(true);
                                                    current_state = "friends";
                                                    profileBtn.setText("Удалить контакт");

                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                    });

                }

            }
        });

    }
}
