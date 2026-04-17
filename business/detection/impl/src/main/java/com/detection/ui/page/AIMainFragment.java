package com.detection.ui.page;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.Manifest;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.speech.VoiceRecognitionCallback;
import com.common.speech.VoiceRecognitionManager;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.ThreadUtils;
import com.common.utils.ToastUtils;
import com.detection.R;
import com.detection.databinding.FragmentAiMainBinding;
import com.detection.model.ChatItem;
import com.detection.ui.adapter.ChatAdapter;
import com.detection.viewmodel.DetectionViewModel;
import com.detection.viewmodel.DetectionViewModelFactory;
import com.permissionx.guolindev.callback.RequestCallback;

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
    private String originalInputText = "";
    Boolean isRecording;
    private StringBuilder voiceInputBuffer = new StringBuilder();
    private ImagePickerUtil imagePickerUtil;
    ViewTreeObserver.OnGlobalLayoutListener listener;

    // 保存 VoiceRecognitionCallback 引用，用于清理，防止内存泄漏
    private VoiceRecognitionCallback voiceCallback;

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
        // 在 initView 开始时安全获取 binding
        binding = getBindingSafe();
        if (binding == null) {
            return; // 防御性检查
        }
        adapter = new ChatAdapter();

        if (!isAdded() || getActivity() == null) return;
        binding.consYuyin.setVisibility(View.GONE);
        DetectionViewModelFactory factory = new DetectionViewModelFactory(
            requireActivity().getApplication()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(DetectionViewModel.class);
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
                binding.aiBack.cvInformationBack.setEnabled(false);
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
                Context ctx = getContext();
                if (ctx != null) ToastUtils.INSTANCE.showShort(ctx, "请输入文本");
            }
        });
        //添加图片返回相册url
        if (imagePickerUtil == null) {
            imagePickerUtil = new ImagePickerUtil(getActivity(), uri -> {
                if (!isAdded() || getActivity() == null) return null;
                Context appCtx = getActivity().getApplicationContext();
                File file1 = FileUtils.INSTANCE.uriToFile(appCtx, uri, getActivity().getCacheDir());
                if (file1 != null) {
                    LogUtils.INSTANCE.d("ljx", "file1 path: " + file1.getPath() + ", size: " + file1.length());
                    viewModel.uploadAndRecognizeFromGallery(appCtx, "", file1);
                    showLoading("加载中...");
                } else {
                    LogUtils.INSTANCE.d("ljx", "file1 is null!");
                }
                return null;
            });
        }
        binding.btnYuyin.setOnClickListener(v->{
            hideKeyboard();
            ThreadUtils.INSTANCE.runOnUiThreadDelayed(()->{
                binding.consYuyin.setVisibility(View.VISIBLE);
            },50);
            originalInputText = binding.etInput.getText().toString();
        });
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
            if (!isAdded() || getActivity() == null) return null;
            ToastUtils.INSTANCE.showShort(getActivity().getApplicationContext(), "图片添加成功");
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
            binding.aiBack.cvInformationBack.setEnabled(true);
            adapter.setData(adapter.getItemCount() - 1,ChatItem.ai(ChatItem.TYPE_AI, mes));
            scrollDialog();
            binding.btnSend.setEnabled(true);
            return null;
        });
        //错误返回
        LiveDataExtKt.observeNonNull(viewModel.getErrorChatMessage(), this, mes -> {
            if (!isAdded() || getActivity() == null) return null;
            hideLoading();
            binding.aiBack.cvInformationBack.setEnabled(true);
            ToastUtils.INSTANCE.showShort(getActivity().getApplicationContext(), mes);
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
                        if (!isAdded() || getActivity() == null) return null;
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
                        if (!isAdded() || getActivity() == null) return null;
                        Context ctx = getContext();
                        if (ctx != null) Toast.makeText(ctx, "您拒绝了权限，功能无法使用", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        });
        setupKeyboardListener();
        binding.rvMessages.setAdapter(adapter);
        setupVoiceInput();
    }

    private void setupVoiceInput() {
        FragmentAiMainBinding currentBinding = getBindingSafe();
        if (currentBinding == null || currentBinding.ivYuyin == null) return;

        currentBinding.ivYuyin.setOnTouchListener((v, event) -> {
            ImageView iv = (ImageView) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    iv.setImageResource(R.drawable.ic_ai_yuyining);
                    startVoiceRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    iv.setImageResource(R.drawable.ic_ai_yuyinbtn);
                    stopVoiceRecording();
                    return true;
            }
            return false;
        });
    }

    private void stopVoiceRecording() {
        VoiceRecognitionManager.INSTANCE.stopListening();
        isRecording = false;
    }

    private void startVoiceRecording() {
        if (!isAdded() || getContext() == null) return;
        if (!VoiceRecognitionManager.INSTANCE.isInitialized()) {
            ToastUtils.INSTANCE.showShort(getContext(), "语音识别未初始化");
            return;
        }

        if (getActivity() == null) return;
        PermissionUtils.INSTANCE.requestRecordAudio((AppCompatActivity)getActivity(), new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                if (!isAdded() || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded() || getActivity() == null) return;
                    if (allGranted) {
                        isRecording = true;
                        voiceInputBuffer.append(originalInputText);
                        voiceInputBuffer.setLength(0);
                        Context ctx = getContext();
                        if (ctx != null) ToastUtils.INSTANCE.showShort(ctx, "开始说话...");

                        voiceCallback = new VoiceRecognitionCallback() {
                            @Override
                            public void onBeginOfSpeech() {
                            }

                            @Override
                            public void onEndOfSpeech() {
                            }

                            @Override
                            public void onPartialResult(String text) {
                                if (!isAdded() || getActivity() == null) return;
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getActivity() == null) return;
                                    if (text != null && !text.isEmpty()) {
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(originalInputText);
                                        voiceInputBuffer.append(text);
                                        originalInputText = originalInputText+text;
                                        updateSearchText();
                                    }
                                });
                            }

                            @Override
                            public void onFinalResult(String text) {
                                if (!isAdded() || getActivity() == null) return;
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getActivity() == null) return;
                                    String existingText = voiceInputBuffer.toString();
                                    if (existingText.isEmpty() && text != null && !text.isEmpty()) {
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(originalInputText);
                                        voiceInputBuffer.append(text);
                                    }
                                    updateSearchText();
                                    isRecording = false;
                                    FragmentAiMainBinding binding = getBindingSafe();
                                    if (binding != null && binding.ivYuyin != null) {
                                        binding.ivYuyin.setImageResource(R.drawable.ic_ai_yuyinbtn);
                                    }

                                    originalInputText = voiceInputBuffer.toString();
                                });
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                if (!isAdded() || getActivity() == null) return;
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || getActivity() == null) return;
                                    isRecording = false;
                                    Context ctx1 = getContext();
                                    if (ctx1 != null) ToastUtils.INSTANCE.showShort(ctx1, "识别失败: " + errorMsg);
                                    close();
                                });
                            }

                            @Override
                            public void onVolumeChanged(int volume) {
                            }
                        };

                        VoiceRecognitionManager.INSTANCE.setCallback(voiceCallback);

                        Context listenCtx = getContext();
                        if (listenCtx != null) VoiceRecognitionManager.INSTANCE.startListening(listenCtx);
                    } else {
                        Context ctx2 = getContext();
                        if (ctx2 != null) ToastUtils.INSTANCE.showShort(ctx2, "需要麦克风权限才能使用语音输入");
                        FragmentAiMainBinding binding = getBindingSafe();
                        if (binding != null && binding.ivYuyin != null) {
                            binding.ivYuyin.setImageResource(R.drawable.ic_ai_yuyinbtn);
                        }
                    }
                });
            }
        });
    }
    private void close(){
        if (isRecording) {
            VoiceRecognitionManager.INSTANCE.stopListening();
            isRecording = false;
        }
    }

    private void updateSearchText() {
        FragmentAiMainBinding currentBinding = getBindingSafe();
        if (currentBinding == null || currentBinding.etInput == null) return;

        EditText et = currentBinding.etInput;
        et.setText(voiceInputBuffer.toString());
        et.setSelection(voiceInputBuffer.length());
    }

    //点击输出框，可以弹出软键盘->底部要跟着网上弹。点击空白地方要弹下；
    private void setupKeyboardListener() {
        final View rootView = getBindingSafe() != null ? getBindingSafe().getRoot() : null;
        if (rootView == null) return;

        // 使用 getBindingSafe() 获取当前 binding，并进行 null 检查
        FragmentAiMainBinding currentBinding = getBindingSafe();
        if (currentBinding == null || currentBinding.rvMessages == null) return;

        // 点击聊天列表收起键盘
        currentBinding.rvMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });


         listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 使用 getBindingSafe() 获取当前 binding，并进行 null 检查
                FragmentAiMainBinding bindingInCallback = getBindingSafe();
                if (bindingInCallback == null || bindingInCallback.inputContainer == null) {
                    return; // Fragment 已销毁或 binding 已被清理，直接返回
                }

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // 键盘弹出
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        bindingInCallback.consYuyin.setVisibility(View.GONE);
                        // 调整 RecyclerView padding，避免输入框被键盘遮挡
                        bindingInCallback.inputContainer.setPadding(
                                bindingInCallback.inputContainer.getPaddingLeft(),
                                bindingInCallback.inputContainer.getPaddingTop(),
                                bindingInCallback.inputContainer.getPaddingRight(),
                                keypadHeight + 0
                        );
                        // 延迟滚动，等布局完成后再滚
                        bindingInCallback.rvMessages.postDelayed(() -> scrollDialog(), 150);
                    }
                } else {
                    // 键盘收起
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        bindingInCallback.inputContainer.setPadding(
                                bindingInCallback.inputContainer.getPaddingLeft(),
                                bindingInCallback.inputContainer.getPaddingTop(),
                                bindingInCallback.inputContainer.getPaddingRight(),
                                0
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
        if (getActivity() == null) return false;
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            Context ctx = getContext();
            if (ctx == null) return false;
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void scrollDialog(){
        // 使用 getBindingSafe() 获取当前 binding，并进行 null 检查
        FragmentAiMainBinding currentBinding = getBindingSafe();
        if (currentBinding == null || currentBinding.rvMessages == null) return;

        currentBinding.rvMessages.post(() -> {
            FragmentAiMainBinding bindingRef = getBindingSafe();
            if (bindingRef == null || bindingRef.rvMessages == null || adapter == null || adapter.getItemCount() == 0) return;
            // 判断内容是否溢出（可滚动）
            boolean canScroll = bindingRef.rvMessages.computeVerticalScrollRange() > bindingRef.rvMessages.getHeight();
            if (canScroll) {
                LinearLayoutManager lm = (LinearLayoutManager) bindingRef.rvMessages.getLayoutManager();
                if (lm != null) lm.setStackFromEnd(true);
                bindingRef.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
    private void showImg(String imageUrl) {
        FragmentAiMainBinding currentBinding = getBindingSafe();
        if (currentBinding == null) return;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            currentBinding.cvImg.setVisibility(View.VISIBLE);
            ImageLoader.INSTANCE.load(currentBinding.rvImages, imageUrl);
            hideLoading();
        } else {
            currentBinding.cvImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        // 先获取当前 binding（用于安全清理）
        FragmentAiMainBinding currentBinding = getBindingSafe();

        // 安全移除 ViewTreeObserver 监听器，防止内存泄漏
        if (listener != null && currentBinding != null && currentBinding.getRoot() != null) {
            ViewTreeObserver vto = currentBinding.getRoot().getViewTreeObserver();
            if (vto.isAlive()) {
                vto.removeOnGlobalLayoutListener(listener);
            }
        }
        listener = null;

        // 清理 ImagePickerUtil
        if (imagePickerUtil != null) {
            imagePickerUtil.release();
            imagePickerUtil = null;
        }

        // 清理 VoiceRecognitionCallback 引用
        if (voiceCallback != null) {
            VoiceRecognitionManager.INSTANCE.setCallback(null);
            voiceCallback = null;
        }

        // 清理适配器
        if (adapter != null) {
            adapter.setList(null);
            adapter = null;
        }

        // 清理 ChatItem 列表
        if (list != null) {
            list.clear();
            list = null;
        }

        // 最后清除 binding 引用，避免泄漏
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VoiceRecognitionManager.INSTANCE.stopListening();
        VoiceRecognitionManager.INSTANCE.setCallback(null);
    }

    @Override
    public void initData() {

    }
}
