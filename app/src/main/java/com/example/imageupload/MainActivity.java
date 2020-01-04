package com.example.imageupload;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.core.content.FileProvider.getUriForFile;
public class MainActivity extends AppCompatActivity {

    ImageView imageview;
    public int CAMERA_STORAGE_REQUEST_CODE=1;
    public int ONLY_CAMERA_REQUEST_CODE=2;
    public int ONLY_STORAGE_REQUEST_CODE=3;
    public int GALLERY_REQUEST_CODE=4;
    public int CAMERA_REQUEST_CODE=5;
    private static CharSequence[] selectOptions={"Select photo","Capture a photo","Cancel"};
    private String currentPhotoPath = "";
    Uri destinationUri;
    Uri uri;
    Uri imageUri;
    File files;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview=(ImageView)findViewById(R.id.imageView);
    }

    public void onBtnClick(View v)
    {
        permissionChecking();
    }
    private void permissionChecking() {
        if(Build.VERSION.SDK_INT>=23)
        {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_STORAGE_REQUEST_CODE);

            } else if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ONLY_CAMERA_REQUEST_CODE);

            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ONLY_STORAGE_REQUEST_CODE);

            }
        }
        showImagePickerDialog();

    }
    private void showImagePickerDialog()
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setItems(selectOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position){
                    case 0:
                        selectPhotoFromGallery();
                        break;
                    case 1:
                        capturePhoto();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });
        AlertDialog dialog1=alertDialog.create();
        dialog1.show();
    }


    private void capturePhoto() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        files = new File(Environment.getExternalStorageDirectory(), "/your_app_Name/Images" + "/photo_" + timeStamp + ".jpg");
        imageUri = Uri.fromFile(files);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file;
        try {
            file = getImageFile();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (Build.VERSION.SDK_INT >= 23)
            uri = getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
        else
            uri = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,CAMERA_REQUEST_CODE);
    }

    private void selectPhotoFromGallery() {
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"),GALLERY_REQUEST_CODE);
    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = Uri.parse(currentPhotoPath);
            imageview.setImageURI(uri);
            try{
                File file=new File(uri.getPath());
                uploadImage(file);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        } else if (requestCode ==GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri sourceUri = data.getData();
                File file = getImageFile();
                destinationUri = Uri.fromFile(file);
                imageview.setImageURI(sourceUri);
                try{
                    uploadImage(file);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Please select another image",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadImage(File file)
    {
        Api api=ApiClient.getApiClient().create(Api.class);
        MultipartBody.Part filepart=MultipartBody.Part.createFormData("image",file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        Call<MyResponse>call=api.uploadAttachment(filepart);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });

    }
}
