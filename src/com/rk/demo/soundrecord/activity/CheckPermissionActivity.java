package com.rk.demo.soundrecord.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.rk.demo.soundrecord.R;

/**
 * Created by rockchip on 2017/12/5.
 */

public class CheckPermissionActivity extends Activity {
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 124;
    private static String mJumpActivityName;
    public static final String[] REQUEST_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.RECORD_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUEST_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            back2JumpActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        // Permission Denied
                        String toast_text = getResources().getString(R.string.toast_err_permission);
                        Toast.makeText(CheckPermissionActivity.this, toast_text,
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                // Permission Granted
                back2JumpActivity();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void back2JumpActivity() {
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(mJumpActivityName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }
        if (null != getIntent()) {
            intent.setAction(getIntent().getAction());
            intent.setDataAndType(getIntent().getData(), getIntent().getType());
        }
        startActivity(intent);
        finish();
    }

    public static boolean jump2PermissionActivity(Activity activity, Intent intent) {
        if (hasUnauthorizedPermission(activity)) {
            Intent newIntent = new Intent(activity, CheckPermissionActivity.class);
            if (null != intent) {
                //not action:newIntent.setAction(intent.getAction());
                newIntent.setDataAndType(intent.getData(), intent.getType());
                if (null != intent.getExtras()) {
                    newIntent.putExtras(intent.getExtras());
                }
                //TODO 第一次的权限取消，下面这行会第二次再进无法跳转，需要调试
                // newIntent.setFlags(intent.getFlags());
            }
            activity.startActivity(newIntent);
            mJumpActivityName = activity.getComponentName().getClassName();
            return true;
        }
        return false;
    }

    public static boolean hasUnauthorizedPermission(Activity activity) {
        for (String permission : REQUEST_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != activity.checkSelfPermission(permission)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }
}
