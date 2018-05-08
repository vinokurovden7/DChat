package com.den.vin.dchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private TextView profile_name;
    private TextView profile_status;

    private Button profile_status_btn;
    private Button profile_avatar_btn;

    private static final int GALLERY_PIC = 1;

    //Storage Firebase
    private StorageReference mImageStorage;

    private CircleImageView mImage;

    //ProgressDialog
    private ProgressDialog mRegProgress;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mRegProgress = new ProgressDialog(this);

        mRegProgress.setTitle("Профиль");
        mRegProgress.setMessage("Загрузка данных профиля, пожалуйста подождите...");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_status = (TextView) findViewById(R.id.profile_status);
        mImage = (CircleImageView) findViewById(R.id.settings_image);
        profile_avatar_btn = (Button) findViewById(R.id.profile_avatar_btn);
        profile_status_btn = (Button) findViewById(R.id.profile_status_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        profile_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = profile_status.getText().toString();

                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);

            }
        });

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                profile_name.setText(name);
                profile_status.setText(status);
                Picasso.with(SettingsActivity.this).load(image).into(mImage);


                mRegProgress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profile_avatar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galerryIntent = new Intent();
                galerryIntent.setType("image/*");
                galerryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galerryIntent, "Выберите фотографию"), GALLERY_PIC);

                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PIC && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);

        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);

                mProgressDialog.setTitle("Загрузка фотографии");
                mProgressDialog.setMessage("Загрузка фотографии профиля, пожалуйста подождите...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference filepath = mImageStorage.child("profile_images").child(mCurrentUser.getEmail()).child("profile_image.jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mProgressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Файл успешно загружен!", Toast.LENGTH_LONG).show();
                                        Picasso.with(SettingsActivity.this).load(download_url).into(mImage);

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Ошибка загрузки файла: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        } else {

                            Toast.makeText(getApplicationContext(), "Ошибка загрузки файла: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();

            }
        }

    }
}
