package com.community.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.model.response.CommentResponseDto;
import com.agri.pest.client.model.response.PostResponseDto;
import com.common.base.BaseViewModel;
import com.common.cache.CacheLoader;
import com.common.cache.MemoryCache;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.community.data.CommunityRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PostDetailViewModel extends BaseViewModel {

    private final CommunityRepository repository = new CommunityRepository();

    private final MutableLiveData<PostResponseDto> postLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CommentResponseDto>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentSendSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> likeLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> collectLoadingLiveData = new MutableLiveData<>(false);

    private PostResponseDto currentPost;

    private final CacheLoader<PostResponseDto> cache = CacheLoader.<PostResponseDto>create()
            .memory(5)
            .build();

    public void loadPost(long postId) {
        if (postId <= 0) {
            errorLiveData.setValue("无效的帖子");
            return;
        }
        String key = String.valueOf(postId);

        PostResponseDto cached = cache.get(key);
        if (cached != null) {
            currentPost = cached;
            postLiveData.setValue(cached);
        }

        loadingLiveData.setValue(cached == null);

        Disposable d = repository.getPostDetail(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(post -> {
                    loadingLiveData.setValue(false);
                    currentPost = post;
                    postLiveData.setValue(post);
                    cache.put(key,post);
                    if (cached == null || !post.equals(cached)) {
                        postLiveData.setValue(post);
                    }
                }, e -> {
                    loadingLiveData.setValue(false);
                    e.printStackTrace();
                    String errorMsg = e.getMessage();
                    if (errorMsg == null) {
                        errorMsg = "加载失败: " + e.getClass().getSimpleName();
                    }
                    errorLiveData.setValue(errorMsg);
                });
        addDisposable(d);
    }

    public void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    public void loadComments(long postId) {
        Disposable d = repository.getComments(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                    commentsLiveData.setValue(comments);
                }, e -> {
                    errorLiveData.setValue("加载评论失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
                });
        addDisposable(d);
    }

    public void sendComment(long postId, String content) {
        if (content == null || content.trim().isEmpty()) {
            errorLiveData.setValue("评论内容不能为空");
            return;
        }
        loadingLiveData.setValue(true);
        Disposable d = repository.sendComment(postId, content.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    loadingLiveData.setValue(false);
                    if (success) {
                        commentSendSuccessLiveData.setValue(true);
                        loadComments(postId);
                    } else {
                        errorLiveData.setValue("发送评论失败");
                    }
                }, e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("发送评论失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
                });
        addDisposable(d);
    }

    public void toggleLike(long postId) {
        if (Boolean.TRUE.equals(likeLoadingLiveData.getValue())) {
            return;
        }
        if (currentPost == null) {
            errorLiveData.setValue("帖子加载中");
            return;
        }
        boolean isLiked = currentPost.isLiked() != null && currentPost.isLiked();
        if (isLiked) {
            unlikePost(postId);
        } else {
            likePost(postId);
        }
    }

    public void toggleCollect(long postId) {
        if (Boolean.TRUE.equals(collectLoadingLiveData.getValue())) {
            return;
        }
        if (currentPost == null) {
            errorLiveData.setValue("帖子加载中");
            return;
        }
        boolean isFavorited = currentPost.isFavorited() != null && currentPost.isFavorited();
        if (isFavorited) {
            uncollectPost(postId);
        } else {
            collectPost(postId);
        }
    }

    public void likePost(long postId) {
        likeLoadingLiveData.setValue(true);
        Disposable d = repository.likePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    likeLoadingLiveData.setValue(false);
                    if (success) {
                        refreshPost(postId);
                    }
                }, e -> {
                    likeLoadingLiveData.setValue(false);
                    errorLiveData.setValue("点赞失败");
                });
        addDisposable(d);
    }

    public void unlikePost(long postId) {
        likeLoadingLiveData.setValue(true);
        Disposable d = repository.unlikePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    likeLoadingLiveData.setValue(false);
                    if (success) {
                        refreshPost(postId);
                    }
                }, e -> {
                    likeLoadingLiveData.setValue(false);
                    errorLiveData.setValue("取消点赞失败");
                });
        addDisposable(d);
    }

    public void collectPost(long postId) {
        collectLoadingLiveData.setValue(true);
        Disposable d = repository.collectPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    collectLoadingLiveData.setValue(false);
                    if (success) {
                        refreshPost(postId);
                        LiveDataBus.getInstance().with(BusKey.COLLECT).setValue(true);
                    }
                }, e -> {
                    collectLoadingLiveData.setValue(false);
                    errorLiveData.setValue("收藏失败");
                });
        addDisposable(d);
    }

    public void uncollectPost(long postId) {
        collectLoadingLiveData.setValue(true);
        Disposable d = repository.uncollectPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    collectLoadingLiveData.setValue(false);
                    if (success) {
                        refreshPost(postId);
                        LiveDataBus.getInstance().with(BusKey.COLLECT).setValue(true);
                    }
                }, e -> {
                    collectLoadingLiveData.setValue(false);
                    errorLiveData.setValue("取消收藏失败");
                });
        addDisposable(d);
    }

    private void refreshPost(long postId) {
        Disposable d = repository.getPostDetail(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(post -> {
                    currentPost = post;
                    postLiveData.setValue(post);
                    cache.put(String.valueOf(postId), post);
                }, e -> {});
        addDisposable(d);
    }
    public MutableLiveData<PostResponseDto> getPostLiveData() {
        return postLiveData;
    }

    public MutableLiveData<List<CommentResponseDto>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<Boolean> getCommentSendSuccessLiveData() {
        return commentSendSuccessLiveData;
    }

    public MutableLiveData<Boolean> getLikeLoadingLiveData() {
        return likeLoadingLiveData;
    }

    public MutableLiveData<Boolean> getCollectLoadingLiveData() {
        return collectLoadingLiveData;
    }
}
