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
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseFragment;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.FragmentDetectionHistoryBinding;
import com.detection.model.HistoryItem;
import com.detection.ui.adapter.HistoryAdapter;
import com.detection.ui.adapter.HistoryListMapper;
import com.detection.viewmodel.DetectionViewModel;
import com.network.NetworkManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryFragment extends BaseFragment<FragmentDetectionHistoryBinding> {

    private HistoryAdapter adapter;
    private DetectionViewModel viewModel;
    FragmentDetectionHistoryBinding binding;
    List<AgentChatHistory> historyList;
    List<HistoryItem> list;

    @NonNull
    @Override
    public FragmentDetectionHistoryBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDetectionHistoryBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        showLoading("稍等一会呢...");
        viewModel = new DetectionViewModel();
        adapter = new HistoryAdapter();
        binding.rvStarPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvStarPosts.setAdapter(adapter);
        viewModel.fetchHistory();
        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

    }

    @Override
    public void initData() {
        loadHistoryData();
    }

    private void loadHistoryData() {
        viewModel.getHistoryLiveData().observe(getViewLifecycleOwner(), result -> {
            hideLoading();
            historyList = result.getData();
            if (historyList != null && !historyList.isEmpty()) {
                LogUtils.INSTANCE.d("ljx",historyList.size()+"");
                list=  HistoryListMapper.toMultiList(historyList);
                adapter.setList(list);
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                        HistoryItem historyItem = list.get(position);
                        getChildFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fl_history, new RecognitionResultFragment(historyItem.getChatHistory()))
                                .addToBackStack(null)
                                .commit();

                    }
                });
//                adapter.setNewData(HistoryAdapter.toMultiList(historyList));
//                binding.layoutStarEmpty.setVisibility(View.GONE);
                binding.rvStarPosts.setVisibility(View.VISIBLE);
            }
            else{
                ToastUtils.INSTANCE.showShort(getActivity().getApplicationContext(),"暂无");
                showEmptyState();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            showEmptyState();
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

    }
    private void showEmptyState() {
        binding.bgHistoryNone.setVisibility(View.VISIBLE);
        binding.rvStarPosts.setVisibility(View.GONE);
    }

}
