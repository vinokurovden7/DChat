package com.den.vin.dchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mPostBtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mRegProgress = new ProgressDialog(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Смена статуса аккаунта");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status);
        mPostBtn = (Button) findViewById(R.id.status_btn);

        String status_value = getIntent().getStringExtra("status_value");

        mStatus.getEditText().setText(status_value);

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRegProgress.setTitle("Сохранение изменений");
                mRegProgress.setMessage("Сохранение изменений, пожалуйста подождите...");
                mRegProgress.setCanceledOnTouchOutside(false);
                mRegProgress.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mRegProgress.dismiss();
                            /*Intent setting_intent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(setting_intent);*/
                            finish();
                        }
                        else {
                            mRegProgress.hide();
                            Toast.makeText(getApplicationContext(), "Ошибка операции сохранения", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }


}
