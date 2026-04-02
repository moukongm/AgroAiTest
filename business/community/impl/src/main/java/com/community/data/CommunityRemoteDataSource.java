package com.community.data;

import com.agri.pest.client.model.request.CommentRequest;
import com.agri.pest.client.model.response.CommentResponseDto;
import com.agri.pest.client.model.response.PostResponseDto;
import com.agri.pest.client.model.response.ResultCommentResponseDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.network.NetworkManager;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class CommunityRemoteDataSource {

    private static volatile CommunityRemoteDataSource instance;

    public static CommunityRemoteDataSource getInstance() {
        if (instance == null) {
            synchronized (CommunityRemoteDataSource.class) {
                if (instance == null) {
                    instance = new CommunityRemoteDataSource();
                }
            }
        }
        return instance;
    }

    public Single<List<PostResponseDto>> getPosts(int page, int pageSize, String keyword) {
        return NetworkManager.INSTANCE.getApi().getPosts(null, keyword, page, pageSize)
                .map(response -> {
                    if (response != null
                            && response.getData() != null
                            && response.getData().getList() != null) {
                        return response.getData().getList();
                    }
                    return Collections.emptyList();
                });
    }

    public Single<PostResponseDto> getPostDetail(Long postId) {
        return NetworkManager.INSTANCE.getApi().getPostDetail(postId)
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return response.getData();
                    }
                    return null;
                });
    }

    public Single<List<CommentResponseDto>> getComments(Long postId) {
        return NetworkManager.INSTANCE.getApi().getComments(postId, 0, 20)
                .map(response -> {
                    if (response != null && response.getData() != null && response.getData().getList() != null) {
                        return response.getData().getList();
                    }
                    return Collections.emptyList();
                });
    }

    public Single<ResultCommentResponseDto> sendComment(Long postId, String content) {
        CommentRequest request = new CommentRequest(content);
        return NetworkManager.INSTANCE.getApi().addComment(postId,request);
    }

    public Single<ResultVoid> likePost(Long postId) {
        return NetworkManager.INSTANCE.getApi().likePost(postId);
    }

    public Single<ResultVoid> unlikePost(Long postId) {
        return NetworkManager.INSTANCE.getApi().unlikePost(postId);
    }

    public Single<ResultVoid> collectPost(Long postId){
        return NetworkManager.INSTANCE.getApi().favoritePost(postId);
    }

    public Single<ResultVoid> uncollectPost(Long postId){
        return NetworkManager.INSTANCE.getApi().unfavoritePost(postId);
    }
}
