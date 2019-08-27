package com.rk.demo.soundrecord;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.rk.demo.soundrecord.utils.DateUtils;
import com.rk.demo.soundrecord.utils.RecordUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * FileName: RecorderThread
 * Author: rockchip Date: 2019/8/13
 * Description:
 */

public class RecorderThread extends Thread {
    private final String TAG = "RecorderThread";
    private int mBufferSizeInBytes;
    private boolean mCancel;
    private int mSampleRateInHz;
    private int mChannelConfig;
    private int mAudioFormat;
    private AudioRecord mAudioRecord;
    private String mPersonId;
    private String mDirName;
    private String mFileName;

    public RecorderThread(int sampleRateInHz, int channelConfig,
                          int audioFormat, int bufferSizeInBytes,
                          String dirName, String personId) {
        mSampleRateInHz = sampleRateInHz;
        mChannelConfig = channelConfig;
        mAudioFormat = audioFormat;
        mBufferSizeInBytes = bufferSizeInBytes;
        mDirName = dirName;
        mPersonId = personId;
    }

    public void setCancel(boolean cancel) {
        mCancel = cancel;
    }

    @Override
    public void run() {
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes);
        File dir = new File("mnt/sdcard/rkdemorecord/" + mDirName + "/");
        if (null != dir && !dir.isDirectory()) {
            dir.mkdirs();
        }
        String fileName = mPersonId + "_nohash_" + DateUtils.getCurrentTime() + ".wav";
        File recordFile = new File(dir, fileName);
        FileOutputStream fos = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            mAudioRecord.startRecording();
            byte[] buffer = new byte[mBufferSizeInBytes];
            int bufferReadResult = 0;
            while (!mCancel) {
                bufferReadResult = mAudioRecord.read(buffer, 0,
                        mBufferSizeInBytes);

                if (bufferReadResult > 0) {
                    baos.write(buffer, 0, bufferReadResult);
                }
            }

            Log.v(TAG, "cancel recording, the file is:" + recordFile.getAbsolutePath());

            buffer = baos.toByteArray();
            fos = new FileOutputStream(recordFile);

            //int sampleRateInHz, int channelConfig,
            //                          int audioFormat, int bufferSizeInBytes
            RecordUtils.setWaveHeader(fos, mSampleRateInHz, mChannelConfig, buffer.length);
            fos.write(buffer);
            mFileName = recordFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fos = null;
            }
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
        }
    }

    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    public String getFileName() {
        return mFileName;
    }
}
