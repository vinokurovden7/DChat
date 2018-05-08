package com.den.vin.dchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout reg_disp_name;
    private TextInputLayout reg_emale;
    private TextInputLayout reg_password;

    private Button reg_create_btn;

    //Toolbar
    private Toolbar mToolbar;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Создание нового аккаунта");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Элементы экрана

        reg_disp_name = (TextInputLayout) findViewById(R.id.reg_disp_name);
        reg_emale = (TextInputLayout) findViewById(R.id.reg_emale);
        reg_password = (TextInputLayout) findViewById(R.id.reg_password);

        reg_create_btn = (Button) findViewById(R.id.reg_create_btn);

        reg_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    String display_name = reg_disp_name.getEditText().getText().toString();
                    String email = reg_emale.getEditText().getText().toString();
                    String password = reg_password.getEditText().getText().toString();

                    if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                        mRegProgress.setTitle("Регистрация пользователя");
                        mRegProgress.setMessage("Создание аккаунта, пожалуйста подождите...");
                        mRegProgress.setCanceledOnTouchOutside(false);
                        mRegProgress.show();
                        register_user(display_name, email, password);

                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {


                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status", "Социальный работник");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap);


                            mRegProgress.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                        else
                        {
                            mRegProgress.hide();
                            String ex = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Ошибка регистрации: "+ex,
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

}
