package com.main.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.model.request.MyCropCreateRequest;
import com.agri.pest.client.model.response.MyCropResponseDto;
import com.common.base.BaseViewModel;
import com.common.utils.FileUtils;
import com.main.data.PlantAddRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PlantAddViewModel extends BaseViewModel {

    private final PlantAddRepository repository = new PlantAddRepository();

    public static final int STATE_IDLE = 0;
    public static final int STATE_UPLOADING = 1;
    public static final int STATE_CREATING = 2;
    public static final int STATE_SUCCESS = 3;
    public static final int STATE_ERROR = 4;

    private final MutableLiveData<Integer> stateLiveData = new MutableLiveData<>(STATE_IDLE);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> cropIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<MyCropResponseDto> cropResponseLiveData = new MutableLiveData<>();

    public MutableLiveData<Integer> getStateLiveData() {
        return stateLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<Long> getCropIdLiveData() {
        return cropIdLiveData;
    }

    public MutableLiveData<MyCropResponseDto> getCropResponseLiveData() {
        return cropResponseLiveData;
    }

    public void createCropWithImage(android.content.Context context, Uri imageUri, String plantName) {
        stateLiveData.setValue(STATE_UPLOADING);

        File file = FileUtils.INSTANCE.uriToFile(context, imageUri, context.getExternalCacheDir());
        if (file == null) {
            errorMessageLiveData.setValue("图片处理失败");
            stateLiveData.setValue(STATE_ERROR);
            return;
        }

        uploadAndCreateCrop(file, plantName);
    }

    private void uploadAndCreateCrop(File file, String plantName) {
        String fileName = file.getName();
        String mimeType = getMimeType(fileName);
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);

        Disposable disposable = repository.uploadFile(part)
                .flatMap(uploadResult -> {
                    if (uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                        String imageUrl = uploadResult.getData();
                        stateLiveData.postValue(STATE_CREATING);

                        MyCropCreateRequest request = new MyCropCreateRequest(plantName, imageUrl, null, null, null, null);
                        return repository.createCrop(request)
                                .doOnSuccess(response -> Log.d("PlantAdd", "创建成功: " + response));
                    } else {
                        return io.reactivex.rxjava3.core.Single.error(
                                new Exception(uploadResult.getMessage() != null ? uploadResult.getMessage() : "上传失败"));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.getCode() == 200 && response.getData() != null && response.getData().getId() != null) {
                                cropIdLiveData.setValue(response.getData().getId());
                                cropResponseLiveData.setValue(response.getData());
                                stateLiveData.setValue(STATE_SUCCESS);
                            } else {
                                errorMessageLiveData.setValue(response.getMessage() != null ? response.getMessage() : "创建失败");
                                stateLiveData.setValue(STATE_ERROR);
                            }
                        },
                        error -> {
                            errorMessageLiveData.setValue(error.getMessage() != null ? error.getMessage() : "操作失败");
                            stateLiveData.setValue(STATE_ERROR);
                        }
                );
        addDisposable(disposable);
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/*";
    }

    public void resetState() {
        stateLiveData.setValue(STATE_IDLE);
        errorMessageLiveData.setValue(null);
    }
}
