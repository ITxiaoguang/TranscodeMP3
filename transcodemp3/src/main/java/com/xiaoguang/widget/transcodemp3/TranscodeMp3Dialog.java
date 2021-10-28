package com.xiaoguang.widget.transcodemp3;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

/**
 * 转码成MP3
 * hxg 2020/9/1 17:22 qq:929842234
 */
public class TranscodeMp3Dialog extends Dialog {

    private OnCallback callback;

    public interface OnCallback {
        void success(String successPath);

        void fail(int code, String message);
    }

    private Context context;

    private TextView tv_des;
    private TextView tv_progress;
    private TextView tv_cancel;

    private MyRxFFmpegSubscriber myRxFFmpegSubscriber;
    private String inputPath;
    private String outputPath;
    private String[] ignores;

    public TranscodeMp3Dialog(@NonNull Context context) {
        super(context, R.style.dialog_default_style);
        this.context = context;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setIgnores(String[] ignores) {
        this.ignores = ignores;
    }

    public void setCallback(OnCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_media_loading);

        tv_des = findViewById(R.id.tv_des);
        tv_progress = findViewById(R.id.tv_progress);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> dismiss());
        tv_des.setText(R.string.transcoding);

        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);

        initOutputPath();

        runFFmpegRxJava();
    }

    /**
     * 默认输出路径  包名/cache/transcodeMp3/outputMp3.mp3
     */
    private void initOutputPath() {
        if (TextUtils.isEmpty(outputPath)) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName();
            mkdir(path);
            mkdir(path + "/cache");
            mkdir(path + "/cache/transcodeMp3");
            outputPath = path + "/cache/transcodeMp3" + "/outputMp3.mp3";
        }
    }

    public static void mkdir(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void cancel() {
    }

    private void runFFmpegRxJava() {
        if (inputPath.endsWith(".m3u8")) {
            callback.fail(-1, this.getContext().getResources().getString(R.string.cant_m3u8));
            dismiss();
            return;
        }
        for (String ignore : ignores) {
            if (ignore.equals(getExtensionName(inputPath))) {
                callback.success(inputPath);
                if (this.isShowing()) {
                    dismiss();
                } else {
                    new Handler().postDelayed(this::dismiss, 400);
                }
                return;
            }
        }

        String ffmpeg = "ffmpeg -y -i %s -preset superfast %s";
        String content = String.format(ffmpeg, inputPath, outputPath);

        String[] commands = content.split(" ");

        myRxFFmpegSubscriber = new MyRxFFmpegSubscriber(this);

        //开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands)
                .subscribe(myRxFFmpegSubscriber);
    }

    public static class MyRxFFmpegSubscriber extends RxFFmpegSubscriber {

        private WeakReference<TranscodeMp3Dialog> mWeakReference;

        public MyRxFFmpegSubscriber(TranscodeMp3Dialog Mp3Converter) {
            mWeakReference = new WeakReference<>(Mp3Converter);
        }

        @Override
        public void onFinish() {
            final TranscodeMp3Dialog mHomeFragment = mWeakReference.get();
            if (mHomeFragment != null) {
                mHomeFragment.progressDialog(1, mHomeFragment.getContext().getString(R.string.handle_success));
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            final TranscodeMp3Dialog mHomeFragment = mWeakReference.get();
            if (mHomeFragment != null) {
                //progressTime 可以在结合视频总时长去计算合适的进度值
                mHomeFragment.setProgressDialog(progress, progressTime);
            }
        }

        @Override
        public void onCancel() {
            final TranscodeMp3Dialog mHomeFragment = mWeakReference.get();
            if (mHomeFragment != null) {
                mHomeFragment.progressDialog(0, mHomeFragment.getContext().getString(R.string.canceled));
            }
        }

        @Override
        public void onError(String message) {
            final TranscodeMp3Dialog mHomeFragment = mWeakReference.get();
            if (mHomeFragment != null) {
                mHomeFragment.progressDialog(-1, mHomeFragment.getContext().getString(R.string.handle_error) + message);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != myRxFFmpegSubscriber) {
            myRxFFmpegSubscriber.dispose();
        }
    }

    private void progressDialog(int code, String s) {
        Log.e("TranscodeMp3Dialog   ", s);
        if (code == 1) {
            callback.success(outputPath);
            dismiss();
        } else if (code == -1) {
            callback.fail(code, this.getContext().getString(R.string.transcode_error));
            dismiss();
        } else if (code == 0) {
            callback.fail(0, this.getContext().getString(R.string.transcode_cancel));
            dismiss();
        }
    }

    private void setProgressDialog(int progress, long progressTime) {
        tv_progress.setText(progress + "%");
        Log.e("TranscodeMp3Dialog   ", "progress:" + progress + "   progressTime:" + progressTime);
    }

    // 获取文件扩展名
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

}
