package com.detection.ui.page;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
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
import com.detection.viewmodel.DetectionViewModelFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


@Route(path = RouterPath.DETECTION_HISTORY)
public class HistoryActivity extends BaseActivity<ActivityDetectionHistoryBinding> {

    private HistoryAdapter adapter;
    private DetectionViewModel viewModel;
    ActivityDetectionHistoryBinding binding;
    List<AgentChatHistory> historyList;
    List<HistoryItem> notNetworklist=  new ArrayList<>();
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
        DetectionViewModelFactory factory = new DetectionViewModelFactory(
            getApplication()
        );
        viewModel = new ViewModelProvider(this, factory).get(DetectionViewModel.class);
        adapter = new HistoryAdapter();
        binding.rvStarPosts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvStarPosts.setAdapter(adapter);
        binding.xz.setOnClickListener(v->{
            showBlurMask();
            CalendarFragment calendarFragment = new CalendarFragment();

            LiveDataBus.getInstance().with(BusKey.FILTER).observe(this,observe->{
                String res = (String) observe;
                if(!res.isEmpty()){
                  if("close".equals(res))  {
                      hideBlurMask();
                  }else{
                      scrollToMonth(res);
                      hideBlurMask();
                  }
                }
            });
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_fragment_container, calendarFragment)
                    .commit();

        });

        binding.bg.cvInformationBack.setOnClickListener(v -> {
            finish();
        });

        // 高斯模糊
        binding.blurMask.setVisibility(View.GONE);
        setupBlurView();
    }

    private void setupBlurView() {
        ViewGroup rootView = findViewById(android.R.id.content);
        float radius = 2f;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.blurMask.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.MIRROR));
            binding.blurMask.setOverlayColor(0x40000000);
        } else {
            binding.blurMask.setupWith(rootView, new RenderScriptBlur(this))
                    .setBlurRadius(radius)
                    .setOverlayColor(0x40000000);
        }
    }

    private void showBlurMask() {
        binding.blurMask.setVisibility(View.VISIBLE);
    }

    private void hideBlurMask() {
        binding.blurMask.setVisibility(View.GONE);
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
                notNetworklist = list;
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
            if(notNetworklist!=null && !notNetworklist.isEmpty()){
                adapter.setList(notNetworklist);
                notNetworklist.clear();
                setupItemClickListener();
                binding.rvStarPosts.setVisibility(View.VISIBLE);
            }else{
                binding.consInformationContent.setBackgroundResource(R.drawable.bg_nonehistory);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
               if(list.size()<=position){

               }else{
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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void scrollToMonth(String yearMonth) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            HistoryItem item = list.get(i);
            if (item.getItemType() == HistoryItem.TYPE_DATE) {
                String dateLabel = item.getDateLabel();
                if (dateLabel != null && dateLabel.startsWith(yearMonth)) {
                    binding.rvStarPosts.smoothScrollToPosition(i);
                    return;
                }
            }
        }
        Toast.makeText(this, "没有该月份的记录", Toast.LENGTH_SHORT).show();
    }
}
