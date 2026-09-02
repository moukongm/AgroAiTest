package com.community.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.common.base.BaseViewModel;
import com.common.storage.database.SearchHistoryRecord;
import com.community.data.SearchRepository;
import com.agri.pest.client.model.response.PostResponseDto;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchViewModel extends BaseViewModel {
    private static final long DEBOUNCE_MS = 300;
    private final MutableLiveData<List<String>> suggestionsLiveData =
            new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<String>> getSuggestionsLiveData() {
        return suggestionsLiveData;
    }
    private final MutableLiveData<List<String>> historyLiveData =
            new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<String>> getHistoryLiveData() {
        return historyLiveData;
    }

    private final MutableLiveData<List<PostResponseDto>> searchResultsLiveData =
            new MutableLiveData<>();
    public LiveData<List<PostResponseDto>> getSearchResultsLiveData() {
        return searchResultsLiveData;
    }

    private final MutableLiveData<Boolean> searchLoadingLiveData =
            new MutableLiveData<>(false);
    public LiveData<Boolean> getSearchLoadingLiveData() {
        return searchLoadingLiveData;
    }

    private final MutableLiveData<Boolean> hasMoreSearchResultsLiveData =
            new MutableLiveData<>(true);
    public LiveData<Boolean> getHasMoreSearchResultsLiveData() {
        return hasMoreSearchResultsLiveData;
    }

    private String currentSearchQuery;
    private int currentSearchPage = 0;
    private static final int SEARCH_PAGE_SIZE = 10;
    private List<PostResponseDto> currentSearchPosts;
    private SearchRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    public void init(Application application){
        if(repository == null) {
            repository = new SearchRepository(application);
            loadHistory();
        }
    }
    public void onQueryChanged(String query) {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        if (query == null || query.trim().isEmpty()) {
            suggestionsLiveData.setValue(Collections.emptyList());
            return;
        }
        pendingSearch = () -> fetchSuggestions(query.trim());
        handler.postDelayed(pendingSearch, DEBOUNCE_MS);
    }
    private void fetchSuggestions(String query) {
        disposables.add(
                repository.getSuggestions(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    suggestionsLiveData.setValue(result);
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    suggestionsLiveData.setValue(Collections.emptyList());
                                }
                        )
        );
    }
    public void saveToHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        disposables.add(
                repository.saveKeyword(keyword)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::loadHistory, Throwable::printStackTrace)
        );
    }
    public void loadHistory() {
        disposables.add(
                repository.getHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(records -> {
                            List<String> keywords = new ArrayList<>();
                            if (records != null) {
                                for (SearchHistoryRecord r : records) {
                                    if (r.getKeyword() != null) {
                                        keywords.add(r.getKeyword());
                                    }
                                }
                            }
                            historyLiveData.setValue(keywords);
                        }, Throwable::printStackTrace)
        );
    }
    public void deleteHistoryItem(String keyword) {
        disposables.add(
                repository.deleteKeyword(keyword)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::loadHistory, Throwable::printStackTrace)
        );
    }

    public void searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) return;
        currentSearchQuery = query.trim();
        currentSearchPage = 0;
        searchLoadingLiveData.setValue(true);

        disposables.add(
                repository.searchPosts(currentSearchQuery, currentSearchPage, SEARCH_PAGE_SIZE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(posts -> {
                            searchLoadingLiveData.setValue(false);
                            currentSearchPosts = posts;
                            searchResultsLiveData.setValue(posts);
                            hasMoreSearchResultsLiveData.setValue(
                                    posts != null && !posts.isEmpty() && posts.size() >= SEARCH_PAGE_SIZE);
                        }, throwable -> {
                            searchLoadingLiveData.setValue(false);
                            throwable.printStackTrace();
                            searchResultsLiveData.setValue(Collections.emptyList());
                        })
        );
    }

    public void loadMoreSearchResults() {
        if (Boolean.TRUE.equals(searchLoadingLiveData.getValue()) ||
                !Boolean.TRUE.equals(hasMoreSearchResultsLiveData.getValue())) {
            return;
        }
        currentSearchPage++;
        searchLoadingLiveData.setValue(true);

        disposables.add(
                repository.searchPosts(currentSearchQuery, currentSearchPage, SEARCH_PAGE_SIZE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(posts -> {
                            searchLoadingLiveData.setValue(false);
                            if (posts != null && !posts.isEmpty()) {
                                if (currentSearchPosts == null) {
                                    currentSearchPosts = posts;
                                } else {
                                    currentSearchPosts.addAll(posts);
                                }
                                searchResultsLiveData.setValue(currentSearchPosts);
                            }
                            hasMoreSearchResultsLiveData.setValue(
                                    posts != null && !posts.isEmpty() && posts.size() >= SEARCH_PAGE_SIZE);
                        }, throwable -> {
                            searchLoadingLiveData.setValue(false);
                            currentSearchPage--;
                            throwable.printStackTrace();
                        })
        );
    }
    public void clearHistory() {
        disposables.add(
                repository.clearHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> historyLiveData.setValue(Collections.emptyList()),
                                Throwable::printStackTrace
                        )
        );
    }
    public void refreshSearchResults() {
        if (currentSearchPosts == null || currentSearchPosts.isEmpty()) {
            return;
        }
        disposables.add(
                repository.searchPosts(currentSearchQuery, 0, currentSearchPosts.size())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(posts -> {
                            if (posts != null && !posts.isEmpty()) {
                                currentSearchPosts = posts;
                                searchResultsLiveData.setValue(currentSearchPosts);
                            }
                        }, throwable -> {})
        );
    }

    public void updatePostLikeStatus(long postId, boolean isLiked, int likeCount) {
        if(currentSearchPosts == null) return;
        for(int i = 0; i < currentSearchPosts.size(); i++) {
            PostResponseDto post = currentSearchPosts.get(i);
            if(post.getId() != null && post.getId().equals(postId)) {
                currentSearchPosts.set(i, createLikeUpdatePost(post, isLiked ,likeCount));
                break;
            }
        }
    }

    private PostResponseDto createLikeUpdatePost(PostResponseDto item, boolean isLiked, int likeCount) {
        return new PostResponseDto(
                item.getId(),
                item.getTitle(),
                item.getContent(),
                item.getImages(),
                item.getImageSizes(),
                item.getTags(),
                item.getAuthorId(),
                item.getAuthorName(),
                item.getAuthorUsername(),
                item.getAuthorAvatar(),
                item.getAuthorAvatarWidth(),
                item.getAuthorAvatarHeight(),
                likeCount,
                item.getFavoriteCount(),
                item.getCommentCount(),
                isLiked,
                item.isFavorited(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        handler.removeCallbacksAndMessages(null);
    }
}
