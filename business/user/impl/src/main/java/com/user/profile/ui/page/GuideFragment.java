package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.user.R;
import com.user.databinding.FragmentGuideRegisterBinding;
import com.user.profile.ui.adapters.GuideFirAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuideFragment extends BaseFragment<FragmentGuideRegisterBinding> {
    FragmentGuideRegisterBinding binding;
    ProfileViewModel viewModel;
    List<String> cropList;

    @NonNull
    @Override
    public FragmentGuideRegisterBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideRegisterBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        viewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);
        List<String> list1 = new ArrayList<>();
        list1.add("小麦");
        list1.add("水稻");
        list1.add("玉米");
        List<String> list2 = new ArrayList<>();
        list2.add("白菜");
        list2.add("萝卜");
        list2.add("莲藕");
        list2.add("芋头");
        list2.add("黄瓜");
        list2.add("番茄");
        list2.add("茄子");
        list2.add("辣椒");
        list2.add("香菇");
        cropList = new ArrayList<>();
        GuideFirAdapter adapter1 = new GuideFirAdapter(0, list1);

        GuideFirAdapter adapter2 = new GuideFirAdapter(0, list2);
        GridLayoutManager layoutManager1 = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 3);
        binding.recy1.setAdapter(adapter1);
        binding.recy1.setLayoutManager(layoutManager1);
        binding.recy2.setAdapter(adapter2);
        binding.recy2.setLayoutManager(layoutManager2);

        adapter1.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            if (v.isSelected()) {
                cropList.remove(list1.get(i));
                v.setSelected(false);
            } else {
                cropList.add(list1.get(i));
                v.setSelected(true);
            }
        });
        adapter2.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            if (v.isSelected()) {
                cropList.remove(list2.get(i));
                v.setSelected(false);
            } else {
                cropList.add(list2.get(i));
                v.setSelected(true);
            }
        });


        binding.btnGuideOk.setOnClickListener(v -> {
//           List<String> list = new ArrayList<>();
//           list.add("水稻");
//           list.add("玉米");
//           list.add("小麦");
            ProfileUpdateRequest request = new ProfileUpdateRequest(null,
                    null, null, null, cropList);
            binding.btnGuideOk.setEnabled(false);
            viewModel.updataProfile(request, "followedCrops");

        });

        LiveDataExtKt.observeNonNull(viewModel.getCropsLivedata(), this, mes -> {
            binding.btnGuideOk.setEnabled(true);
            viewModel.showDialog(getContext(), mes);
//            ToastUtils.INSTANCE.showShort(getActivity().getBaseContext(),mes);
            if ("修改成功".equals(mes)) {
                getParentFragmentManager().popBackStack();
                Log.d("xzr", mes);
            }
            return null;
        });

    }

    @Override
    public void initData() {

    }
}
