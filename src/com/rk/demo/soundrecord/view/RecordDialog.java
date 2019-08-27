package com.rk.demo.soundrecord.view;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.rk.demo.soundrecord.R;
import com.rk.demo.soundrecord.activity.MainActivity;

import java.io.File;
import java.io.IOException;

/**
 * FileName: MainActivity
 * Author: rockchip Date: 2019/8/15
 * Description:
 */

public class RecordDialog extends Dialog implements View.OnClickListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private TextView txtWarn;
    private TextView txtCount;
    private Button btnDelete;
    private Button btnPlay;
    private Button btnClose;

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private String mRecordContent;//当前选中的文本内容
    private int mRecordDuration;//当前选中的录音时长
    private String mSaveFileName;//已保持的录音文件
    private OnRecordDialogListener mListener;
    private MediaPlayer mMediaPlayer;
    private Object mLock;

    public RecordDialog(Context context, int width, int height,
                        String content, int duration,
                        OnRecordDialogListener listener) {
        super(context, R.style.LoadingView);
        mContext = context;
        mWidth = width;
        mHeight = height;
        mRecordContent = content;
        mRecordDuration = duration;
        mListener = listener;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_record);
        TextView txtContent = (TextView) findViewById(R.id.txt_dialog_context);
        txtContent.setText(mRecordContent);
        txtCount = (TextView) findViewById(R.id.txt_dialog_count);
        txtCount.setText("已录音次数: " + MainActivity.mSaveCount);
        txtWarn = (TextView) findViewById(R.id.txt_dialog_warn);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        btnClose = (Button) findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);

        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    private void playRecord() {
        if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mSaveFileName);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            txtWarn.setText("播放失败");
        }
    }

    public void setWarnText(String ss) {
        if (null != txtWarn) {
            txtWarn.setText(ss);
        }
    }

    public void recordFinish(String fileName) {
        mSaveFileName = fileName;
        btnClose.setEnabled(true);
        btnPlay.setEnabled(true);
        btnDelete.setEnabled(true);
        setWarnText(fileName);
        txtCount.setText("已录音次数: " + (MainActivity.mSaveCount + 1));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete: {
                txtWarn.setText("删除文件...");
                stopPlay();
                btnDelete.setEnabled(false);
                btnPlay.setEnabled(false);
                File file = new File(mSaveFileName);
                if (null != file && file.exists()) {
                    boolean result = file.delete();
                    Log.v("RecordDialog", "delete " + mSaveFileName + " " + result);
                    if (result) {
                        txtWarn.setText("文件已删除");
                    }
                }
                txtCount.setText("已录音次数: " + MainActivity.mSaveCount);
                break;
            }
            case R.id.btn_play: {
                txtWarn.setText("播放中...");
                btnPlay.setEnabled(false);
                File file = new File(mSaveFileName);
                if (null != file && file.exists()) {
                    playRecord();
                }
                break;
            }
            case R.id.btn_close: {
                stopPlay();
                cancel();
                if (null != mSaveFileName) {
                    File file = new File(mSaveFileName);
                    if (file.exists()) {
                        MainActivity.mSaveCount++;
                    }
                }
                break;
            }
        }
    }

    private void stopPlay() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        LayoutParams attributes = getWindow().getAttributes();
        attributes.gravity = Gravity.CENTER;
        attributes.width = mWidth;//LayoutParams.MATCH_PARENT;
        attributes.height = mHeight;//LayoutParams.MATCH_PARENT;
        //attributes.alpha = 0.85f;
        //getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(attributes);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlay();
        btnPlay.setEnabled(true);
        btnDelete.setEnabled(true);
        txtWarn.setText("播放完成");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopPlay();
        txtWarn.setText(mSaveFileName + "\n播放失败");
        btnDelete.setEnabled(true);
        return true;
    }

    public interface OnRecordDialogListener {
    }
}
