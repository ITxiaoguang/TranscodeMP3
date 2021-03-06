# TranscodeMP3
# 一款好用的音频转码成MP3格式的工具。
[![](https://www.jitpack.io/v/ITxiaoguang/TranscodeMP3.svg)](https://www.jitpack.io/#ITxiaoguang/TranscodeMP3)

### 用FFmpeg实现转码。

- 支持音频格式转换成mp3格式的工具。
- 支持自定义输出路径。


### 自带`TranscodeMp3Dialog`弹窗，用法超级简单。

## 如何添加
### Gradle添加：
#### 1.在Project的`build.gradle`中添加仓库地址

``` gradle
allprojects {
  repositories {
     ...
     maven { url "https://jitpack.io" }
  }
}
```

#### 2.在Module目录下的`build.gradle`中添加依赖


[![](https://www.jitpack.io/v/ITxiaoguang/TranscodeMP3.svg)](https://www.jitpack.io/#ITxiaoguang/TranscodeMP3)

``` gradle
dependencies {
       implementation 'com.github.ITxiaoguang:TranscodeMP3:xxx'
}
```

#### 3.Android10.0 需要在`AndroidManifest.xml`文件中`application`出加上
```xml
<application
  android:requestLegacyExternalStorage="true"
  ...
  />
```

#### 4.`AndroidManifest.xml`加上读写权限，代码里并请求读写权限
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

自带`TranscodeMp3Dialog`弹窗，用法超级简单：

```java
// String inputPath = "输入你需要转码的音频地址";
// String cameraPath = Environment.getExternalStorageDirectory().getPath() + "/transcodeMp3/";
// TranscodeMp3Dialog.mkdir(cameraPath);
// String outputPath = cameraPath + System.currentTimeMillis() + ".mp3";

String[] ignores = {"mp3"};//mp3 不转码
TranscodeMp3Dialog dialog = new TranscodeMp3Dialog(this);
dialog.setInputPath(inputPath);
// dialog.setOutputPath(outputPath);// 你想导出的地址 默认输出路径：包名/cache/transcodeMp3/outputMp3.mp3
dialog.setIgnores(ignores);
dialog.setCallback(new TranscodeMp3Dialog.OnCallback() {
    @Override
    public void success(String successPath) {
        Toast.makeText(MainActivity.this, "成功，path： " + successPath, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void fail(int code, String message) {
        Toast.makeText(MainActivity.this, "失败，message： " + message, Toast.LENGTH_SHORT).show();
    }
});
dialog.show();
```


更多ffmpeg命令转码，请看：[RxFFmpeg](https://github.com/microshow/RxFFmpeg)


# 选择文件

## 推荐使用文件选择器选择文件 --> [文件选择器](https://github.com/ITxiaoguang/FilePicker)

#### 1.选择文件
```java
private void filePicker() {
    String[] zips = {"zip", "rar"};
    String[] doc = {"doc", "docx"};
    String[] ppt = {"ppt", "pptx"};
    String[] pdf = {"pdf"};
    String[] txt = {"txt"};
    String[] apk = {"apk"};
    String[] xls = {"xls", "xlsx"};
    String[] music = {"m3u", "m4a", "m4b", "m4p", "ogg", "wma", "wmv", "ogg", "rmvb", "mp2", "mp3", "aac", "awb", "amr", "mka"};
    FilePickerBuilder.getInstance()
            .setMaxCount(1)
            .enableCameraSupport(false)
            .showPic(true)
            .showVideo(true)
            .enableDocSupport(false)
            .addFileSupport("Word", doc, R.drawable.ic_file_word)
            .addFileSupport("压缩包", zips, R.drawable.ic_file_zip)
            .addFileSupport("PDF", pdf, R.drawable.ic_file_pdf)
            .addFileSupport("Txt文本", txt, R.drawable.ic_file_txt)
            .addFileSupport("PPT", ppt, R.drawable.ic_file_ppt)
            .addFileSupport("安装包", apk, R.drawable.ic_file_zip)
            .addFileSupport("Excel表格", xls, R.drawable.ic_file_excel)
            .addFileSupport("音乐", music, R.drawable.ic_file_music)
            .setActivityTitle("请选择文件")
            .sortDocumentsBy(SortingTypes.name)
            .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .pickFile(this, REQUEST_CODE_FILE);
}
```

#### 2.回调
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
        if (requestCode == REQUEST_CODE_FILE) {//选择文件
            ArrayList<String> filePaths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
            String path = filePaths.get(0);
//                Toast.makeText(this, "地址： " + path, Toast.LENGTH_SHORT).show();
            // doto 得到文件地址，进行转码
        }
    }
}
```

