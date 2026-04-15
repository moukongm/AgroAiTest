package com.detection.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.model.response.AgentChatHistory;

import com.agri.pest.client.model.response.DiagnosisItem;
import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.utils.AvatarUtils;
import com.common.utils.ImageLoader;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.FragmentRecocgnitionResultBinding;
import com.detection.viewmodel.DetectionViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.noties.markwon.Markwon;

public class RecognitionResultFragment extends BaseFragment<FragmentRecocgnitionResultBinding> {
    private DetectionViewModel viewModel;

    private List<DiagnosisItem> diagnosisItems;
    private int currentIndex = 0;
    private Boolean ishistory = false;
    FragmentRecocgnitionResultBinding binding;
    Gson gson;
    private AgentChatHistory history = null;
    private String pendingImageUrl = null;
    Markwon markwon;

    @NonNull
    @Override
    public FragmentRecocgnitionResultBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRecocgnitionResultBinding.inflate(inflater, container, false);
    }

    public RecognitionResultFragment(AgentChatHistory history) {
        this.history = history;
        if (history != null && history.getAgentResponse() != null) {
            this.pendingImageUrl = history.getImageUrl();
        }
    }

    public RecognitionResultFragment() {
    }


    @Override
    public void initView() {
        viewModel = new ViewModelProvider(requireActivity()).get(DetectionViewModel.class);
        binding = getBinding();

        markwon = Markwon.builder(requireActivity().getApplicationContext())
                .build();
        if(history != null){
            binding.btnCamera.setVisibility(View.INVISIBLE);
        }
        // 设置标签点击事件
        binding.tvRes1.setOnClickListener(v -> selectTab(0));
        binding.tvRes2.setOnClickListener(v -> selectTab(1));
        binding.tvRes3.setOnClickListener(v -> selectTab(2));

        Boolean cd= history != null ? Boolean.FALSE : Boolean.TRUE;
        binding.llAi.setOnClickListener(v ->{
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_result, new AIMainFragment(cd))
                    .addToBackStack(null)
                    .commit();
        });
        binding.btnCamera.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        if (history != null) {
            LogUtils.INSTANCE.d("ljxxjl", "history: " + history.getAgentResponse());
            if (history.getAgentResponse() != null) {
                try {
                     gson = new Gson();
                     TypeToken<List<DiagnosisItem>> listType = new TypeToken<List<DiagnosisItem>>(){};
                     diagnosisItems = gson.fromJson(history.getAgentResponse().toString(), listType.getType());
                } catch (Exception e) {
                    LogUtils.INSTANCE.e("ljxxjl", e);
                    notHaveResult(history.getAgentResponse());
                }
            }
            if (pendingImageUrl != null) {
                ImageLoader.INSTANCE.load(binding.imgResult, pendingImageUrl);
            }
        } else {
            LiveDataExtKt.observeNonNull(viewModel.getChatResult(), this, result -> {
                if(result.isEmpty()){
                    ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(),"服务端返回错误，请到ai处询问");
                }
                diagnosisItems = result;
                LogUtils.INSTANCE.d("ljx", "原始结果: " + result);
                selectTab(0);
                return null;
            });
            LiveDataExtKt.observeNonNull(viewModel.getPhotoUriResult(), this, result -> {
                ImageLoader.INSTANCE.load(binding.imgResult, result);
                return null;
            });
            // 监听识别成功后的数据，用于保存到本地数据库
            LiveDataExtKt.observeNonNull(viewModel.getSaveRecordResult(), this, data -> {
                if (data != null && data.diagnosisItems != null && !data.diagnosisItems.isEmpty()) {
                    viewModel.insertLocalDetectionHistory(requireActivity().getApplicationContext(), data);
                }
                return null;
            });
        }
        selectTab(0);
    }

    // 切换标签
    private void selectTab(int index) {
        if (index < 0 || index >= 3) return;
        updateUI(index);
    }

    private void updateUI(int index) {
        if (binding == null) return;

        binding.tvRes1.setSelected(index == 0);
        binding.tvRes2.setSelected(index == 1);
        binding.tvRes3.setSelected(index == 2);

        if (diagnosisItems == null || diagnosisItems.isEmpty()) {
            notHaveResult("暂无解析结果");
            return;
        }

        int totalCount = diagnosisItems.size();
        if (index >= totalCount) {
            notHaveResult("暂无更多解析结果");
            return;
        }

        DiagnosisItem item = diagnosisItems.get(index);
        if (item == null) {
            notHaveResult("暂无更多解析结果");
            return;
        }

        markwon.setMarkdown(binding.tvBing, item.getDiseaseName() != null ? item.getDiseaseName() : "无法识别");

        if (item.getConfidence() > 0) {
            binding.bingGailv.setVisibility(View.VISIBLE);
            int percent = item.getConfidence();
            markwon.setMarkdown(binding.bingGailv, percent + "%");
        } else {
            binding.bingGailv.setVisibility(View.GONE);
        }

        String solutionText = buildSolutionText(item);
        LogUtils.INSTANCE.d("ljx", "防治方案原始内容: " + solutionText);
        binding.tvJiejue.setText(solutionText);
//        markwon.setMarkdown(binding.tvJiejue, solutionText);

        binding.flBing.setVisibility(View.VISIBLE);
        binding.tvBingname.setVisibility(View.VISIBLE);
        binding.tvJiejuefangan.setVisibility(View.VISIBLE);
    }

    private String buildSolutionText(DiagnosisItem item) {
        if (item.getControlPlan() != null && !item.getControlPlan().isEmpty()) {
            return item.getControlPlan();
        }
        return "暂无防治方案";
    }

    private void notHaveResult(String res){
        binding.flBing.setVisibility(View.GONE);
        binding.tvBingname.setVisibility(View.GONE);
        binding.tvJiejuefangan.setVisibility(View.GONE);
        markwon.setMarkdown(binding.tvJiejue,res);
        binding.tvJiejue.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveDataBus.getInstance().with(BusKey.DETECTIONHISTORY).setValue(false);
    }
}
