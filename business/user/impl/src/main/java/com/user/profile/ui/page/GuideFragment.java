package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.user.databinding.FragmentGuideRegisterBinding;
import com.user.profile.model.GuideMultiItem;
import com.user.profile.ui.adapters.GuideFirAdapter;
import com.user.profile.ui.adapters.GuideMuiltiAdapter;
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
        Fragment parent = requireParentFragment();
        if (parent instanceof ProfileFragment) {
            viewModel = new ViewModelProvider(parent).get(ProfileViewModel.class);
        } else {
            // 兼容：尝试从爷爷辈获取
            viewModel = new ViewModelProvider(parent.requireParentFragment()).get(ProfileViewModel.class);
        }

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
        List<GuideMultiItem> list3 = new ArrayList<>();
        list3.add(new GuideMultiItem(GuideMultiItem.TYPE_EDIT, null));
        list3.add(new GuideMultiItem(GuideMultiItem.TYPE_ADD, null));
        cropList = new ArrayList<>();

        GuideFirAdapter adapter1 = new GuideFirAdapter(0, list1);
        GuideFirAdapter adapter2 = new GuideFirAdapter(0, list2);
        GuideMuiltiAdapter adapter = new GuideMuiltiAdapter();
        adapter.setList(list3);

        GridLayoutManager layoutManager1 = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager3 = new GridLayoutManager(getContext(), 3);
        binding.recy1.setAdapter(adapter1);
        binding.recy1.setLayoutManager(layoutManager1);
        binding.recy2.setAdapter(adapter2);
        binding.recy2.setLayoutManager(layoutManager2);
        binding.recyAdd.setAdapter(adapter);
        binding.recyAdd.setLayoutManager(layoutManager3);

        adapter.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            Log.d("ljxljxljx", "hhh");
            if (i == list3.size() - 2) {
            } else if (i == list3.size() - 1) {
                LogUtils.INSTANCE.d("-1");
                GuideMultiItem editItem = list3.get(list3.size() - 2);
                String inputText = editItem.getEditText();
                if (inputText != null && !inputText.trim().isEmpty()) {
                    editItem.setEditText("");
                    list3.add(list3.size() - 2, new GuideMultiItem(GuideMultiItem.TYPE_NEW, inputText.trim()));
                    adapter.setList(new ArrayList<>(list3));
                }
            } else {
                if (v.isSelected()) {
                    cropList.remove(list3.get(i).getNewCrop());
                    v.setSelected(false);
                } else {
                    cropList.add(list3.get(i).getNewCrop());
                    v.setSelected(true);
                }
            }
        });

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
            ProfileUpdateRequest request = new ProfileUpdateRequest(null,
                    null, null, null, cropList);
            binding.btnGuideOk.setEnabled(false);
            viewModel.updataProfile(request, "followedCrops");
        });

        LiveDataExtKt.observeNonNull(viewModel.getCropsLivedata(), this, mes -> {
            binding.btnGuideOk.setEnabled(true);
            viewModel.showDialog(getContext(), mes);
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
