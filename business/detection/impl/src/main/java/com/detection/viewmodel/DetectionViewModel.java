package com.detection.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.ChatRequest;
import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.common.base.BaseViewModel;
import com.common.utils.FileUtils;
import com.common.utils.LogUtils;
import com.common.utils.SingleLiveEvent;
import com.detection.data.Repository;
import com.detection.ui.page.DetectionActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class DetectionViewModel extends BaseViewModel {
    private final SingleLiveEvent<Boolean> loadingState = new SingleLiveEvent<>();
    private final SingleLiveEvent<File> photoCaptured = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> photoUriResult = new SingleLiveEvent<>();
    private final MutableLiveData<String> chatResult = new MutableLiveData<>();
    private final SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    private final SingleLiveEvent<ResultListAgentChatHistory> historyResult = new SingleLiveEvent<>();

    private final Repository repository = new Repository();

    public void takePhoto(DetectionActivity detectionActivity, ImageCapture imageCapture) {
        loadingState.setValue(true);

        File photoFile = new File(
                detectionActivity.getCacheDir(),
                "detection_" + System.currentTimeMillis() + ".jpg"
        );
        LogUtils.INSTANCE.d("ljx",photoFile +"");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(detectionActivity),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        photoCaptured.setValue(photoFile);
                        LogUtils.INSTANCE.d("ljx", "onimagesaved");
                        File file = compressImage(photoFile);
                        if(file == null){
                            file = photoFile;
                        }
                        LogUtils.INSTANCE.d("ljx",file +"");
                        uploadAndRecognize(detectionActivity.getApplicationContext(), photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        LogUtils.INSTANCE.d("ljx", exception.toString());
                        exception.printStackTrace();
                        loadingState.setValue(false);
                    }
                });
    }
    public void fetchHistory() {
        addDisposable(
                repository.getchatHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        historyResult.setValue(response);

                                    } else {
                                        errorMessage.setValue("获取历史记录失败");
                                    }
                                },
                                error -> {
                                    LogUtils.INSTANCE.e("ljx", error);
                                    errorMessage.setValue("网络错误，请重试");
                                }
                        ));
    }

    public SingleLiveEvent<ResultListAgentChatHistory> getHistoryLiveData() {
        return historyResult;
    }

    public SingleLiveEvent<String> getErrorLiveData() {
        return errorMessage;
    }
    //压缩图片
    private File compressImage(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            if (width <= 0 || height <= 0) {
                return null;
            }
            // 计算采样率，避免OOM
            int inSampleSize = 1;
            int maxSize = 1024;
            if (width > maxSize || height > maxSize) {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                while ((halfWidth / inSampleSize) >= maxSize && (halfHeight / inSampleSize) >= maxSize) {
                    inSampleSize *= 2;
                }
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (bitmap == null) {
                return null;
            }
            int targetWidth = width;
            int targetHeight = height;
            if (width > maxSize || height > maxSize) {
                float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                targetWidth = (int) (width * ratio);
                targetHeight = (int) (height * ratio);
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
            FileOutputStream fos = new FileOutputStream(file);
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            bitmap.recycle();
            if (scaled != bitmap) {
                scaled.recycle();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void uploadAndRecognizeFromGallery(Context context, File file) {
        loadingState.setValue(true);
        File compressedFile = compressImage(file);
        if (compressedFile == null) {
            compressedFile = file;
        }
        uploadAndRecognize(context, compressedFile);
    }

    private void uploadAndRecognize(Context context, File photoFile) {
        RequestBody requestBody = RequestBody.create(
                MediaType.parse(FileUtils.INSTANCE.getMimeType(photoFile)), photoFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", photoFile.getName(), requestBody);

        addDisposable(
                repository.uploadAvatar(part, "uploads/demo/")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        String imageUrl = response.getData().toString();
                                        photoUriResult.setValue(imageUrl);
                                        LogUtils.INSTANCE.d("ljx", "imageok");
                                        recognize(imageUrl);
                                    } else {
                                        loadingState.setValue(false);
                                        LogUtils.INSTANCE.d("ljx", "imagenotok");
                                    }
                                },
                                error -> {
                                    loadingState.setValue(false);
                                    LogUtils.INSTANCE.e("ljx", error);

                                    errorMessage.setValue("图片上传失败，请重试");
                                }
                        ));
    }

    private void recognize(String imageUrl) {
        ChatRequest chatRequest = new ChatRequest("这个叶子得了什么病？", imageUrl, null, null);
        addDisposable(
                repository.getchat(chatRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        chatResult.setValue(response.getData().toString());
                                        LogUtils.INSTANCE.d("ljx", "aiok");
                                    }
                                    loadingState.setValue(false);
                                },
                                error -> {
                                    loadingState.setValue(false);
                                    LogUtils.INSTANCE.e("ljx", error);
                                    errorMessage.setValue("AI 识别失败，请重试");
                                }
                        ));
    }

    public SingleLiveEvent<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getChatResult() {
        return chatResult;
    }

    public SingleLiveEvent<String> getPhotoUriResult() {
        return photoUriResult;
    }

    public SingleLiveEvent<File> getPhotoCaptured() {
        return photoCaptured;
    }

    public MutableLiveData<Boolean> getLoadingState() {
        return loadingState;
    }

 
}
