package com.detection.ui.page;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.agri.pest.client.model.response.DiagnosisItem;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.storage.database.DetectionRecord;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.ActivityDetectionHistoryBinding;
import com.detection.model.HistoryItem;
import com.detection.ui.adapter.HistoryAdapter;
import com.detection.ui.adapter.HistoryListMapper;
import com.detection.viewmodel.DetectionViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


@Route(path = RouterPath.DETECTION_HISTORY)
public class HistoryActivity extends BaseActivity<ActivityDetectionHistoryBinding> {

    private HistoryAdapter adapter;
    private DetectionViewModel viewModel;
    ActivityDetectionHistoryBinding binding;
    List<AgentChatHistory> historyList;
    List<HistoryItem> list;

    @NonNull
    @Override
    public ActivityDetectionHistoryBinding getViewBinding() {
        return ActivityDetectionHistoryBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        binding = getBinding();
        LogUtils.INSTANCE.d("home","history");
        showLoading("加载中...");
        viewModel = new ViewModelProvider(this).get(DetectionViewModel.class);
        adapter = new HistoryAdapter();
        binding.rvStarPosts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvStarPosts.setAdapter(adapter);
        binding.bg.cvInformationBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void initData() {
        showLoading("加载中...");
        viewModel.loadLocalRecords(getApplicationContext());
        viewModel.fetchHistory();
        observeData();
    }

    private void observeData() {
        // 1. 先观察本地数据，显示本地数据
        viewModel.getLocalRecordsLiveData().observe(this, records -> {
            if (records != null && !records.isEmpty()) {
                LogUtils.INSTANCE.d("opopop","local");
                list = HistoryListMapper.toMultiListFromLocal(records);
                LogUtils.INSTANCE.d("opopop",list.size()+"");
                adapter.setList(list);
                setupItemClickListener();
                binding.rvStarPosts.setVisibility(View.VISIBLE);
            }
        });

        // 2. 观察网络数据，用网络数据覆盖本地数据
        viewModel.getHistoryLiveData().observe(this, historyList -> {
            hideLoading();
            if (historyList != null && !historyList.isEmpty()) {
                list = HistoryListMapper.toMultiList(historyList);
                adapter.setList(list);
                setupItemClickListener();
                binding.rvStarPosts.setVisibility(View.VISIBLE);
            }
        });

        // 3. 网络错误时保持显示本地数据
        viewModel.getErrorLiveData().observe(this, error -> {
            hideLoading();
            // 如果 adapter 没有数据，说明本地也没有，显示暂无
            if (adapter.getItemCount() == 0) {
                binding.consInformationContent.setBackgroundResource(R.drawable.bg_nonehistory);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                HistoryItem historyItem = list.get(position);
                if (historyItem.getChatHistory() != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_history, new RecognitionResultFragment(historyItem.getChatHistory()))
                            .addToBackStack(null)
                            .commit();
                } else if (historyItem.getLocalRecord() != null) {
                    // 本地记录，点击后可以查看详情
                    ToastUtils.INSTANCE.showShort(getApplicationContext(), "本地记录详情");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
