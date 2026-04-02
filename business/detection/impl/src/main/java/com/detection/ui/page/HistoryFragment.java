package com.detection.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.common.base.BaseFragment;
import com.common.utils.LogUtils;
import com.detection.databinding.FragmentDetectionHistoryBinding;
import com.detection.ui.adapter.HistoryAdapter;
import com.detection.viewmodel.DetectionViewModel;
import com.network.NetworkManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryFragment extends BaseFragment<FragmentDetectionHistoryBinding> {

    private HistoryAdapter adapter;
    private DetectionViewModel viewModel;
    FragmentDetectionHistoryBinding binding;

    @NonNull
    @Override
    public FragmentDetectionHistoryBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDetectionHistoryBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new DetectionViewModel();
        binding = getBinding();
        adapter = new HistoryAdapter();
        binding.rvStarPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvStarPosts.setAdapter(adapter);
        viewModel.fetchHistory();
    }

    @Override
    public void initData() {
        loadHistoryData();
    }

    private void loadHistoryData() {
        viewModel.getHistoryLiveData().observe(getViewLifecycleOwner(), result -> {
            List<AgentChatHistory> historyList = result.getData();
            if (historyList != null && !historyList.isEmpty()) {
                LogUtils.INSTANCE.d("ljx",historyList.size()+"");
                adapter.setList(HistoryAdapter.toMultiList(historyList));
//                adapter.setNewData(HistoryAdapter.toMultiList(historyList));
//                binding.layoutStarEmpty.setVisibility(View.GONE);
                binding.rvStarPosts.setVisibility(View.VISIBLE);
            }
            else{
                showEmptyState();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            showEmptyState();
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

    }
    private void showEmptyState() {
//        binding.layoutStarEmpty.setVisibility(View.VISIBLE);
        binding.rvStarPosts.setVisibility(View.GONE);
    }

}
