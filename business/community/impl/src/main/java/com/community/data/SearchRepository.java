package com.community.data;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

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
}
