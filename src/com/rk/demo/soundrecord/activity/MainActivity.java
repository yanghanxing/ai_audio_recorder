package com.rk.demo.soundrecord.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rk.demo.soundrecord.R;
import com.rk.demo.soundrecord.RecorderThread;
import com.rk.demo.soundrecord.view.RecordDialog;

import java.util.Random;

/**
 * FileName: MainActivity
 * Author: rockchip Date: 2019/8/13
 * Description:
 */

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final int MSG_COUNT_DOWN = 0x00001;
    private final int TIME_MIN_RECORD_DURATION = 3;//s
    private final int TIME_DEFAULT_SAVE = 1;//s
    private final int DEFAULT_ITEM = 5;
    private final int MAX_RANDOM_SEED = 1000000;//随机数

    private LinearLayout layoutMain;
    private Button btnChange;
    private Button btnClear;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mCountDown;
    private boolean mStart;
    private int mSampleRateInHz = 16000;//44100;
    private int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSizeInBytes;
    private RecordDialog mRecordDialog;
    private RecorderThread mRecorderThread;
    private String mSaveFileName;
    private STEP mStep;
    private String mCurrentContent;//当前选中的文本内容
    private int mCurrentDuration;//当前选中的录音时长
    private String mCurrentSaveDir;//当前选中的文件夹名称
    private String mCurrentPersonId;
    public static int mSaveCount;

    private enum STEP {
        RECORDING,
        SAVE
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COUNT_DOWN: {
                    if (!mStart) {
                        return;
                    }
                    if (mCountDown < 0) {//倒计时完成
                        if (STEP.RECORDING == mStep) {//进入保存
                            if (null != mRecordDialog) {
                                mRecordDialog.setWarnText("保存中...");
                            }
                            stopRecord();
                            mStep = STEP.SAVE;
                            mCountDown = TIME_DEFAULT_SAVE;
                            mHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                        } else if (STEP.SAVE == mStep) {//保存完成
                            if (null != mRecorderThread) {
                                mSaveFileName = mRecorderThread.getFileName();
                            }
                            if (TextUtils.isEmpty(mSaveFileName)) {
                                Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                                if (null != mRecordDialog) {
                                    mRecordDialog.cancel();
                                    mRecordDialog = null;
                                }
                            } else {
                                Toast.makeText(MainActivity.this, mSaveFileName, Toast.LENGTH_SHORT).show();
                                if (null != mRecordDialog) {
                                    mRecordDialog.recordFinish(mSaveFileName);
                                }
                            }
                            mStep = STEP.RECORDING;
                        }
                    } else {
                        if (STEP.RECORDING == mStep) {
                            if (null != mRecordDialog) {
                                mRecordDialog.setWarnText(mCountDown + " 秒");
                            }
                        } else if (STEP.SAVE == mStep) {
                            //txtWarn.setVisibility(View.VISIBLE);
                        }
                        mCountDown--;
                        mHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;

        mCurrentPersonId = getPersonId();
        mSaveCount = 0;

        layoutMain = (LinearLayout) findViewById(R.id.layout_activity_main);
        for (int i = 0; i < DEFAULT_ITEM; i++) {
            LayoutInflater li = LayoutInflater.from(this);
            View recordView = li.inflate(R.layout.layout_record_item, null);
            layoutMain.addView(recordView);
            EditText etDuration = (EditText) recordView.findViewById(R.id.et_duration);
            etDuration.setText(getDurationWithId(i));

            EditText etSaveDir = (EditText) recordView.findViewById(R.id.et_save_dir);
            etSaveDir.setText(getSaveDirWithId(i));

            EditText etContent = (EditText) recordView.findViewById(R.id.et_content);
            etContent.setText(getContentWithId(i));

            Button btnStart = (Button) recordView.findViewById(R.id.btn_start);
            btnStart.setTag(i);
            btnStart.setOnClickListener(this);
        }
        btnChange = (Button) findViewById(R.id.btn_change);
        btnChange.setOnClickListener(this);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);
    }

    private void startRecord() {
        if (null != mRecorderThread) {
            if (null != mRecorderThread.getAudioRecord()) {
                mRecorderThread.getAudioRecord().stop();
            }
            mRecorderThread.setCancel(true);
        }
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz,
                mChannelConfig, mAudioFormat);
        mRecorderThread = new RecorderThread(mSampleRateInHz,
                mChannelConfig, mAudioFormat,
                mBufferSizeInBytes, mCurrentSaveDir, mCurrentPersonId);
        mRecorderThread.start();
        Log.v(TAG, "mSampleRateInHz=" + mSampleRateInHz + ", mChannelConfig="
                + mChannelConfig + ", mAudioFormat=" + mAudioFormat
                + ", mBufferSizeInBytes=" + mBufferSizeInBytes);
    }

    private void stopRecord() {
        if (null != mRecorderThread) {
            if (null != mRecorderThread.getAudioRecord()) {
                mRecorderThread.getAudioRecord().stop();
            }
            mRecorderThread.setCancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStart = false;
        stopRecord();

        int count = layoutMain.getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = layoutMain.getChildAt(i);
            EditText etDuration = (EditText) childView.findViewById(R.id.et_duration);
            setDurationWithId(i, etDuration.getText().toString());

            EditText etSaveDir = (EditText) childView.findViewById(R.id.et_save_dir);
            setSaveDirWithId(i, etSaveDir.getText().toString());

            EditText etContent = (EditText) childView.findViewById(R.id.et_content);
            setContentWithId(i, etContent.getText().toString());
        }

        mHandler.removeMessages(MSG_COUNT_DOWN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start: {
                int position = Integer.parseInt(v.getTag().toString());
                logV("start position=" + position);
                if (!isInputLegal(position)) {
                    return;
                }
                if (null != mRecordDialog) {
                    mRecordDialog.cancel();
                }
                mRecordDialog = new RecordDialog(
                        this, mScreenWidth * 2 / 3, mScreenHeight * 2 / 3,
                        mCurrentContent, mCurrentDuration,
                        null);
                mRecordDialog.show();
                startRecord();
                mStart = true;
                mStep = STEP.RECORDING;
                mCountDown = mCurrentDuration;
                mHandler.sendEmptyMessage(MSG_COUNT_DOWN);
                break;
            }
            case R.id.btn_change: {
                Log.v(TAG, "change people");
                //++mCurrentPersonId;
                mSaveCount = 0;
                mCurrentPersonId = String.valueOf((long) ((Math.random() + 1) * MAX_RANDOM_SEED));
                setPersonId(mCurrentPersonId);
                break;
            }
            case R.id.btn_clear: {
                int count = layoutMain.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childView = layoutMain.getChildAt(i);
                    setContentWithId(i, "");
                    setDurationWithId(i, "");
                    setSaveDirWithId(i, "");
                    EditText etDuration = (EditText) childView.findViewById(R.id.et_duration);
                    etDuration.setText(getDurationWithId(i));

                    EditText etSaveDir = (EditText) childView.findViewById(R.id.et_save_dir);
                    etSaveDir.setText(getSaveDirWithId(i));

                    EditText etContent = (EditText) childView.findViewById(R.id.et_content);
                    etContent.setText(getContentWithId(i));
                }
                break;
            }
            default:
                break;
        }
    }
