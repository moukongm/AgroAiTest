package com.community.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.model.request.PostCreateRequest;
import com.agri.pest.client.model.response.PostResponseDto;
import com.common.base.BaseViewModel;
import com.community.data.PublishPostRepository;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PublishPostViewModel extends BaseViewModel {

    private PublishPostRepository repository = new PublishPostRepository();

    public static final int STATE_IDLE = 0;
    public static final int STATE_UPLOADING = 1;
    public static final int STATE_PUBLISHING = 2;
    public static final int STATE_SUCCESS = 3;
    public static final int STATE_ERROR = 4;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> publishState = new MutableLiveData<>(STATE_IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<PostResponseDto> publishSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getPublishState() {
        return publishState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<PostResponseDto> getPublishSuccess() {
        return publishSuccess;
    }

    public void publishPost(String title, String content, List<File> imageFiles, List<String> tags) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            createPost(title, content, Collections.emptyList(), tags);
        } else {
            uploadImagesAndPublish(title, content, imageFiles, tags);
        }
    }

    private void uploadImagesAndPublish(String title, String content, List<File> imageFiles, List<String> tags) {
        isLoading.setValue(true);
        publishState.setValue(STATE_UPLOADING);
        List<MultipartBody.Part> parts = new java.util.ArrayList<>();
        for (File file : imageFiles) {
            String fileName = file.getName();
            String mimeType = "image/*";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                mimeType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (fileName.endsWith(".webp")) {
                mimeType = "image/webp";
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", fileName, requestBody);
            parts.add(part);
        }

        Disposable disposable = repository.uploadFiles(parts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        imageUrls -> {
                            isLoading.setValue(false);
                            if (imageUrls.isEmpty()) {
                                errorMessage.setValue("图片上传失败");
                                publishState.setValue(STATE_ERROR);
                            } else {
                                createPost(title, content, imageUrls, tags);
                            }
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            errorMessage.setValue("图片上传失败: " + throwable.getMessage());
                            publishState.setValue(STATE_ERROR);
                        }
                );
        addDisposable(disposable);
    }

    private void createPost(String title, String content, List<String> imageUrls, List<String> tags) {
        isLoading.setValue(true);
        publishState.setValue(STATE_PUBLISHING);

        if (title == null || title.isEmpty()) {
            title = "无标题";
        }

        PostCreateRequest request = new PostCreateRequest(title, content, imageUrls, tags);

        Disposable disposable = repository.createPost(request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        post -> {
                            isLoading.setValue(false);
                            if (post != null) {
                                publishSuccess.setValue(post);
                                publishState.setValue(STATE_SUCCESS);
                            } else {
                                errorMessage.setValue("发布失败");
                                publishState.setValue(STATE_ERROR);
                            }
                        },
                        throwable -> {
                            isLoading.setValue(false);
                            errorMessage.setValue("发布失败: " + throwable.getMessage());
                            publishState.setValue(STATE_ERROR);
                        }
                );
        addDisposable(disposable);
    }

    public void resetState() {
        isLoading.setValue(false);
        publishState.setValue(STATE_IDLE);
        errorMessage.setValue(null);
    }
}
