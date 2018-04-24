package tw.org.iii.fileproviderforphoto;

import android.content.Intent;
import android.drm.DrmStore;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private PhotoView photoView;
    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int IMAGE_REQUEST_CODE = 2;
    static final int SELECT_PIC_NOUGAT = 3;
    private String mCurrentPhotoPath;
    private Uri photoURI;
    private Uri photoOutputUri = null; // 图片最终的输出文件的 Uri
    private static final int CROP_PHOTO_REQUEST_CODE = 5; // 裁剪图片返回的 requestCode
    private File photoFile;
    private int screenWidth;
    String path = Environment.getExternalStorageDirectory()+"/Android/data/tw.org.iii.fileproviderforphoto/files/Pictures";
    File mCameraFile = new File(path, "IMAGE_FILE_NAME.jpg");//照相机的File对象
    File mCropFile = new File(path, "PHOTO_FILE_NAME.jpg");//裁剪后的File对象
    File mGalleryFile = new File(path, "IMAGE_GALLERY_NAME.jpg");//相册的File对象
    private Uri uriForFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoView = findViewById(R.id.image);


    }


    public void takePhoto(View view) {
        dispatchTakePictureIntent();
    }

    public void getPhotoFromAlbum(View view) {
        getPhoto();
    }

    private void getPhoto(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果大于等于7.0使用FileProvider
            uriForFile = FileProvider.getUriForFile
                    (this, "tw.org.iii.fileproviderforphoto", mGalleryFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, SELECT_PIC_NOUGAT);
        } else {
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGalleryFile));
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        // 確保有相機來處理intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                        MainActivity.this,
                        "tw.org.iii.fileproviderforphoto",
                        photoFile);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(Intent.createChooser(takePictureIntent, "TakePhoto"), REQUEST_TAKE_PHOTO);
                Log.v("brad", "photoURI = " + photoURI);

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        // timeStamp的格式-> 20180420_004839
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /**  getExternalFilesDir() 需要給的是type參數
         *   傳回的是該app packagename 底下,參數的系統位置
         *  storage/emulated/0/Android/data/tw.org.iii.takepicturetest/files/Pictures
         */
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.v("brad", "image = " + image.getAbsolutePath());
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            //拍照完返回
            case REQUEST_TAKE_PHOTO:
                Log.v("brad", "onActivityResult:photoFile = " + photoFile.getAbsolutePath());
                Uri dataUri = FileProvider.getUriForFile
                        (this, "tw.org.iii.fileproviderforphoto", photoFile);
                // Uri dataUri = getImageContentUri(data.getData());
                photoView.enable();
                Glide
                        .with(this)
                        .load(dataUri)
                        .into(photoView);
                break;

            case IMAGE_REQUEST_CODE:

                break;

            case SELECT_PIC_NOUGAT:
                Uri uri = data.getData();
                photoView.enable();
                Glide
                        .with(this)
                        .load(uri)
                        .into(photoView);
                break;
        }
    }
}
