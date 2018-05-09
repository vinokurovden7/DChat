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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;

    private Button mLogin_btn;

    private FirebaseAuth mAuth;

    //ProgressDialog
    private ProgressDialog mLoginProgress;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Вход в аккаунт");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mLoginProgress = new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginEmail = (TextInputLayout) findViewById(R.id.login_emale);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);

        mLogin_btn = (Button) findViewById(R.id.login_btn);

        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String email = mLoginEmail.getEditText().getText().toString();
                    String password = mLoginPassword.getEditText().getText().toString();

                    if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                        mLoginProgress.setTitle("Авторизация пользователя");
                        mLoginProgress.setMessage("Проверка учетных данных, пожалуйста подождите...");
                        mLoginProgress.setCanceledOnTouchOutside(false);
                        mLoginProgress.show();
                        loginUser(email, password);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mLoginProgress.dismiss();

                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                } else {
                    mLoginProgress.hide();
                    String ex = task.getException().toString();
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Ошибка авторизации. "+ex,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
