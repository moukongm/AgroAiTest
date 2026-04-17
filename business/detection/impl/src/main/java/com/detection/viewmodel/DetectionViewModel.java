package com.detection.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
import com.agri.pest.client.model.response.SseEmitter;
import com.common.base.BaseViewModel;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.storage.database.DetectionRecord;
import com.common.utils.AvatarUtils;
import com.common.utils.FileUtils;
import com.common.utils.LogUtils;
import com.common.utils.NetworkUtil;
import com.common.utils.SingleLiveEvent;
import com.common.utils.ThreadUtils;
import com.detection.cloudmodel.Recognition;
import com.detection.cloudmodel.TFLiteClassifier;
import com.detection.data.Repository;
import com.detection.data.UserLocalDataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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

    // 新增流式相关 LiveData
    private final MutableLiveData<String> streamDelta = new MutableLiveData<>();  // 增量文本
    private final MutableLiveData<String> finalPlan = new MutableLiveData<>();  // 完整方案
    private final MutableLiveData<Boolean> streamDone = new MutableLiveData<>();  // 流式结束标志
    private final Repository repository = new Repository();
    private TFLiteClassifier localClassifier; 
    private final Application application;  // Application Context（用于访问 assets）
    
    // 用于传递识别成功后的数据给结果页面保存
    public static class SaveRecordData {
        public final String imageUrl;
        public final List<DiagnosisItem> diagnosisItems;

        public SaveRecordData(String imageUrl, List<DiagnosisItem> diagnosisItems) {
            this.imageUrl = imageUrl;
            this.diagnosisItems = diagnosisItems;
        }
    }
    
    public DetectionViewModel(@NonNull Application application) {
        super();
        this.application = application;
        initLocalClassifier();
    }
    
    
     // 初始化本地 TFLite 模型
  
    private void initLocalClassifier() {
        try {
            // 检查 assets 中是否有模型文件
            String[] assets = application.getAssets().list("");
            
            boolean hasModel = false, hasLabels = false;
            if (assets != null) {
                for (String asset : assets) {
                    if ("model.tflite".equals(asset)) hasModel = true;
                    if ("labels.txt".equals(asset)) hasLabels = true;
                }
            }
            if (!hasModel || !hasLabels) {
                LogUtils.INSTANCE.d("LocalModel", "模型文件缺失（model.tflite: " + hasModel + ", labels.txt: " + hasLabels + "），本地识别不可用");
                return;
            }
            
            // 使用 open() 方法读取文件流
            LogUtils.INSTANCE.d("LocalModel", "正在加载模型文件...");
            try (InputStream modelStream = application.getAssets().open("model.tflite");
                 InputStream labelStream = application.getAssets().open("labels.txt")) {
                localClassifier = new TFLiteClassifier(modelStream, labelStream);
                 }

            
        } catch (IOException e) {
            LogUtils.INSTANCE.e("LocalModel", "本地模型加载失败 - IOException", e);
            localClassifier = null;
        } catch (Exception e) {
            LogUtils.INSTANCE.e("LocalModel", "本地模型加载失败", e);
            localClassifier = null;
        }
    }

    public void takePhoto(Context context, ImageCapture imageCapture) {
        loadingState.setValue(true);

        File photoFile = new File(
                context.getCacheDir(),
                "detection_" + System.currentTimeMillis() + ".jpg"
        );
        LogUtils.INSTANCE.d("uiuiui", photoFile + "");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        photoCaptured.setValue(photoFile);
                        // 同时设置图片路径供结果显示使用
                        photoUriResult.setValue("file://" + photoFile.getAbsolutePath());
                        LogUtils.INSTANCE.d("uiuiui", "onimagesaved");
                        File file = compressImage(photoFile);
                        if (file == null) {
                            file = photoFile;
                        }
                        LogUtils.INSTANCE.d("ljx", file + "");
                        // 改为双路识别
                        recognizeImage(file);
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
            try (FileOutputStream fos = new FileOutputStream(file)) {
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
            } finally {
                bitmap.recycle();
                if (scaled != bitmap) {
                    scaled.recycle();
                }
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadAndRecognizeFromGallery(Context context, String mes, File file) {
        loadingState.setValue(true);
        File compressedFile = compressImage(file);
        if (compressedFile == null) {
            compressedFile = file;
        }
        uploadAndRecognize(context, mes, compressedFile);
    }

    //双路识别
    public void recognizeImage(File imageFile) {
        loadingState.setValue(true);
        Application app = this.application;
        
        if (!NetworkUtil.isNetworkAvailable(app)) {
            // 无网络，走本地
            LogUtils.INSTANCE.d("Recognition", "使用本地模型识别");
            recognizeLocally(imageFile);
        } else {
            // 有网络，走云端
            LogUtils.INSTANCE.d("Recognition", "使用云端API识别");
            uploadAndRecognize(app, "这个得了什么病", imageFile);
        }
    }

    public void insertLocalDetectionHistory(Context context, SaveRecordData data) {
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

    private void uploadAndRecognize(Context context, String mes, File photoFile) {
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
                                        if (mes.isEmpty()) {
                                            photoNewUriResult.setValue(imageUrl);
                                        } else {
                                            photoUriResult.setValue(imageUrl);
                                            LogUtils.INSTANCE.d("ljx", "imageok");
                                            recognize(mes, imageUrl, false);
                                        }
                                    } else {
                                        if (mes.isEmpty()) {
                                            errorChatMessage.setValue("图片上传失败，请重试");
                                        } else {
                                            errorMessage.setValue("图片上传失败，请重试");
                                            loadingState.setValue(false);
                                            LogUtils.INSTANCE.d("ljx", "imagenotok");
                                        }
                                    }
                                },
                                error -> {
                                    if (mes.isEmpty()) {
                                        errorChatMessage.setValue("图片上传失败，请重试");
                                    } else {
                                        loadingState.setValue(false);
                                        LogUtils.INSTANCE.e("ljx", error);
                                        errorMessage.setValue("图片上传失败，请重试");
                                    }

                                }
                        ));
    }

    public void aiChatRecognize(String mes, String imageUrl) {
        ChatRequest chatRequest;
        chatRequest = new ChatRequest(mes, imageUrl, null, null, false);
        addDisposable(
                repository.getChatStream(chatRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                },
                                error -> {

                                }
                        ));
    }

    public void recognize(String mes, String imageUrl, Boolean ischat) {
        ChatRequest chatRequest;
        if (ischat) {
            chatRequest = new ChatRequest("这次是和ai聊天，不用返回json，这是聊天，直接讲内容,下面是用户发的话: " + mes, imageUrl, null, null, false);
        } else {
            chatRequest = new ChatRequest(mes + "如果要存到历史记录里面，请在返回的agentResponse字段上返回List<DiagnosisItem>json供我们解析", imageUrl, null, null, true);
        }

        addDisposable(
                repository.getchat(chatRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == ServiceCode.SUCCESS) {
                                        if (ischat) {
                                            if (response.getData() != null && !response.getData().isEmpty()) {
                                                chatChatResult.setValue(response.getData().get(0).getControlPlan());
                                            } else {
                                                chatChatResult.setValue(response.getData().toString());
                                            }

                                        } else {
                                            chatResult.setValue(response.getData());
                                            LogUtils.INSTANCE.d("ljx", "aiok");
                                            // 同时发送保存记录所需的数据
                                            saveRecordResult.setValue(new SaveRecordData(imageUrl, response.getData()));
                                        }
                                    } else {
                                        if (ischat) {
                                            errorChatMessage.setValue("AI连接错误");
                                        } else {
                                            errorMessage.setValue("AI连接错误");
                                        }

                                    }

                                    loadingState.setValue(false);
                                },
                                error -> {
                                    if (ischat) {
                                        errorChatMessage.setValue("AI 识别失败，请重试");
                                    } else {
                                        loadingState.setValue(false);
                                        LogUtils.INSTANCE.e("ljx", error);
                                        errorMessage.setValue("AI 识别失败，请重试");
                                    }
                                }
                        ));
    }

    public MutableLiveData<String> getFinalPlan() {
        return finalPlan;
    }

    public MutableLiveData<String> getStreamDelta() {
        return streamDelta;
    }

    public MutableLiveData<Boolean> getStreamDone() {
        return streamDone;
    }

    public Single<ResultListAgentChatHistory> getHistoryCount() {
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

    //file转bitmap转buffer获取结果setvalue
    private void recognizeLocally(File imageFile) {
        if (localClassifier == null) {
            postError("本地模型未加载，请检查模型文件");
            return;
        }
        
        ThreadUtils.INSTANCE.executeByIo(() -> {
            try {
                //压缩图片
                File compressedFile = compressImage(imageFile);
                if (compressedFile == null) compressedFile = imageFile;

                BitmapFactory.Options opts = new BitmapFactory.Options();

                //根据file计算图片采样率
                opts.inSampleSize = calculateInSampleSize(compressedFile);

                //把file加载bitmap，通过采样率来对file转换为bitmap，长度大是1024像素
                Bitmap bitmap = BitmapFactory.decodeFile(
                    compressedFile.getAbsolutePath(), 
                    opts
                );
                
                if (bitmap == null) {
                    postError("图片解码失败");
                    return;
                }
                
                // 把bitmap转换成模型需要的；
                ByteBuffer inputBuffer = convertBitmapToByteBuffer(bitmap);
                bitmap.recycle();  // 及时回收
                
                // 本地推理
                List<Recognition> localResults = localClassifier.recognizeImage(inputBuffer);
                List<DiagnosisItem> diagnosisItems = convertRecognitionToDiagnosis(localResults);

                AndroidSchedulers.mainThread().scheduleDirect(() -> {
                    chatResult.setValue(diagnosisItems);
//                    saveRecordResult.setValue(new SaveRecordData(
//                        "local://" + imageFile.getAbsolutePath(),
//                        diagnosisItems
//                    ));
                    loadingState.setValue(false);
                });
                
            } catch (Exception e) {
                LogUtils.INSTANCE.e("TFLite", "本地识别异常", e);
                postError("本地识别失败: " + e.getMessage());
            }
        });
    }
    
    //计算图片采样率
    private int calculateInSampleSize(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只读宽高，不加载，放到outWidth，outHeight；
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        
        int width = options.outWidth;
        int height = options.outHeight;
        int maxSize = 1024;
        int inSampleSize = 1;

        //计算采样率；
        if (width > maxSize || height > maxSize) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while ((halfWidth / inSampleSize) >= maxSize && 
                   (halfHeight / inSampleSize) >= maxSize) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    

     // 将 Bitmap 转换为 TFLite 模型需要的 ByteBuffer

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // 使用模型实际的输入尺寸
        int inputSize = localClassifier.getInputSize();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        buffer.order(java.nio.ByteOrder.nativeOrder());
        
        // 缩放至模型输入尺寸
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        
        // 提取像素并归一化
        int[] pixels = new int[inputSize * inputSize];
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);
        
        for (int pixel : pixels) {
            // 提取 R、G、B 通道，归一化到 [0,1]
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);  // R
            buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);   // G
            buffer.putFloat((pixel & 0xFF) / 255.0f);          // B
        }
        
        scaled.recycle();
        return buffer;
    }
    

    private List<DiagnosisItem> convertRecognitionToDiagnosis(List<Recognition> recognitions) {
        List<DiagnosisItem> items = new ArrayList<>();
        for (Recognition r : recognitions) {
            DiagnosisItem item = new DiagnosisItem(
                r.getDiseaseName(),
                (int) r.getConfidence(),
                r.getControlPlan()
            );
            items.add(item);
        }
        return items;
    }

    //在主线程显示错误
    private void postError(String msg) {
        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            errorMessage.setValue(msg);
            loadingState.setValue(false);
        });
    }

}
