package com.detection.ui.page;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.Manifest;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.FragmentAiMainBinding;
import com.detection.model.ChatItem;
import com.detection.ui.adapter.ChatAdapter;
import com.detection.viewmodel.DetectionViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.noties.markwon.Markwon;

public class AIMainFragment extends BaseFragment<FragmentAiMainBinding> {

    private ChatAdapter adapter;
    private DetectionViewModel viewModel;
    FragmentAiMainBinding binding;

    List<ChatItem> list;
    String url = null;
    Boolean isKeyboardVisible = false;
    private Boolean ifold;
    private ImagePickerUtil imagePickerUtil;
    ViewTreeObserver.OnGlobalLayoutListener listener;

    public AIMainFragment(Boolean ifold) {
        this.ifold = ifold;
    }

    @NonNull
    @Override
    public FragmentAiMainBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAiMainBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        adapter = new ChatAdapter();

        viewModel = new ViewModelProvider(requireActivity()).get(DetectionViewModel.class);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.rvMessages.setLayoutManager(layoutManager);
//        if (ifold) {
//            showLoading("稍等喔");
//        }
        binding.aiBack.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        list = new ArrayList<>();
        list.add(ChatItem.ai(ChatItem.TYPE_AI, "嗨，我是你的AI小助手！初次见面很开心。我呢，可以回答你的各种关于病虫害的问题。你想问点什么呢？"));
        adapter.setList(list);
        //发送消息
        binding.btnSend.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString();
            if (!input.trim().isEmpty() || url != null) {
                viewModel.recognize(input, url, true);
                adapter.addData(ChatItem.mine(ChatItem.TYPE_USER, input, url));
                adapter.addData(ChatItem.ai(ChatItem.TYPE_AI, "稍等一会..."));
                scrollDialog();
                binding.btnSend.setEnabled(false);
                binding.etInput.setText("");
                showImg("");
                LogUtils.INSTANCE.d("ljx", input + adapter.getItemCount());
                url = null;
            }
            else{
                ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(),"请输入文本");
            }
        });
        //添加图片返回相册url
        if (imagePickerUtil == null) {
            imagePickerUtil = new ImagePickerUtil(getActivity(), uri -> {
                File file1 = FileUtils.INSTANCE.uriToFile(requireActivity().getApplicationContext(), uri, requireActivity().getCacheDir());
                if (file1 != null) {
                    LogUtils.INSTANCE.d("ljx", "file1 path: " + file1.getPath() + ", size: " + file1.length());
                    viewModel.uploadAndRecognizeFromGallery(requireActivity().getApplicationContext(), "", file1);
                    showLoading("加载中...");
                } else {
                    LogUtils.INSTANCE.d("ljx", "file1 is null!");
                }
                return null;
            });
        }
        //取消添加图片
        binding.rvNoimages.setOnClickListener(v -> {
            showImg("");
            url = null;
        });
//        //历史记录的图片返回
//        LiveDataExtKt.observeNonNull(viewModel.getPhotoUriResult(), this, url -> {
//            adapter.addData(ChatItem.mine(ChatItem.TYPE_USER, "这个叶子得了什么病", url));
//            scrollDialog();
//            return null;
//        });
        //我添加的图片
        LiveDataExtKt.observeNonNull(viewModel.getPhotoNewUriResult(), this, url -> {
            ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(), "图片添加成功");
            showImg(url);
            this.url = url;
            return null;
        });
