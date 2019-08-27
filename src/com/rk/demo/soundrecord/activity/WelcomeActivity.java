package com.rk.demo.soundrecord.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.rk.demo.soundrecord.R;

/**
 * FileName: WelcomeActivity
 * Author: rockchip Date: 2019/8/13
 * Description:
 */

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (CheckPermissionActivity.jump2PermissionActivity(this, getIntent())) {
                finish();
                return;
            }
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}