//
//    public void testButton(View view) {
//        Log.e(TAG, "testButton: ");
//        // throw new NullPointerException();
//        //multicastTest.sendMulticast();
//        Intent intent = new Intent();
//
//        intent.setComponent(new ComponentName("com.android.settings",
//                "com.android.settings.RebootReceiver"));
//
//
//        intent.setAction("com.intent.action.WX_OTG_CONFIG");
//        intent.putExtra("test", "Android ");
//        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//        //intent.setData(Uri.parse("otg_function"));
//
//        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
//
//    }

    private boolean isInputLegal(int position) {
        mCurrentDuration = -1;
        mCurrentSaveDir = "";
        mCurrentContent = "";
        View view = layoutMain.getChildAt(position);
        //获取录音时长
        EditText etDuration = (EditText) view.findViewById(R.id.et_duration);
        if (null != etDuration.getText()) {
            String value = etDuration.getText().toString();
            if (TextUtils.isEmpty(value)) {
                showToast("未输入录音时长");
                return false;
            }
            int tempValue = Integer.parseInt(value);
            if (tempValue < TIME_MIN_RECORD_DURATION) {
                showToast("录音时长不得少于 " + TIME_MIN_RECORD_DURATION + " 秒");
                return false;
            }
            mCurrentDuration = tempValue;
        }

        //获取录音文件夹
        EditText etSaveDir = (EditText) view.findViewById(R.id.et_save_dir);
        if (null != etSaveDir.getText()) {
            String value = etSaveDir.getText().toString();
            if (TextUtils.isEmpty(value)) {
                showToast("未输入要保存的文件夹名称");
                return false;
            }
            mCurrentSaveDir = value;
        }

        //获取录音的朗读文本
        EditText etContent = (EditText) view.findViewById(R.id.et_content);
        if (null != etContent.getText()) {
            String value = etContent.getText().toString();
            if (TextUtils.isEmpty(value)) {
                showToast("未输入朗读文本内容");
                return false;
            }
            mCurrentContent = value;
        }

        if (mCurrentDuration < 0 || TextUtils.isEmpty(mCurrentSaveDir)
                || TextUtils.isEmpty(mCurrentContent)) {
            showToast("输入不合法");
            return false;
        }
        return true;
    }

    private String getPersonId() {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        //return sf.getString("person_id", String.valueOf(MAX_RANDOM_SEED));
        return sf.getString("person_id", String.valueOf((long) ((Math.random() + 1) * MAX_RANDOM_SEED)));
    }

    private void setPersonId(String id) {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        sf.edit().putString("person_id", id).commit();
    }

    private String getContentWithId(int position) {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        return sf.getString("content_" + position, "");
    }

    private void setContentWithId(int position, String value) {
        if (null == value) {
            value = "";
        }
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        sf.edit().putString("content_" + position, value).commit();
    }

    private String getSaveDirWithId(int position) {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        return sf.getString("savedir_" + position, "");
    }

    private void setSaveDirWithId(int position, String value) {
        if (null == value) {
            value = "";
        }
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        sf.edit().putString("savedir_" + position, value).commit();
    }

    private String getDurationWithId(int position) {
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        return sf.getString("duration_" + position, "");
    }

    private void setDurationWithId(int position, String value) {
        if (null == value) {
            value = "";
        }
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        sf.edit().putString("duration_" + position, value).commit();
    }

    private void logV(String ss) {
        Log.v(TAG, ss);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}