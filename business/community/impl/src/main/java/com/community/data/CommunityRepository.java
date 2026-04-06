package com.community.data;

import com.agri.pest.client.model.response.CommentResponseDto;
import com.agri.pest.client.model.response.PostResponseDto;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class CommunityRepository {

    private final CommunityRemoteDataSource remoteDataSource;

    public CommunityRepository() {
        this.remoteDataSource = CommunityRemoteDataSource.getInstance();
    }

    public Single<List<PostResponseDto>> getPosts(int page, int pageSize, String keyword) {
        return remoteDataSource.getPosts(page, pageSize, keyword);
    }

    public Single<PostResponseDto> getPostDetail(Long postId) {
        return remoteDataSource.getPostDetail(postId);
    }

    public Single<List<CommentResponseDto>> getComments(Long postId) {
        return remoteDataSource.getComments(postId);
    }

    public Single<Boolean> sendComment(Long postId, String content) {
        return remoteDataSource.sendComment(postId, content)
                .map(result -> result != null && result.getCode() == 200);
    }

    public Single<Boolean> likePost(Long postId) {
        return remoteDataSource.likePost(postId)
                .map(result -> result != null && result.getCode() == 200);
    }

    public Single<Boolean> unlikePost(Long postId) {
        return remoteDataSource.unlikePost(postId)
                .map(result -> result != null && result.getCode() == 200);
    }

    public Single<Boolean> collectPost(Long postId){
        return remoteDataSource.collectPost(postId)
                .map(result -> result != null && result.getCode() == 200);
    }

    public Single<Boolean> uncollectPost(Long postId){
        return remoteDataSource.uncollectPost(postId)
                .map(result -> result != null && result.getCode() == 200);
    }
}
