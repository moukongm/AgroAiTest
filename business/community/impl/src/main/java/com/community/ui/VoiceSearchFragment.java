package com.community.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.speech.VoiceRecognitionCallback;
import com.common.speech.VoiceRecognitionManager;
import com.common.utils.PermissionUtils;
import com.common.utils.ToastUtils;
import com.community.R;
import com.community.databinding.FragmentVoiceSearchBinding;

import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

@Route(path = RouterPath.COMMUNITY_VOICE_SEARCH)
public class VoiceSearchFragment extends BaseFragment<FragmentVoiceSearchBinding> {

    public static final String KEY_VOICE_SEARCH_RESULT = "voice_search_result";

    private boolean isRecording = false;
    private StringBuilder voiceInputBuffer = new StringBuilder();

    public static final String ACTION_VOICE_SEARCH = "com.community.action.VOICE_SEARCH";

    @Override
    public FragmentVoiceSearchBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentVoiceSearchBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        getBinding().icBack.setOnClickListener(v -> close());

        setupVoiceInput();
        setupBlurView();
    }

    @Override
    public void initData() {
    }

    private void setupBlurView() {
        BlurView blurView = getBinding().blurMask;
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);

        float radius = 15f;

        blurView.setupWith(rootView, new RenderScriptBlur(requireContext()))
                .setBlurRadius(radius)
                .setOverlayColor(0x40000000);
    }

    private void setupVoiceInput() {
        getBinding().icVoiceInput.setOnTouchListener((v, event) -> {
            ImageView iv = (ImageView) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    iv.setImageResource(R.drawable.ic_voice_input_ing);
                    startVoiceRecording();
                    getBinding().icInput.setVisibility(View.VISIBLE);
                    AnimationDrawable animation = (AnimationDrawable) getResources().getDrawable(R.drawable.voice_anim, null);
                    getBinding().icInput.setImageDrawable(animation);
                    if (animation != null && !animation.isRunning()) {
                        animation.start();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    iv.setImageResource(R.drawable.ic_voice_input);
                    stopVoiceRecording();
                    return true;
            }
            return false;
        });
    }

    private void startVoiceRecording() {
        if (!VoiceRecognitionManager.INSTANCE.isInitialized()) {
            ToastUtils.INSTANCE.showShort(requireContext(), "语音识别未初始化");
            return;
        }

        PermissionUtils.INSTANCE.requestRecordAudio((AppCompatActivity) getActivity(), new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                requireActivity().runOnUiThread(() -> {
                    if (allGranted) {
                        isRecording = true;
                        voiceInputBuffer.setLength(0);
                        ToastUtils.INSTANCE.showShort(requireContext(), "开始说话...");

                        VoiceRecognitionManager.INSTANCE.setCallback(new VoiceRecognitionCallback() {
                            @Override
                            public void onBeginOfSpeech() {
                            }

                            @Override
                            public void onEndOfSpeech() {
                            }

                            @Override
                            public void onPartialResult(String text) {
                                requireActivity().runOnUiThread(() -> {
                                    if (text != null && !text.isEmpty()) {
                                        // 科大讯飞 onPartialResult 返回实时片段结果（非累积）
                                        // 先清空之前的内容再加新的，避免重复
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(text);
                                        updateSearchText();
                                    }
                                });
                            }

                            @Override
                            public void onFinalResult(String text) {
                                requireActivity().runOnUiThread(() -> {
                                    // onFinalResult 只返回标点符号确认，最终结果已在 onPartialResult 累积
                                    // 如果有缓存内容，则保留；如果没有，则使用返回的文本
                                    String existingText = voiceInputBuffer.toString();
                                    if (existingText.isEmpty() && text != null && !text.isEmpty()) {
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(text);
                                    }
                                    updateSearchText();
                                    isRecording = false;
                                    String finalText = voiceInputBuffer.toString().trim();
                                    // 去除末尾的标点符号
                                    if (!finalText.isEmpty()) {
                                        finalText = finalText.replaceAll("[。！？，、；：]$", "");
                                        performSearch(finalText);
                                    } else {
                                        getBinding().icInput.setVisibility(View.GONE);
                                        getBinding().icVoiceInput.setImageResource(R.drawable.ic_voice_input);
                                    }
                                });
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                requireActivity().runOnUiThread(() -> {
                                    isRecording = false;
                                    ToastUtils.INSTANCE.showShort(requireContext(), "识别失败: " + errorMsg);
                                    close();
                                });
                            }

                            @Override
                            public void onVolumeChanged(int volume) {
                            }
                        });

                        VoiceRecognitionManager.INSTANCE.startListening(requireContext());
                    } else {
                        ToastUtils.INSTANCE.showShort(requireContext(), "需要麦克风权限才能使用语音输入");
                        getBinding().icVoiceInput.setImageResource(R.drawable.ic_voice_input);
                    }
                });
            }
        });
    }

    private void stopVoiceRecording() {
        VoiceRecognitionManager.INSTANCE.stopListening();
        isRecording = false;
    }

    private void updateSearchText() {
        EditText et = getBinding().etSearch;
        et.setText(voiceInputBuffer.toString());
        et.setSelection(voiceInputBuffer.length());
    }

    private void performSearch(String keyword) {
        Bundle result = new Bundle();
        result.putString(KEY_VOICE_SEARCH_RESULT, keyword);
        getParentFragmentManager().setFragmentResult(ACTION_VOICE_SEARCH, result);
        close();
    }

    private void close() {
        if (isRecording) {
            VoiceRecognitionManager.INSTANCE.stopListening();
            isRecording = false;
        }
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRecording) {
            stopVoiceRecording();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VoiceRecognitionManager.INSTANCE.stopListening();
        VoiceRecognitionManager.INSTANCE.setCallback(null);
    }
}
