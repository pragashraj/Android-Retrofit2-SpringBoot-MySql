package com.example.imageupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {
    @Multipart
    @POST("ocr")
    Call<MyResponse> uploadAttachment(@Part MultipartBody.Part filePart);
}
