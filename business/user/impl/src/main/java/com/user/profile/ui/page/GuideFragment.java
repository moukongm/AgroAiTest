package com.user.profile.ui.page;

import static android.app.ProgressDialog.show;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.user.databinding.FragmentGuideRegisterBinding;
import com.user.profile.Utils;
import com.user.login.data.UserStorageConstant;
import com.user.profile.model.GuideMultiItem;
import com.user.profile.ui.adapters.GuideFirAdapter;
import com.user.profile.ui.adapters.GuideMuiltiAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuideFragment extends BaseFragment<FragmentGuideRegisterBinding> {
    ProfileViewModel viewModel;
    Set<String> cropSet;
    private boolean isStandaloneMode = false;
    private String returnTo = null;
    private GuideFirAdapter adapter1;
    private GuideFirAdapter adapter2;
    private GuideMuiltiAdapter adapter;
    private List<GuideMultiItem> list3;

    @NonNull
    @Override
    public FragmentGuideRegisterBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideRegisterBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        Bundle args = getArguments();
        returnTo = args != null ? args.getString("returnTo") : null;

        if (getParentFragment() == null && "guide".equals(returnTo)) {
            isStandaloneMode = true;
        }

        if (isStandaloneMode) {
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        } else if(requireActivity() instanceof EditProfileActivity){
            viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        } else {
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        }

        // 只有从发帖页(post)进入时才显示返回按钮
        getBinding().btnBack.setVisibility("post".equals(returnTo) ? View.VISIBLE : View.GONE);

        getBinding().btnBack.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;
            if ("post".equals(returnTo)) {
                Bundle result = new Bundle();
                result.putString("selected_crop", TextUtils.join(",", cropSet));
                requireActivity().getSupportFragmentManager().setFragmentResult("crop_select_result", result);
            }
            requireActivity().finish();
        });

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
        list3 = new ArrayList<>();
        list3.add(new GuideMultiItem(GuideMultiItem.TYPE_EDIT, null));
        list3.add(new GuideMultiItem(GuideMultiItem.TYPE_ADD, null));

        // 只有从发帖页(post)进入时才需要保存上次选中状态，其他页面调用网络接口即可
        if ("post".equals(returnTo)) {
            String preselectedCrops = getArguments() != null ? getArguments().getString("selected_crops") : null;
            if (!TextUtils.isEmpty(preselectedCrops)) {
                cropSet = new HashSet<>(Arrays.asList(preselectedCrops.split(",")));
            } else {
                cropSet = new HashSet<>();
            }
        } else {
            cropSet = new HashSet<>();
        }

        adapter1 = new GuideFirAdapter(0, list1);
        adapter2 = new GuideFirAdapter(0, list2);
        adapter = new GuideMuiltiAdapter();
        adapter.setList(list3);

        GridLayoutManager layoutManager1 = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager3 = new GridLayoutManager(getContext(), 3);
        getBinding().recy1.setAdapter(adapter1);
        getBinding().recy1.setLayoutManager(layoutManager1);
        getBinding().recy2.setAdapter(adapter2);
        getBinding().recy2.setLayoutManager(layoutManager2);
        getBinding().recyAdd.setAdapter(adapter);
        getBinding().recyAdd.setLayoutManager(layoutManager3);

        // 只有从发帖页(post)进入时才需要同步选中状态
        if ("post".equals(returnTo) && cropSet != null && !cropSet.isEmpty()) {
            adapter1.setSelectedItems(new ArrayList<>(cropSet));
            adapter2.setSelectedItems(new ArrayList<>(cropSet));
            adapter.setSelectedItems(new ArrayList<>(cropSet));
        }

        adapter.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            if (!isAdded() || getActivity() == null) return;
            Log.d("ljxljxljx", "hhh");
            if (i == list3.size() - 2) {
            } else if (i == list3.size() - 1) {
                if(list3.size()>5){
                    Toast.makeText(requireActivity().getApplicationContext() ,"最多添加4个作物哦", Toast.LENGTH_SHORT).show();
                }else{
                    LogUtils.INSTANCE.d("klklkl",list3.size()+"");
                    GuideMultiItem editItem = list3.get(list3.size() - 2);
                    String inputText = editItem.getEditText();
                    Boolean ifEuple = false;
                    for (int i1 = 0; i1 < list3.size()-2; i1++) {
                        LogUtils.INSTANCE.d("klklkl",i1+"op"+list3.get(i1).getNewCrop()+"kl"+inputText);
                        if(list3.get(i1).getNewCrop()!=null && list3.get(i1).getNewCrop().equals(inputText)) {
                            ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(),"不能重复自定义作物！");
                            ifEuple = true;
                            break;
                        }
                    }
                    if(!ifEuple){
                        if (inputText != null && !inputText.trim().isEmpty()) {
                            editItem.setEditText("");
                            list3.add(list3.size() - 2, new GuideMultiItem(GuideMultiItem.TYPE_NEW, inputText.trim()));
                            adapter.setList(new ArrayList<>(list3));
                        }
                    }

                }
            } else {
                String crop = list3.get(i).getNewCrop();
                if (v.isSelected()) {
                    cropSet.remove(crop);
                } else {
                    cropSet.add(crop);
                }
                v.setSelected(!v.isSelected());
                adapter.setSelectedItems(new ArrayList<>(cropSet));
            }
        });

        adapter1.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            String crop = list1.get(i);
            if (v.isSelected()) {
                cropSet.remove(crop);
            } else {
                cropSet.add(crop);
            }
            v.setSelected(!v.isSelected());
            adapter.setSelectedItems(new ArrayList<>(cropSet));
        });
        adapter2.setOnItemClickListener((baseQuickAdapter, v, i) -> {
            String crop = list2.get(i);
            if (v.isSelected()) {
                cropSet.remove(crop);
            } else {
                cropSet.add(crop);
            }
            v.setSelected(!v.isSelected());
            adapter.setSelectedItems(new ArrayList<>(cropSet));
        });

        getBinding().btnGuideOk.setOnClickListener(v -> {
            ProfileUpdateRequest request = new ProfileUpdateRequest(null,
                    null, null, null, new ArrayList<>(cropSet));
            getBinding().btnGuideOk.setEnabled(false);
            // 只有从发帖页(post)进入时才保存自定义作物到本地
            if ("post".equals(returnTo)) {
                saveCustomCrops();
                return;
            }
            viewModel.updataProfile(request, "followedCrops");
        });

        LiveDataExtKt.observeNonNull(viewModel.getCropsLivedata(), this, mes -> {
            getBinding().btnGuideOk.setEnabled(true);
            if (!isAdded() || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
                return null;
            }
            if ("修改成功".equals(mes)) {
                if (isStandaloneMode || "guide".equals(returnTo) || "login".equals(returnTo)) {
                    // 独立模式/从引导页进入/从登录页进入 -> 弹提示后关闭 Activity
                    Utils.showDialog(getActivity(), mes);
                    requireActivity().finish();
                }else {
                    // 从主页进入（如 ProfileFragment）-> 弹提示后 popBackStack
                    Utils.showDialog(requireActivity(), mes);
                    getParentFragmentManager().popBackStack();
                }
                Log.d("xzr", mes);
            } else {
                // 修改失败时，所有场景均弹提示
                Utils.showDialog(getActivity(), mes);
            }
            return null;
        });
    }

    @Override
    public void initData() {
        // 只有从发帖页(post)进入时才加载自定义作物
        if ("post".equals(returnTo)) {
            loadCustomCrops();
        }
    }

    private void loadCustomCrops() {
        String customCropsStr = UserStorageConstant.getCustomCrops();
        if (!TextUtils.isEmpty(customCropsStr)) {
            List<String> savedCrops = Arrays.asList(customCropsStr.split(","));
            for (String crop : savedCrops) {
                boolean exists = false;
                for (int i = 0; i < list3.size(); i++) {
                    if (crop.equals(list3.get(i).getNewCrop())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    list3.add(list3.size() - 2, new GuideMultiItem(GuideMultiItem.TYPE_NEW, crop));
                }
            }
            adapter.setList(new ArrayList<>(list3));
        }
    }

    private void saveCustomCrops() {
        if (!isAdded() || getActivity() == null) return;
        List<String> newCrops = new ArrayList<>();
        for (int i = 0; i < list3.size() - 2; i++) {
            GuideMultiItem item = list3.get(i);
            if (item.getNewCrop() != null && !item.getNewCrop().isEmpty()) {
                newCrops.add(item.getNewCrop());
            }
        }
        String cropsToSave = TextUtils.join(",", newCrops);
        UserStorageConstant.saveCustomCrops(cropsToSave);
        // 从发帖页进入 -> 直接返回结果关闭 Activity，不弹 Dialog
        // 避免 Dialog 持有 Activity Window 引用，在 finish() 后延迟 dismiss 时崩溃
        Bundle result = new Bundle();
        result.putString("selected_crop", TextUtils.join(",", cropSet));
        requireActivity().getSupportFragmentManager().setFragmentResult("crop_select_result", result);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        // 清理所有引用，防止内存泄漏
        viewModel = null;
        adapter = null;
        adapter1 = null;
        adapter2 = null;
        list3 = null;
        cropSet = null;
        returnTo = null;
        super.onDestroyView();
    }
}
