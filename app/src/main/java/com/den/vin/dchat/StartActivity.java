package com.den.vin.dchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = (Button) findViewById(R.id.startRegBtn);
        mLoginBtn = (Button) findViewById(R.id.startLoginBtn);

        //Переход к созданию нового аккаунта
        try {
            mRegBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                    startActivity(reg_intent);

                }
            });

            //Переход к авторизации существующего аккаунта
            mLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(login_intent);
                }
            });
        } catch (Exception e) {
            Toast.makeText(StartActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }
}
