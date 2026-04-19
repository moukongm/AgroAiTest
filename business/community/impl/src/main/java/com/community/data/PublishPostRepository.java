package com.community.data;

import com.agri.pest.client.model.request.PostCreateRequest;
import com.agri.pest.client.model.response.PostResponseDto;
import com.agri.pest.client.model.response.ResultListString;
import com.agri.pest.client.model.response.ResultPostResponseDto;
import com.network.NetworkManager;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;

public class PublishPostRepository {

    public Single<List<String>> uploadFiles(List<MultipartBody.Part> file){
        return NetworkManager.INSTANCE.getApi().uploadFiles(file, null)
                .map(result -> {
                    if (result != null
                            && result.getCode() != null
                            && result.getCode() == 200
                            && result.getData() != null) {
                        return result.getData();
                    }
                    return Collections.emptyList();
                });
    }

    public Single<PostResponseDto> createPost(PostCreateRequest request){
        return NetworkManager.INSTANCE.getApi().createPost(request)
                .map(result -> {
                    if (result != null
                            && result.getCode() != null
                            && result.getCode() == 200
                            && result.getData() != null) {
                        return result.getData();
                    }
                    return null;
                });
    }
}
