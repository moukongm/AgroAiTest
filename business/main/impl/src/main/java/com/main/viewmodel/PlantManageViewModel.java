package com.main.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.model.request.MyCropCreateRequest;
import com.agri.pest.client.model.request.MyCropUpdateRequest;
import com.agri.pest.client.model.response.CultivationRecordDto;
import com.agri.pest.client.model.response.MyCropResponseDto;
import com.agri.pest.client.model.response.ResultMyCropResponseDto;
import com.common.base.BaseViewModel;
import com.common.utils.FileUtils;
import com.common.utils.LogUtils;
import com.main.data.PlantAddRepository;
import com.main.data.Repository;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class PlantManageViewModel extends BaseViewModel {

    private final Repository repository = new Repository();

    // 作物详情 LiveData
    private final MutableLiveData<MyCropResponseDto> cropDetailLiveData = new MutableLiveData<>();

    // 错误消息 LiveData
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    // 加载状态 LiveData
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    private final MutableLiveData<Map<String, List<CultivationRecordDto>>> recordsLiveData = new MutableLiveData<>();

    public MutableLiveData<MyCropResponseDto> getCropDetailLiveData() {
        return cropDetailLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public MutableLiveData<Map<String, List<CultivationRecordDto>>> getRecordsLiveData() {
        return recordsLiveData;
    }


    public void getCropDetail(Long cropId) {
        if (cropId == null || cropId <= 0) {
            errorMessageLiveData.setValue("作物ID无效");
            return;
        }

        loadingLiveData.setValue(true);

        Disposable disposable = repository.getCropDetail(cropId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            loadingLiveData.setValue(false);
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                cropDetailLiveData.setValue(response.getData());
                                recordsLiveData.setValue(response.getData().getRecords());
                                LogUtils.INSTANCE.d("PlantManageViewModel", "获取作物详情成功: " + response.getData().getPlantName());
                            } else {
                                String message = response != null && response.getMessage() != null ? response.getMessage() : "获取作物详情失败";
                                errorMessageLiveData.setValue(message);
                            }
                        },
                        error -> {
                            loadingLiveData.setValue(false);
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "获取作物详情失败";
                            errorMessageLiveData.setValue(errorMsg);
                            LogUtils.INSTANCE.e("PlantManageViewModel", error);
                        }
                );
        addDisposable(disposable);
    }


    public List<CultivationRecordDto> getWaterRecords() {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey("WATERING")) {
            return records.get("WATERING");
        }
        return null;
    }


    public List<CultivationRecordDto> getFertilizeRecords() {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey("FERTILIZING")) {
            return records.get("FERTILIZING");
        }
        return null;
    }

    /**
     * 获取用药记录列表
     */
    public List<CultivationRecordDto> getMedicineRecords() {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey("MEDICATION")) {
            return records.get("MEDICATION");
        }
        return null;
    }

    /**
     * 获取笔记记录列表
     */
    public List<CultivationRecordDto> getNoteRecords() {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey("NOTE")) {
            return records.get("NOTE");
        }
        return null;
    }

    /**
     * 根据日期筛选记录
     */
    public List<CultivationRecordDto> getRecordsByDate(LocalDate date, String tagType) {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey(tagType)) {
            List<CultivationRecordDto> tagRecords = records.get(tagType);
            if (tagRecords != null) {
                for (CultivationRecordDto record : tagRecords) {
                    if (record.getRecordDate() != null && record.getRecordDate().equals(date)) {
                        return tagRecords;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据日期获取笔记内容
     * @param date 日期
     * @return 笔记内容，如果没有则返回 null
     */
    public String getNoteContentByDate(LocalDate date) {
        Map<String, List<CultivationRecordDto>> records = recordsLiveData.getValue();
        if (records != null && records.containsKey("NOTE")) {
            List<CultivationRecordDto> noteRecords = records.get("NOTE");
            if (noteRecords != null) {
                for (CultivationRecordDto record : noteRecords) {
                    if (record.getRecordDate() != null && record.getRecordDate().equals(date)) {
                        return record.getContent();
                    }
                }
            }
        }
        return null;
    }

    // 标签操作结果 LiveData
    private final MutableLiveData<Boolean> tagOperationLiveData = new MutableLiveData<>();

    public MutableLiveData<Boolean> getTagOperationLiveData() {
        return tagOperationLiveData;
    }

    // 修改作物信息操作结果 LiveData
    private final MutableLiveData<Boolean> updateCropLiveData = new MutableLiveData<>();

    public MutableLiveData<Boolean> getUpdateCropLiveData() {
        return updateCropLiveData;
    }

    // 修改作物状态常量
    public static final int STATE_IDLE = 0;
    public static final int STATE_UPLOADING = 1;
    public static final int STATE_UPDATING = 2;
    public static final int STATE_SUCCESS = 3;
    public static final int STATE_ERROR = 4;

    private final MutableLiveData<Integer> updateStateLiveData = new MutableLiveData<>(STATE_IDLE);

    public MutableLiveData<Integer> getUpdateStateLiveData() {
        return updateStateLiveData;
    }

    public void updateCropBasicInfo(Long cropId, String plantName, String status, LocalDate plantingDate, LocalDate maturityDate) {
        if (cropId == null || cropId <= 0) {
            errorMessageLiveData.setValue("作物ID无效");
            return;
        }

        updateStateLiveData.setValue(STATE_UPDATING);

        // MyCropUpdateRequest 构造函数参数: plantName, imageUrl, status, plantingDate, maturityDate, pestCount
        MyCropUpdateRequest request = new MyCropUpdateRequest(plantName, null, status, plantingDate, maturityDate, null);

        Disposable disposable = repository.updateCrop(cropId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            updateStateLiveData.setValue(STATE_IDLE);
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                cropDetailLiveData.setValue(response.getData());
                                updateCropLiveData.setValue(true);
                                updateStateLiveData.setValue(STATE_SUCCESS);
                                LogUtils.INSTANCE.d("PlantManageViewModel", "修改作物信息成功");
                            } else {
                                updateCropLiveData.setValue(false);
                                updateStateLiveData.setValue(STATE_ERROR);
                                String message = response != null && response.getMessage() != null ? response.getMessage() : "修改失败";
                                errorMessageLiveData.setValue(message);
                            }
                        },
                        error -> {
                            updateCropLiveData.setValue(false);
                            updateStateLiveData.setValue(STATE_ERROR);
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "修改失败";
                            errorMessageLiveData.setValue(errorMsg);
                            LogUtils.INSTANCE.e("PlantManageViewModel", error);
                        }
                );
        addDisposable(disposable);
    }

    /**
     * 修改作物信息（带图片上传）
     * @param context Context
     * @param cropId 作物ID
     * @param imageUri 新图片的Uri
     * @param plantName 作物名称
     * @param status 健康状态
     * @param plantingDate 种植日期
     * @param maturityDate 成熟时间
     */
    public void updateCropWithImage(android.content.Context context, Long cropId, Uri imageUri,
                                     String plantName, String status, LocalDate plantingDate, LocalDate maturityDate) {
        if (cropId == null || cropId <= 0) {
            errorMessageLiveData.setValue("作物ID无效");
            return;
        }

        if (imageUri == null) {
            // 没有新图片，直接更新文字信息
            updateCropBasicInfo(cropId, plantName, status, plantingDate, maturityDate);
            return;
        }

        updateStateLiveData.setValue(STATE_UPLOADING);

        // 先上传图片
        File file = FileUtils.INSTANCE.uriToFile(context, imageUri, context.getExternalCacheDir());
        if (file == null) {
            errorMessageLiveData.setValue("图片处理失败");
            updateStateLiveData.setValue(STATE_ERROR);
            return;
        }

        String fileName = file.getName();
        String mimeType = getMimeType(fileName);
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);

        // 使用 PlantAddRepository 进行上传
        PlantAddRepository plantAddRepository = new PlantAddRepository();
        Disposable disposable = plantAddRepository.uploadFile(part)
                .flatMap(uploadResult -> {
                    if (uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                        String imageUrl = uploadResult.getData();
                        updateStateLiveData.postValue(STATE_UPDATING);

                        // 构建更新请求: plantName, imageUrl, status, plantingDate, maturityDate, pestCount
                        MyCropUpdateRequest updateRequest = new MyCropUpdateRequest(plantName, imageUrl, status, plantingDate, maturityDate, null);

                        return repository.updateCrop(cropId, updateRequest);
                    } else {
                        return io.reactivex.rxjava3.core.Single.error(
                                new Exception(uploadResult.getMessage() != null ? uploadResult.getMessage() : "上传失败"));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            updateStateLiveData.setValue(STATE_IDLE);
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                cropDetailLiveData.setValue(response.getData());
                                updateCropLiveData.setValue(true);
                                updateStateLiveData.setValue(STATE_SUCCESS);
                                LogUtils.INSTANCE.d("PlantManageViewModel", "修改作物信息成功（含图片）");
                            } else {
                                updateCropLiveData.setValue(false);
                                updateStateLiveData.setValue(STATE_ERROR);
                                String message = response != null && response.getMessage() != null ? response.getMessage() : "修改失败";
                                errorMessageLiveData.setValue(message);
                            }
                        },
                        error -> {
                            updateCropLiveData.setValue(false);
                            updateStateLiveData.setValue(STATE_ERROR);
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "操作失败";
                            errorMessageLiveData.setValue(errorMsg);
                            LogUtils.INSTANCE.e("PlantManageViewModel", error);
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

    /**
     * 打上标签
     * @param cropId 作物ID
     * @param tagType 标签类型: WATERING, FERTILIZING, MEDICATION, NOTE
     * @param recordDate 打卡日期
     * @param content 笔记内容或状态文案
     * @param status 状态: 0-待处理, 1-已完成
     */
    public void addTag(Long cropId, String tagType, LocalDate recordDate, String content, int status) {
        Disposable disposable = repository.addTag(cropId, tagType, recordDate, content, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() == 200) {
                                tagOperationLiveData.setValue(true);
                                LogUtils.INSTANCE.d("PlantManageViewModel", "打标签成功");
                            } else {
                                tagOperationLiveData.setValue(false);
                                String message = response != null && response.getMessage() != null ? response.getMessage() : "打标签失败";
                                errorMessageLiveData.setValue(message);
                            }
                        },
                        error -> {
                            tagOperationLiveData.setValue(false);
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "打标签失败";
                            errorMessageLiveData.setValue(errorMsg);
                            LogUtils.INSTANCE.e("PlantManageViewModel", error);
                        }
                );
        addDisposable(disposable);
    }

    /**
     * 取消标签
     * @param cropId 作物ID
     * @param tagType 标签类型: WATERING, FERTILIZING, MEDICATION, NOTE
     * @param recordDate 打卡日期
     * @param status 状态: 0-待处理, 1-已完成
     */
    public void cancelTag(Long cropId, String tagType, LocalDate recordDate, int status) {
        Disposable disposable = repository.cancelTag(cropId, tagType, recordDate, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() == 200) {
                                tagOperationLiveData.setValue(true);
                                LogUtils.INSTANCE.d("PlantManageViewModel", "取消标签成功");
                            } else {
                                tagOperationLiveData.setValue(false);
                                String message = response != null && response.getMessage() != null ? response.getMessage() : "取消标签失败";
                                errorMessageLiveData.setValue(message);
                            }
                        },
                        error -> {
                            tagOperationLiveData.setValue(false);
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "取消标签失败";
                            errorMessageLiveData.setValue(errorMsg);
                            LogUtils.INSTANCE.e("PlantManageViewModel", error);
                        }
                );
        addDisposable(disposable);
    }
}
