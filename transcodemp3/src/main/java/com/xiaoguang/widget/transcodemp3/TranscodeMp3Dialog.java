package com.xiaoguang.widget.transcodemp3;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
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
    private String[] ignores;
    private String outputPath;

    public TranscodeMp3Dialog(@NonNull Context context) {
        super(context, R.style.dialog_default_style);
        this.context = context;
        String directory = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName() + "/cache/transcode_mp3";
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdir();
        }
        this.outputPath = directory + "/outputMp3.mp3";
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
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

        // set window params
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.color.transparent);
        WindowManager.LayoutParams params = window.getAttributes();
        int density = getWidthPixels(context);
        params.width = density - 40;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        runFFmpegRxJava();
    }

    private int getWidthPixels(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
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
        Log.e("Mp3Converter   ", s);
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
        Log.e("Mp3Converter   ", "progress:" + progress + "   progressTime:" + progressTime);
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

//    private void runFFmpegRxJava() {
//        String ffmpeg = "ffmpeg -y -i %s -preset superfast %s";
//        String content = String.format(ffmpeg, inputPath, wavPath);
//
//        String[] commands = content.split(" ");
//
//        myRxFFmpegSubscriber = new Mp3ConverterDialog.MyRxFFmpegSubscriber(this);
//
//        //开始执行FFmpeg命令
//        RxFFmpegInvoke.getInstance()
//                .runCommandRxJava(commands)
//                .subscribe(myRxFFmpegSubscriber);
//    }
//
//    public static class MyRxFFmpegSubscriber extends RxFFmpegSubscriber {
//
//        private WeakReference<Mp3ConverterDialog> mWeakReference;
//
//        public MyRxFFmpegSubscriber(Mp3ConverterDialog Mp3Converter) {
//            mWeakReference = new WeakReference<>(Mp3Converter);
//        }
//
//        @Override
//        public void onFinish() {
//            final Mp3ConverterDialog mHomeFragment = mWeakReference.get();
//            if (mHomeFragment != null) {
//                mHomeFragment.progressDialog(1, "处理成功");
//            }
//        }
//
//        @Override
//        public void onProgress(int progress, long progressTime) {
//            final Mp3ConverterDialog mHomeFragment = mWeakReference.get();
//            if (mHomeFragment != null) {
//                //progressTime 可以在结合视频总时长去计算合适的进度值
//                mHomeFragment.setProgressDialog(progress, progressTime);
//            }
//        }
//
//        @Override
//        public void onCancel() {
//            final Mp3ConverterDialog mHomeFragment = mWeakReference.get();
//            if (mHomeFragment != null) {
//                mHomeFragment.progressDialog(0, "已取消");
//            }
//        }
//
//        @Override
//        public void onError(String message) {
//            final Mp3ConverterDialog mHomeFragment = mWeakReference.get();
//            if (mHomeFragment != null) {
//                mHomeFragment.progressDialog(-1, "出错了 onError：" + message);
//            }
//        }
//    }
//
//
//    @Override
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        if (myRxFFmpegSubscriber != null) {
//            myRxFFmpegSubscriber.dispose();
//        }
//        if (handler != null) {
//            handler.removeCallbacksAndMessages(null);
//        }
//    }
//
//    private void progressDialog(int code, String s) {
//        Log.e("Mp3Converter   ", s);
//        if (code == 1) {
//            if (doing) {
//                tv_des.setText("转码中...");
//                initLame();
//            } else {
//                callback.success(outputPath);
//                dismiss();
//                Log.e("Mp3Converter   ", "转码完成");
//            }
//        } else if (code == -1) {
//            ToastHelper.showToast(BaseApp.getContext(), "转码失败了。");
//            dismiss();
//        } else {
//            ToastHelper.showToast(BaseApp.getContext(), "取消转码。");
//        }
//    }
//
//    private void initLame() {
//        MediaExtractor mex = new MediaExtractor();
//        try {
//            mex.setDataSource(inputPath);// the adresss location of the sound on sdcard.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        MediaFormat mf = mex.getTrackFormat(0);
//        int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE) / 1000;//148    mode = 0  29:30   mode = 1  24:06  mode = 2  29:30
//        int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);//48000
//        int channelCount = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT);//2
//
//        Mp3Converter.init(sampleRate, channelCount, 0, sampleRate, bitRate, 7);
//        fileSize = new File(wavPath).length();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Mp3Converter.convertMp3(wavPath, outputTempPath);
//            }
//        }).start();
//
//        handler.postDelayed(runnable, 500);
//    }
//
//    private long fileSize;
//    Handler handler = new Handler();
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            long bytes = Mp3Converter.getConvertBytes();
//            int progress = (int) (100f * bytes / fileSize);
//            if (bytes == -1) {
//                progress = 100;
//            }
//            Log.e("Mp3Converter", "convert progress: " + progress);
//
//            if (handler != null && progress < 100) {
//                tv_progress.setText(progress + "%");
//                handler.postDelayed(this, 1000);
//            } else if (100 == progress) {
//                cut();
//            }
//        }
//    };
//
//    /**
//     * 裁剪
//     */
//    private void cut() {
//        tv_des.setText("优化中...");
//        doing = false;
//        float duration = 0F;
//        try {
//            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//            mmr.setDataSource(inputPath);
//            duration = Float.parseFloat(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String ffmpeg1 = "ffmpeg -y -i %s -vn -acodec copy -ss 0 -t %f %s";
//        String content = String.format(ffmpeg1, outputTempPath, duration, outputPath);
//
//        String[] commands = content.split(" ");
//
//        myRxFFmpegSubscriber = new Mp3ConverterDialog.MyRxFFmpegSubscriber(this);
//
//        //开始执行FFmpeg命令
//        RxFFmpegInvoke.getInstance()
//                .runCommandRxJava(commands)
//                .subscribe(myRxFFmpegSubscriber);
//    }
//
//    private void setProgressDialog(int progress, long progressTime) {
//        tv_progress.setText(progress + "%");
//        Log.e("Mp3Converter   ", "progress:" + progress + "   progressTime:" + progressTime);
//    }
}
