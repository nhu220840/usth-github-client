package com.usth.githubclient.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.usth.githubclient.activities.AuthenticationActivity;

import com.usth.githubclient.activities.AuthenticationActivity;
public class AuthCallbackActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forward redirect to AuthenticationActivity
        Intent forward = new Intent(this, AuthenticationActivity.class);
        forward.setData(getIntent().getData());
        forward.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(forward);

        // End up fast and not keep it in back stack (hien ra va bien mat gan nhu ngay lap tuc)
        finish();
    }
}
