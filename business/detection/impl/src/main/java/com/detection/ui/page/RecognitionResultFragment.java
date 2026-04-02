package com.detection.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.common.utils.ImageLoader;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.Utils;
import com.detection.databinding.FragmentRecocgnitionResultBinding;
import com.detection.viewmodel.DetectionViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecognitionResultFragment extends BaseFragment<FragmentRecocgnitionResultBinding> {
    FragmentRecocgnitionResultBinding binding;
    DetectionViewModel viewModel;

    // 存储最多3个结果
    private String[] diseases = new String[3];
    private String[] confidences = new String[3];
    private String[] solutions = new String[3];
    String[] sections;
    private int currentIndex = 0;

    @NonNull
    @Override
    public FragmentRecocgnitionResultBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRecocgnitionResultBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        viewModel = new ViewModelProvider(this).get(DetectionViewModel.class);

        // 设置标签点击事件
        binding.tvRes1.setOnClickListener(v -> selectTab(0));
        binding.tvRes2.setOnClickListener(v -> selectTab(1));
        binding.tvRes3.setOnClickListener(v -> selectTab(2));

        LiveDataExtKt.observeNonNull(viewModel.getChatResult(), this, result -> {
            parseResult(result);
            LogUtils.INSTANCE.d("ljx", "原始结果: " + result);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getPhotoUriResult(), this, uri -> {
            ImageLoader.INSTANCE.load(binding.imgResult, uri);
            return null;
        });
    }

    // 切换标签
    private void selectTab(int index) {
        if (index < 0 || index >= 3) return;
        updateUI(index);
    }

    private void updateUI(int index) {
        if (index < 0 || index >= 3) return;

        binding.tvRes1.setSelected(index == 0);
        binding.tvRes2.setSelected(index == 1);
        binding.tvRes3.setSelected(index == 2);

        // 检查数据是否加载
        if (sections == null || diseases == null) {
            return;
        }

        if (diseases[index] != null) {
            binding.tvBing.setText(diseases[index]);
        }
        if (confidences[index] != null && !confidences[index].isEmpty()) {
            binding.bingGailv.setVisibility(View.VISIBLE);
            binding.bingGailv.setText(confidences[index]);
        } else {
            binding.bingGailv.setVisibility(View.GONE);
        }
        if (solutions[index] != null) {
            binding.tvJiejue.setText(solutions[index]);
        }

        // 检查是否有多个结果
        if (sections.length == 1 && index != 0) {
            notHaveResult("暂无更多解析结果");
        } else if (sections.length == 2 && index == 2) {
            notHaveResult("暂无更多解析结果");
        } else {
            binding.flBing.setVisibility(View.VISIBLE);
            binding.tvBingname.setVisibility(View.VISIBLE);
            binding.tvJiejuefangan.setVisibility(View.VISIBLE);
        }
    }

    private void notHaveResult(String res){
        binding.flBing.setVisibility(View.GONE);
        binding.tvBingname.setVisibility(View.GONE);
        binding.tvJiejuefangan.setVisibility(View.GONE);
        binding.tvJiejue.setText(res);
    }
    private void parseResult(String result) {
        LogUtils.INSTANCE.d("ljx", "原始结果: " + result);

        if (result == null || result.isEmpty()) {
            ToastUtils.INSTANCE.showShort(getActivity().getApplicationContext(), "未获取到识别结果");
            return;
        }
        //为什么要重置
        diseases = new String[3];
        confidences = new String[3];
        solutions = new String[3];
        if(result.contains("无法开展病虫害诊断")){
            sections = new String[3];
            diseases[0] = "无法确认";
            solutions[0] = result;
        }
        else{
            sections = result.split("(?=结果[一二三])");
            for (int i = 0; i < sections.length && i < 3; i++) {
                String section = sections[i].trim();
                if (section.isEmpty()) continue;
                // 提取病害名称
                diseases[i] = Utils.extractDiseaseName(section);
                // 提取置信度
                confidences[i] = extractPercent(section);
                // 提取防治建议
                solutions[i] = extractSolution(section);
            }
        }
        // 显示第一个结果
        updateUI(0);
    }

    // 提取病害名称


    // 提取百分比
    private String extractPercent(String text) {
        int percentIdx = text.indexOf("%");
        if (percentIdx != -1) {
            int i = percentIdx - 1;
            StringBuilder sb = new StringBuilder();
            while (i >= 0 && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) {
                sb.insert(0, text.charAt(i));
                i--;
            }
            if (sb.length() > 0) {
                try {
                    double value = Double.parseDouble(sb.toString());
                    return (int) value + "%";
                } catch (Exception e) {
                    return sb.toString() + "%";
                }
            }
        }
        return "";
    }

    // 提取防治建议
    private String extractSolution(String text) {
        int idx = text.indexOf("防治建议");
        if (idx != -1) {
            String solution = text.substring(idx);
            solution = solution.replaceAll("\\n{3,}", "\n\n");
            return solution;
        }
        return text;
    }


    @Override
    public void initData() {
    }
}
