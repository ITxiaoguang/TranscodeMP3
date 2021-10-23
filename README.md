# TranscodeMP3
一款好用的转码成MP3格式的工具。

用FFmpeg实现转码。

支持音频格式转换成mp3格式的工具（m3u8除外）。


自带TranscodeMp3Dialog弹窗，用法：

```java
String inputFilePath = "输入你需要转码的音频地址";
String[] ignores = {"mp3"};// mp3 不转码
TranscodeMp3Dialog dialog = new TranscodeMp3Dialog(this);
dialog.setInputPath(inputFilePath);// 输入的文件地址
dialog.setIgnores(ignores);// 忽略的格式
dialog.setCallback(new TranscodeMp3Dialog.OnCallback() {
    // 转码成功  successPath：转码成功后地址
    @Override
    public void success(String successPath) {
        Toast.makeText(MainActivity.this, "成功，path： " + successPath, Toast.LENGTH_SHORT).show();
    }

    // 转码失败  code：失败代码 message：失败描述
    @Override
    public void fail(int code, String message) {
        Toast.makeText(MainActivity.this, "失败，message： " + message, Toast.LENGTH_SHORT).show();
    }
});
dialog.show();
```


支持更多ffmpeg命令转码，请看：[RxFFmpeg](https://github.com/microshow/RxFFmpeg)

