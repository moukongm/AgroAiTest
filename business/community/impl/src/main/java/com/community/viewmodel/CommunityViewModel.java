package com.community.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.common.base.BaseViewModel;
import com.community.data.CommunityRepository;
import com.agri.pest.client.model.response.PostResponseDto;
import com.community.viewmodel.SearchViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CommunityViewModel extends BaseViewModel {

    private final MutableLiveData<List<PostResponseDto>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> toastLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMoreLiveData = new MutableLiveData<>(true);

    private final CommunityRepository repository = new CommunityRepository();
    private List<PostResponseDto> currentPosts;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;
    private boolean hasLoadedMore = false;

    public CommunityViewModel() {
        loadPosts();
    }

    public void loadPosts() {
        loadPosts("");
    }

    public void loadPosts(String keyword) {
        if (Boolean.TRUE.equals(loadingLiveData.getValue())) {
            return;
        }
        currentPage = 0;
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        Disposable disposable = repository.getPosts(currentPage, PAGE_SIZE, keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    loadingLiveData.setValue(false);
                    currentPosts = posts;
                    postsLiveData.setValue(posts);
                    hasMoreLiveData.setValue(posts != null && !posts.isEmpty() && posts.size() >= PAGE_SIZE);
                }, error -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error.getMessage());
                    toastLiveData.setValue("加载失败：" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void loadMorePosts() {
        if (Boolean.TRUE.equals(loadingLiveData.getValue()) || !Boolean.TRUE.equals(hasMoreLiveData.getValue())) {
            return;
        }
        currentPage++;
        hasLoadedMore = true;
        loadingLiveData.setValue(true);

        Disposable disposable = repository.getPosts(currentPage, PAGE_SIZE, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    loadingLiveData.setValue(false);
                    if (posts != null && !posts.isEmpty()) {
                        if (currentPosts == null) {
                            currentPosts = posts;
                        } else {
                            currentPosts.addAll(posts);
                        }
                        postsLiveData.setValue(currentPosts);
                    }
                    hasMoreLiveData.setValue(posts != null && !posts.isEmpty() && posts.size() >= PAGE_SIZE);
                }, error -> {
                    loadingLiveData.setValue(false);
                    currentPage--;
                    toastLiveData.setValue("加载更多失败：" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void searchPosts(String keyword) {
        currentPage = 0;
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        Disposable disposable = repository.getPosts(currentPage, PAGE_SIZE, keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    loadingLiveData.setValue(false);
                    postsLiveData.setValue(posts);
                    hasMoreLiveData.setValue(posts != null && !posts.isEmpty() && posts.size() >= PAGE_SIZE);
                }, error -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error.getMessage());
                    toastLiveData.setValue("搜索失败：" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void refresh() {
        currentPage = 0;
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        Disposable disposable = repository.getPosts(currentPage, PAGE_SIZE, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    loadingLiveData.setValue(false);
                    currentPosts = posts;
                    postsLiveData.setValue(posts);
                    hasMoreLiveData.setValue(posts != null && !posts.isEmpty() && posts.size() >= PAGE_SIZE);
                }, error -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error.getMessage());
                    toastLiveData.setValue("刷新失败：" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void toggleLike(long postId) {
        toggleLike(postId, null);
    }

    public void toggleLike(long postId, SearchViewModel searchViewModel) {
        if (currentPosts == null) {
            toastLiveData.setValue("数据加载中");
            return;
        }
        boolean isLiked = false;
        for (PostResponseDto post : currentPosts) {
            if (post.getId() != null && post.getId().equals(postId)) {
                isLiked = post.isLiked() != null && post.isLiked();
                break;
            }
        }
        if (isLiked) {
            unlikePost(postId, searchViewModel);
        } else {
            likePost(postId, searchViewModel);
        }
    }

    public void likePost(long postId) {
        likePost(postId, null);
    }

    public void likePost(long postId, SearchViewModel searchViewModel) {
        Disposable d = repository.likePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        refreshPosts();
                        if (searchViewModel != null) {
                            searchViewModel.refreshSearchResults();
                        }
                    }
                }, e -> {
                    toastLiveData.setValue("点赞失败");
                });
        addDisposable(d);
    }

    public void unlikePost(long postId) {
        unlikePost(postId, null);
    }

    public void unlikePost(long postId, SearchViewModel searchViewModel) {
        Disposable d = repository.unlikePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        refreshPosts();
                        if (searchViewModel != null) {
                            searchViewModel.refreshSearchResults();
                        }
                    }
                }, e -> {
                    toastLiveData.setValue("取消点赞失败");
                });
        addDisposable(d);
    }

    public void refreshPosts() {
        if (hasLoadedMore) {
            refreshAllPages();
        } else {
            Disposable disposable = repository.getPosts(0, PAGE_SIZE, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(posts -> {
                        if (posts != null) {
                            currentPosts = posts;
                            postsLiveData.setValue(currentPosts);
                        }
                    }, error -> {});
            addDisposable(disposable);
        }
    }

    private void refreshAllPages() {
        List<io.reactivex.rxjava3.core.Single<List<PostResponseDto>>> requests = new java.util.ArrayList<>();
        for (int i = 0; i <= currentPage; i++) {
            final int page = i;
            requests.add(repository.getPosts(page, PAGE_SIZE, null));
        }

        Disposable disposable = io.reactivex.rxjava3.core.Single.zip(requests, objects -> {
            List<PostResponseDto> result = new java.util.ArrayList<>();
            for (Object obj : objects) {
                if (obj instanceof List) {
                    result.addAll((List<PostResponseDto>) obj);
                }
            }
            return result;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(posts -> {
            if (posts != null) {
                currentPosts = posts;
                postsLiveData.setValue(currentPosts);
            }
        }, error -> {});
        addDisposable(disposable);
    }


    public MutableLiveData<List<PostResponseDto>> getPostsLiveData() {
        return postsLiveData;
    }

    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<String> getToastLiveData() {
        return toastLiveData;
    }
}
