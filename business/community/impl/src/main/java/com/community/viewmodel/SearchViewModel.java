package com.community.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.common.base.BaseViewModel;
import com.common.storage.database.SearchHistoryRecord;
import com.community.data.SearchRepository;
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
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        handler.removeCallbacksAndMessages(null);
    }
}
