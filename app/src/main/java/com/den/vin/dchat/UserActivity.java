package com.den.vin.dchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;


public class UserActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUserlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Все пользователи");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserlist = (RecyclerView) findViewById(R.id.users_list);

    }
}
