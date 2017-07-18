package com.single.photopick;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

/**
 * Created by chenhailin on 2017/7/17.
 */

public class ImagePickActivity extends AppCompatActivity {

    static ImagePickActivity instance;
    static final int SELECT_BY_CAMERA = 1;//拍照
    static final int SELECT_BY_PICTURE_LIST = 2;//从相册选择图片
    static final int CUT_IMAGE = 3;//裁剪图片
    static String imageFilePath;
    private SetPhotoDialog mSetPhotoDialog;
    //    private String uploadUrl="http://172.16.227.16:8181/wangxingtong-web/api/image/upload";
    public static final int PICK_REQUESTCODE = 0X16;
    public String tempDir = "";
    public String tempFile = "";
    public static  final  String PATH = "PATH";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window=getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        wl.alpha=0.0f;
        window.setAttributes(wl);
        setContentView(R.layout.image_pick_layout);
        instance = this;
        tempDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempCahe";
        tempFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempCahe" + File.separator + "corp.jpg";
        mSetPhotoDialog = new SetPhotoDialog(this);
        mSetPhotoDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSetPhotoDialog.hide();
        if (requestCode == SELECT_BY_PICTURE_LIST && resultCode == Activity.RESULT_OK) {
            /**
             * 当选择的图片不为空的话，在获取到图片的途径
             */
            Uri uri = data.getData();
            try {
                String[] pojo = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, pojo, null, null, null);
                if (cursor != null) {
                    ContentResolver cr = this.getContentResolver();
                    int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(colunm_index);
                    /***
                     * 这里加这样一个判断主要是为了第三方的软件选择，比如：使用第三方的文件管理器的话，你选择的文件就不一定是图片了，
                     * 这样的话，我们判断文件的后缀名 如果是图片格式的话，那么才可以
                     */
                    if (path.endsWith("jpg") || path.endsWith("png")) {
                        startPhotoZoom(Uri.fromFile(new File(path)));
                    } else {
                        ToastUtil.showToastShort(this, "请选择图片");
                        this.finish();
                    }
                } else {
                    ToastUtil.showToastShort(this, "请选择图片");
                    this.finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.finish();
            }
        } else if (requestCode == SELECT_BY_CAMERA) {
            toZoomPic();
        } else if (requestCode == CUT_IMAGE && resultCode == Activity.RESULT_OK) {
            File file = new File(tempFile);
            if (file != null && file.isFile()) {
                Intent intent = new Intent();
                intent.putExtra(PATH, tempFile);
                setResult(Activity.RESULT_OK, intent);
                this.finish();
            } else {
                ToastUtil.showToastShort(this, "图片裁剪异常");
            }
        } else {
            this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toZoomPic() {
        File files=new File(imageFilePath);
        Bitmap bmp=BitmapFactory.decodeFile(imageFilePath);
        String uri = imageFilePath;
        Uri fileUri =UploadUtil.getImageContentUri(this,new File(uri));
        if (uri != null) {
            File file = new File(
                    tempDir);
            if (!file.isDirectory())
                file.mkdirs();
            file = new File(
                    tempFile);
            Uri uricrop = Uri.fromFile(file);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(fileUri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 132);
            intent.putExtra("outputY", 132);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uricrop);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uricrop);
            intent.putExtra("output", uricrop);
            startActivityForResult(intent, CUT_IMAGE);
        }
    }

    public  void openCamera() {
        //獲取系統版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        Uri uri;
          File  tempFile = new File(imageFilePath);
            if (currentapiVersion < 24) {
                // 从文件中创建uri
                 uri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } else {
                //兼容android7.0 使用共享文件的形式
                uri = FileProvider.getUriForFile(this, "mini.fileprovider", tempFile);//通过FileProvider创建一个content类型的Uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
        this.startActivityForResult(intent, SELECT_BY_CAMERA);
    }
    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        if (uri != null) {
            File file = new File(
                    tempDir);
            if (!file.isDirectory())
                file.mkdirs();
            file = new File(
                    tempFile);
            Uri uricrop = Uri.fromFile(file);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 132);
            intent.putExtra("outputY", 132);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uricrop);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uricrop);
            intent.putExtra("output", uricrop);
            startActivityForResult(intent, CUT_IMAGE);
        }
    }

}
