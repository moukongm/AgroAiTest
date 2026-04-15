package com.community.data;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

import com.agri.pest.client.model.response.PostResponseDto;
import com.agri.pest.client.model.response.ResultSearchResultResponse;
import com.common.storage.database.AppDatabase;
import com.common.storage.database.SearchHistoryDao;
import com.common.storage.database.SearchHistoryRecord;
import com.network.NetworkManager;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class SearchRepository {
    private final SearchHistoryDao dao;
    public SearchRepository(Application application) {
        dao = AppDatabase.Companion.getInstance(application).searchHistoryDao();
    }
    public Single<List<SearchHistoryRecord>> getHistory() {
        return dao.getAll();
    }
    public Completable saveKeyword(String keyword) {
        return Completable.fromAction(() -> {
            SearchHistoryRecord record = new SearchHistoryRecord(
                    keyword.trim(),
                    System.currentTimeMillis()
            );
            dao.insert(record);
        });
    }
    public Completable deleteKeyword(String keyword) {
        return Completable.fromAction(() -> dao.deleteByKeyword(keyword));
    }
    public Completable clearHistory() {
        return Completable.fromAction(dao::deleteAll);
    }

    public Single<List<String>> getSuggestions(String query) {
        return NetworkManager.INSTANCE.getApi().getSuggestions(query)
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

    public Single<List<PostResponseDto>> searchPosts(String query, int page, int size) {
        return NetworkManager.INSTANCE.getApi().searchPosts(query, page, size)
                .map(result -> {
                    if (result != null
                            && result.getCode() != null
                            && result.getCode() == 200
                            && result.getData() != null
                            && result.getData().getMatches() != null
                            && result.getData().getMatches().getList() != null) {
                        return result.getData().getMatches().getList();
                    }
                    return Collections.emptyList();
                });
    }
}
