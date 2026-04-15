package com.detection.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.ChatRequest;
import com.agri.pest.client.model.response.AgentChatHistory;
import com.agri.pest.client.model.response.DiagnosisItem;
import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.agri.pest.client.model.response.ResultListDiagnosisItem;
import com.common.base.BaseViewModel;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.storage.database.DetectionRecord;
import com.common.utils.AvatarUtils;
import com.common.utils.FileUtils;
import com.common.utils.LogUtils;
import com.common.utils.SingleLiveEvent;
import com.common.utils.ThreadUtils;
import com.detection.Utils;
import com.detection.data.Repository;
import com.detection.data.UserLocalDataSource;
import com.detection.ui.page.DetectionActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DetectionViewModel extends BaseViewModel {
    private final SingleLiveEvent<Boolean> loadingState = new SingleLiveEvent<>();
    private final SingleLiveEvent<File> photoCaptured = new SingleLiveEvent<>();
    private final MutableLiveData<String> photoUriResult = new MutableLiveData<>();

    private final SingleLiveEvent<String> photoNewUriResult = new SingleLiveEvent<>();
    private final MutableLiveData<List<DiagnosisItem>> chatResult = new MutableLiveData<>();
    private final SingleLiveEvent<String> chatChatResult = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorChatMessage = new SingleLiveEvent<>();
    private final SingleLiveEvent<List<AgentChatHistory>> historyResult = new SingleLiveEvent<>();
    private final SingleLiveEvent<SaveRecordData> saveRecordResult = new SingleLiveEvent<>();
    private final MutableLiveData<List<DetectionRecord>> localRecordsResult = new MutableLiveData<>();

    private final Repository repository = new Repository();
    
    // 用于传递识别成功后的数据给结果页面保存
    public static class SaveRecordData {
        public final String imageUrl;
        public final List<DiagnosisItem> diagnosisItems;
        
        public SaveRecordData(String imageUrl, List<DiagnosisItem> diagnosisItems) {
            this.imageUrl = imageUrl;
            this.diagnosisItems = diagnosisItems;
        }
    }

    public void takePhoto(DetectionActivity detectionActivity, ImageCapture imageCapture) {
        loadingState.setValue(true);

        File photoFile = new File(
                detectionActivity.getCacheDir(),
                "detection_" + System.currentTimeMillis() + ".jpg"
        );
        LogUtils.INSTANCE.d("uiuiui",photoFile +"");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(detectionActivity),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        photoCaptured.setValue(photoFile);
                        LogUtils.INSTANCE.d("uiuiui", "onimagesaved");
                        File file = compressImage(photoFile);
                        if(file == null){
                            file = photoFile;
                        }
                        LogUtils.INSTANCE.d("ljx",file +"");
                        uploadAndRecognize(detectionActivity.getApplicationContext(), "这个得了什么病",photoFile);
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
                                        historyResult.setValue(response.getData());

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


    public void loadLocalRecords(Context context) {
        LogUtils.INSTANCE.d("ljx", "loadLocalRecords 被调用");
        ThreadUtils.INSTANCE.executeByIo(() -> {
            List<DetectionRecord> records = repository.getLocalRecords(context);
            LogUtils.INSTANCE.d("ljx", "loadLocalRecords 完成，准备设置值: " + (records != null ? records.size() : 0));
            getLocalRecordsLiveData().postValue(records);
            LogUtils.INSTANCE.d("ljx", "loadLocalRecords 值已设置");
        });
    }

    public MutableLiveData<List<DetectionRecord>> getLocalRecordsLiveData() {
        return localRecordsResult;
    }

    public SingleLiveEvent<List<AgentChatHistory>> getHistoryLiveData() {
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
    public void uploadAndRecognizeFromGallery(Context context, String mes,File file) {
        loadingState.setValue(true);
        File compressedFile = compressImage(file);
        if (compressedFile == null) {
            compressedFile = file;
        }
        uploadAndRecognize(context,mes ,compressedFile);
    }

    public void insertLocalDetectionHistory(Context context, SaveRecordData data){
        if (data == null || data.diagnosisItems == null || data.diagnosisItems.isEmpty()) {
            return;
        }
        String diseaseName = data.diagnosisItems.get(0).getDiseaseName();
        String imageUrl = data.imageUrl;
        
        // 在后台线程下载图片并保存到数据库
        ThreadUtils.INSTANCE.executeByIo(() -> {
            long recordId = System.currentTimeMillis();
            String localPath = AvatarUtils.downloadAndSaveHistoryImage(context, imageUrl, recordId);
            LogUtils.INSTANCE.d("ljx", "图片下载结果: " + localPath);
            
            if (localPath != null) {
                repository.saveToLocalDatabase(diseaseName, localPath, context);
            } else {
                LogUtils.INSTANCE.d("ljx", "图片下载失败，跳过保存");
            }
        });
    }
    private void uploadAndRecognize(Context context, String mes,File photoFile) {
        LogUtils.INSTANCE.d("ljx_upload", "upload start - file: " + photoFile + ", exists: " + photoFile.exists() + ", size: " + photoFile.length());
        RequestBody requestBody = RequestBody.create(
                MediaType.parse(FileUtils.INSTANCE.getMimeType(photoFile)), photoFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", photoFile.getName(), requestBody);
        LogUtils.INSTANCE.d("ljx_upload", "mimeType: " + FileUtils.INSTANCE.getMimeType(photoFile) + ", fileName: " + photoFile.getName());

        addDisposable(
                repository.uploadAvatar(part, "uploads/demo/")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        String imageUrl = response.getData().toString();
                                        if(mes.isEmpty()){
                                            photoNewUriResult.setValue(imageUrl);
                                        }
                                        else{
                                            photoUriResult.setValue(imageUrl);
                                            LogUtils.INSTANCE.d("ljx", "imageok");
                                            recognize(mes,imageUrl,false);
                                        }
                                    } else {
                                        if(mes.isEmpty()){
                                            errorChatMessage.setValue("图片上传失败，请重试");
                                        }
                                        else {
                                            errorMessage.setValue("图片上传失败，请重试");
                                            loadingState.setValue(false);
                                            LogUtils.INSTANCE.d("ljx", "imagenotok");
                                        }
                                    }
                                },
                                error -> {
                                    if(mes.isEmpty()){
                                        errorChatMessage.setValue("图片上传失败，请重试");
                                    }
                                    else{
                                        loadingState.setValue(false);
                                        LogUtils.INSTANCE.e("ljx", error);
                                        errorMessage.setValue("图片上传失败，请重试");
                                    }

                                }
                        ));
    }

    public void recognize(String mes,String imageUrl,Boolean ischat) {
        ChatRequest chatRequest;
        if(ischat) {
            chatRequest = new ChatRequest("这次是和ai聊天，不用返回json，这是聊天，直接讲内容,下面是用户发的话: "+mes, imageUrl, null, null,false);
        }
        else {
            chatRequest = new ChatRequest(mes + "如果要存到历史记录里面，请在返回的agentResponse字段上返回List<DiagnosisItem>json供我们解析", imageUrl, null, null,true);
        }

        addDisposable(
                repository.getchat(chatRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        if(ischat){
                                            if(response.getData()!=null && !response.getData().isEmpty()){
                                                chatChatResult.setValue(response.getData().get(0).getControlPlan());
                                            }else{
                                                chatChatResult.setValue(response.getData().toString());
                                            }

                                        }
                                        else{
                                            chatResult.setValue(response.getData());
                                            LogUtils.INSTANCE.d("ljx", "aiok");
                                            // 同时发送保存记录所需的数据
                                            saveRecordResult.setValue(new SaveRecordData(imageUrl, response.getData()));
                                        }
                                    }
                                    else{
                                        if(ischat){
                                            errorChatMessage.setValue("AI连接错误");
                                        }
                                        else{
                                            errorMessage.setValue("AI连接错误");
                                        }

                                    }

                                    loadingState.setValue(false);
                                },
                                error -> {
                                    if(ischat){
                                        errorChatMessage.setValue("AI 识别失败，请重试");
                                    }
                                    else{
                                        loadingState.setValue(false);
                                        LogUtils.INSTANCE.e("ljx", error);
                                        errorMessage.setValue("AI 识别失败，请重试");
                                    }
                                }
                        ));
    }

    public    Single<ResultListAgentChatHistory>  getHistoryCount(){
        return repository.getchatHistory();
    }

    public SingleLiveEvent<String> getChatChatResult() {
        return chatChatResult;
    }

    public SingleLiveEvent<String> getErrorChatMessage() {
        return errorChatMessage;
    }

    public SingleLiveEvent<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getPhotoNewUriResult() {
        return photoNewUriResult;
    }

    public MutableLiveData<List<DiagnosisItem>> getChatResult() {
        return chatResult;
    }

    public MutableLiveData<String> getPhotoUriResult() {
        return photoUriResult;
    }

    public SingleLiveEvent<File> getPhotoCaptured() {
        return photoCaptured;
    }

    public SingleLiveEvent<SaveRecordData> getSaveRecordResult() {
        return saveRecordResult;
    }
    
    public MutableLiveData<Boolean> getLoadingState() {
        return loadingState;
    }

}