//        //ai返回拍照识别
//        LiveDataExtKt.observeNonNull(viewModel.getChatResult(), this, mes -> {
//            hideLoading();
//            adapter.setData(adapter.getItemCount() - 1,ChatItem.ai(ChatItem.TYPE_AI, mes));
//            scrollDialog();
//            return null;
//        });
        //ai对话返回
        LiveDataExtKt.observeNonNull(viewModel.getChatChatResult(), this, mes -> {
            hideLoading();
            adapter.setData(adapter.getItemCount() - 1,ChatItem.ai(ChatItem.TYPE_AI, mes));
            scrollDialog();
            binding.btnSend.setEnabled(true);
            return null;
        });
        //错误返回
        LiveDataExtKt.observeNonNull(viewModel.getErrorChatMessage(), this, mes -> {
            hideLoading();
            ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(), mes);
            if ("AI连接错误".equals(mes) || "AI 识别失败，请重试".equals(mes)) {
                binding.btnSend.setEnabled(true);
                adapter.setData(adapter.getItemCount() - 1,ChatItem.ai(ChatItem.TYPE_AI, mes));
                scrollDialog();
            }
            return null;
        });

        //权限
        binding.btnXc.setOnClickListener(v -> {
            PermissionUtils.INSTANCE.request(this, Arrays.asList(Manifest.permission.CAMERA),
                    () -> {
                        imagePickerUtil.getStorage();
                        return null;
                    },
                    deniedList -> {
                        ToastUtils.INSTANCE.showShort(getActivity().getApplicationContext(), "您拒绝了权限，功能无法使用");
                        return null;
                    });
        });
        binding.btnAddImage.setOnClickListener(v -> {
            PermissionUtils.INSTANCE.request(this, Arrays.asList(Manifest.permission.CAMERA),
                    () -> {
                        imagePickerUtil.getCamera();
                        return null;
                    },
                    deniedList -> {
                        Toast.makeText(requireContext(), "您拒绝了权限，功能无法使用", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        });
        setupKeyboardListener();
        binding.rvMessages.setAdapter(adapter);
    }

    //点击输出框，可以弹出软键盘->底部要跟着网上弹。点击空白地方要弹下；
    private void setupKeyboardListener() {
        final View rootView = binding.getRoot();

        // 点击聊天列表收起键盘
        binding.rvMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });


         listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // 键盘弹出
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        // 调整 RecyclerView padding，避免输入框被键盘遮挡
                        binding.inputContainer.setPadding(
                                binding.inputContainer.getPaddingLeft(),
                                binding.inputContainer.getPaddingTop(),
                                binding.inputContainer.getPaddingRight(),
                                keypadHeight + 20
                        );
                        // 延迟滚动，等布局完成后再滚
                        binding.rvMessages.postDelayed(() -> scrollDialog(), 150);
                    }
                } else {
                    // 键盘收起
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        binding.inputContainer.setPadding(
                                binding.inputContainer.getPaddingLeft(),
                                binding.inputContainer.getPaddingTop(),
                                binding.inputContainer.getPaddingRight(),
                                20
                        );
                    }
                }
            }
        };

        // 通过根布局获得布局数，从而得到整个布局的监听；
        // 得到整个页面除了键盘以外的可见区域，和，整个根布局页面高度，从而得到键盘高度，从而实现监听；
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private boolean hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void scrollDialog(){
        binding.rvMessages.post(() -> {
            if (binding.rvMessages == null || adapter == null || adapter.getItemCount() == 0) return;
            // 判断内容是否溢出（可滚动）
            boolean canScroll = binding.rvMessages.computeVerticalScrollRange() >binding.rvMessages.getHeight();
            if (canScroll) {
                LinearLayoutManager lm = (LinearLayoutManager) binding.rvMessages.getLayoutManager();
                if (lm != null) lm.setStackFromEnd(true);
                binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
    private void showImg(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.cvImg.setVisibility(View.VISIBLE);
            ImageLoader.INSTANCE.load(binding.rvImages, imageUrl);
            hideLoading();
        } else {
            binding.cvImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 安全移除 ViewTreeObserver 监听器，防止内存泄漏
        if (listener != null && binding != null && binding.getRoot() != null) {
            ViewTreeObserver vto = binding.getRoot().getViewTreeObserver();
            if (vto.isAlive()) {
                vto.removeOnGlobalLayoutListener(listener);
            }
        }
        listener = null;
        if (imagePickerUtil != null) {
            imagePickerUtil.release();
            imagePickerUtil = null;
        }
        binding = null;  // 清除 binding 引用，避免泄漏
    }

    @Override
    public void initData() {

    }
}
