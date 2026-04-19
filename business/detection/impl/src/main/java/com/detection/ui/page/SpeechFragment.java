package com.detection.ui.page;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.Manifest;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import com.bytedance.speech.speechengine.SpeechEngine;
import com.bytedance.speech.speechengine.SpeechEngineDefines;
import com.common.base.BaseFragment;
import com.common.speech.SpeechManager;
import com.common.utils.PermissionUtils;
import com.common.utils.ThreadUtils;
import com.common.utils.ToastUtils;
import com.detection.databinding.FragmentSpeechBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SpeechFragment extends BaseFragment<FragmentSpeechBinding> {
    private FragmentSpeechBinding binding;
    private final StringBuilder conversationLog = new StringBuilder();
    private String lastAsrMessage = "";
    private String lastAiMessage = "";
    private boolean sessionActive = false;
    private boolean isListeningPhase = false;
    private boolean isStartingConversation = false;
    private boolean autoStartTriggered = false;
    private boolean ignoreNextEngineStop = false;
    private boolean userFinishedTalking = false;
    private AnimatorSet[] waveAnimatorSets;
    private ObjectAnimator[] volumeAnimators;

    @NonNull
    @Override
    public FragmentSpeechBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSpeechBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        setupSpeechListener();
        updateStatus("正在自动接通语音通话...");
        updateButtonsState(false);
        stopWaveAnimation();
        stopVolumeAnimation();

        binding.aiBack.cvInformationBack.setOnClickListener(v -> hangupAndExit());
        binding.btnStartTalk.setOnClickListener(v -> requestVoicePermissionsAndStart());
        binding.btnFinishTalk.setOnClickListener(v -> finishTalking());
        binding.btnHangup.setOnClickListener(v -> hangupAndExit());
        binding.getRoot().post(() -> {
            if (!autoStartTriggered && isAdded()) {
                autoStartTriggered = true;
                requestVoicePermissionsAndStart();
            }
        });
    }

    @Override
    public void initData() {
    }

    private void requestVoicePermissionsAndStart() {
        if (isStartingConversation) {
            return;
        }
        PermissionUtils.INSTANCE.request(
                this,
                Arrays.asList(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE),
                () -> {
                    startVoiceConversation();
                    return null;
                },
                deniedList -> {
                    if (!isAdded()) {
                        return null;
                    }
                    isStartingConversation = false;
                    updateStatus("缺少录音权限，无法开始语音对话");
                    ToastUtils.INSTANCE.showShort(requireContext().getApplicationContext(), "需要录音权限才能使用语音对话");
                    return null;
                }
        );
    }

    private void setupSpeechListener() {
        SpeechEngine engine = SpeechManager.INSTANCE.getEngine();
        if (engine == null) {
            appendLog("错误：语音引擎未初始化");
            updateStatus("语音引擎未初始化");
            return;
        }
        engine.setListener((type, data, length) ->
                ThreadUtils.INSTANCE.runOnUiThread(() -> handleSpeechMessage(type, data)));
    }

    private void handleSpeechMessage(int type, byte[] data) {
        if (!isAdded() || binding == null) {
            return;
        }
        switch (type) {
            case SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CONNECTION_STARTED:
                sessionActive = true;
                isStartingConversation = false;
                updateStatus("网络连接已建立，正在准备会话...");
                updateButtonsState(true);
                startWaveAnimation();
                appendLog("系统：网络连接已建立");
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_DIALOG_SESSION_STARTED:
                sessionActive = true;
                isStartingConversation = false;
                updateStatus("会话已启动，请开始说话");
                updateButtonsState(true);
                startWaveAnimation();
                appendLog("系统：会话已启动");
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_START:
                sessionActive = true;
                isStartingConversation = false;
                isListeningPhase = true;
                userFinishedTalking = false;
                updateStatus("正在聆听，请说话");
                updateButtonsState(true);
                startWaveAnimation();
                startVolumeAnimation();
                pulseCenterIcon(1.08f);
                appendLog("系统：开始录音");
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_STOP:
                if (ignoreNextEngineStop) {
                    ignoreNextEngineStop = false;
                    appendLog("系统：已清理上一轮语音会话");
                    break;
                }
                isListeningPhase = false;
                updateStatus(userFinishedTalking ? "已结束说话，等待 AI 回复" : "录音已停止，可再次开始通话");
                updateButtonsState(false);
                stopVolumeAnimation();
                startWaveAnimation();
                appendLog(userFinishedTalking ? "系统：已结束说话，等待 AI 回复" : "系统：录音已停止");
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_ERROR:
                sessionActive = false;
                isListeningPhase = false;
                isStartingConversation = false;
                userFinishedTalking = false;
                ignoreNextEngineStop = false;
                updateStatus("语音通话异常");
                updateButtonsState(false);
                stopWaveAnimation();
                stopVolumeAnimation();
                resetCenterIconScale();
                appendLog("错误：" + bytesToString(data));
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_RESPONSE:
                String asrText = parseSpeechPayload(bytesToString(data));
                if (!TextUtils.isEmpty(asrText) && !TextUtils.equals(lastAsrMessage, asrText)) {
                    lastAsrMessage = asrText;
                    pulseCenterIcon(1.12f);
                    appendLog("我：" + asrText);
                }
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CHAT_RESPONSE:
                String aiText = parseSpeechPayload(bytesToString(data));
                if (!TextUtils.isEmpty(aiText) && !TextUtils.equals(lastAiMessage, aiText)) {
                    lastAiMessage = aiText;
                    pulseCenterIcon(1.1f);
                    appendLog("AI：" + aiText);
                }
                break;
            default:
                break;
        }
    }

    private void startVoiceConversation() {
        SpeechEngine engine = SpeechManager.INSTANCE.getEngine();
        if (engine == null) {
            if (!isAdded()) {
                return;
            }
            isStartingConversation = false;
            updateStatus("语音引擎未初始化");
            ToastUtils.INSTANCE.showShort(requireContext().getApplicationContext(), "语音引擎未初始化");
            return;
        }

        isStartingConversation = true;
        isListeningPhase = false;
        userFinishedTalking = false;
        appendLog("系统：开始建立语音通话");
        updateStatus("正在启动语音通话...");
        startWaveAnimation();
        if (sessionActive) {
            ignoreNextEngineStop = true;
            engine.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "");
        }

        String startJson = "{\n" +
                "  \"dialog\": {\n" +
                "    \"bot_name\": \"小农\"\n" +
                "  }\n" +
                "}";
        int startRet = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, startJson);
        if (startRet != SpeechEngineDefines.ERR_NO_ERROR) {
            isStartingConversation = false;
            sessionActive = false;
            updateStatus("启动语音通话失败");
            appendLog("错误：启动引擎失败，返回值 " + startRet);
            updateButtonsState(false);
            stopWaveAnimation();
            return;
        }

        updateButtonsState(true);
        String helloJson = "{\"content\": \"我是小农，有什么病虫害相关的问题都可以问我。\"}";
        int helloRet = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_EVENT_SAY_HELLO, helloJson);
        if (helloRet != SpeechEngineDefines.ERR_NO_ERROR) {
            appendLog("系统：开场白发送失败，返回值 " + helloRet);
        } else {
            appendLog("系统：通话已接通，小农已上线");
        }
    }

    private void finishTalking() {
        SpeechEngine engine = SpeechManager.INSTANCE.getEngine();
        if (engine == null) {
            return;
        }
        int ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_FINISH_TALKING, "");
        if (ret == SpeechEngineDefines.ERR_NO_ERROR) {
            isListeningPhase = false;
            userFinishedTalking = true;
            updateStatus("已结束说话，等待 AI 回复");
            stopVolumeAnimation();
            appendLog("系统：已结束说话，等待 AI 回复");
        } else {
            appendLog("错误：结束说话失败，返回值 " + ret);
        }
    }

    private void hangupAndExit() {
        stopVoiceConversation();
        if (!isAdded()) {
            return;
        }
        getParentFragmentManager().popBackStack();
    }

    private void stopVoiceConversation() {
        SpeechEngine engine = SpeechManager.INSTANCE.getEngine();
        if (engine != null) {
            engine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "");
        }
        sessionActive = false;
        isListeningPhase = false;
        isStartingConversation = false;
        userFinishedTalking = false;
        ignoreNextEngineStop = false;
        if (binding != null) {
            updateButtonsState(false);
            updateStatus("语音通话已结束");
        }
        stopWaveAnimation();
        stopVolumeAnimation();
        resetCenterIconScale();
    }

    private void updateButtonsState(boolean active) {
        if (binding == null) {
            return;
        }
        binding.btnStartTalk.setEnabled(!active && !isStartingConversation);
        binding.btnFinishTalk.setEnabled(active && isListeningPhase);
    }

    private void updateStatus(String status) {
        if (binding == null) {
            return;
        }
        binding.tvStatus.setText(status);
        binding.tvWecan.setText(sessionActive
                ? (isListeningPhase ? "正在聆听中，说完后点击“结束说话”" : "等待 AI 回复中，可稍后重新接通")
                : (isStartingConversation ? "正在自动连接，请稍候..." : "进入页面后会自动接通语音通话"));
    }

    private void appendLog(String message) {
        if (binding == null) {
            return;
        }
        if (conversationLog.length() > 0) {
            conversationLog.append("\n\n");
        }
        conversationLog.append(message);
        binding.tvConversationLog.setText(conversationLog.toString());
        binding.tvConversationLog.post(() -> {
            if (binding == null) {
                return;
            }
            android.view.View parent = (android.view.View) binding.tvConversationLog.getParent().getParent();
            if (parent instanceof android.widget.ScrollView) {
                ((android.widget.ScrollView) parent).fullScroll(android.view.View.FOCUS_DOWN);
            }
        });
    }

    private String bytesToString(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private String parseSpeechPayload(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "";
        }
        try {
            JSONObject jsonObject = new JSONObject(raw);
            String directText = extractTextFromJson(jsonObject);
            return TextUtils.isEmpty(directText) ? raw : directText;
        } catch (Exception ignore) {
            return raw;
        }
    }

    private String extractTextFromJson(JSONObject jsonObject) {
        String[] directKeys = new String[]{"content", "text", "message", "result"};
        for (String key : directKeys) {
            String value = jsonObject.optString(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }

        JSONObject payload = jsonObject.optJSONObject("payload");
        if (payload != null) {
            String payloadText = extractTextFromJson(payload);
            if (!TextUtils.isEmpty(payloadText)) {
                return payloadText;
            }
        }

        JSONArray choices = jsonObject.optJSONArray("choices");
        if (choices != null && choices.length() > 0) {
            JSONObject firstChoice = choices.optJSONObject(0);
            if (firstChoice != null) {
                String choiceText = extractTextFromJson(firstChoice);
                if (!TextUtils.isEmpty(choiceText)) {
                    return choiceText;
                }
                JSONObject message = firstChoice.optJSONObject("message");
                if (message != null) {
                    String messageText = extractTextFromJson(message);
                    if (!TextUtils.isEmpty(messageText)) {
                        return messageText;
                    }
                }
            }
        }

        JSONArray messages = jsonObject.optJSONArray("messages");
        if (messages != null && messages.length() > 0) {
            JSONObject firstMessage = messages.optJSONObject(0);
            if (firstMessage != null) {
                return extractTextFromJson(firstMessage);
            }
        }

        return "";
    }

    private void startWaveAnimation() {
        if (binding == null || waveAnimatorSets != null) {
            return;
        }
        View[] waveViews = new View[]{binding.viewWaveInner, binding.viewWaveMiddle, binding.viewWaveOuter};
        waveAnimatorSets = new AnimatorSet[waveViews.length];
        for (int i = 0; i < waveViews.length; i++) {
            View waveView = waveViews[i];
            waveView.setVisibility(View.VISIBLE);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(waveView, View.SCALE_X, 0.88f, 1.45f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(waveView, View.SCALE_Y, 0.88f, 1.45f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(waveView, View.ALPHA, 0.55f, 0f);
            scaleX.setRepeatCount(ObjectAnimator.INFINITE);
            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
            alpha.setRepeatCount(ObjectAnimator.INFINITE);
            scaleX.setRepeatMode(ObjectAnimator.RESTART);
            scaleY.setRepeatMode(ObjectAnimator.RESTART);
            alpha.setRepeatMode(ObjectAnimator.RESTART);
            scaleX.setDuration(1800);
            scaleY.setDuration(1800);
            alpha.setDuration(1800);
            scaleX.setStartDelay(i * 240L);
            scaleY.setStartDelay(i * 240L);
            alpha.setStartDelay(i * 240L);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.start();
            waveAnimatorSets[i] = animatorSet;
        }
    }

    private void stopWaveAnimation() {
        if (binding == null) {
            waveAnimatorSets = null;
            return;
        }
        if (waveAnimatorSets != null) {
            for (AnimatorSet animatorSet : waveAnimatorSets) {
                if (animatorSet != null) {
                    animatorSet.cancel();
                }
            }
            waveAnimatorSets = null;
        }
        View[] waveViews = new View[]{binding.viewWaveInner, binding.viewWaveMiddle, binding.viewWaveOuter};
        for (View waveView : waveViews) {
            waveView.setAlpha(0f);
            waveView.setScaleX(1f);
            waveView.setScaleY(1f);
        }
    }

    private void startVolumeAnimation() {
        if (binding == null || volumeAnimators != null) {
            return;
        }
        binding.llVolumeBars.setVisibility(View.VISIBLE);
        View[] barViews = new View[]{binding.viewVolumeBar1, binding.viewVolumeBar2, binding.viewVolumeBar3};
        float[][] scales = new float[][]{
                {0.45f, 1.1f, 0.55f},
                {0.65f, 1.35f, 0.6f},
                {0.5f, 1.2f, 0.5f}
        };
        volumeAnimators = new ObjectAnimator[barViews.length];
        for (int i = 0; i < barViews.length; i++) {
            View barView = barViews[i];
            barView.setPivotY(barView.getHeight() == 0 ? 0 : barView.getHeight());
            ObjectAnimator animator = ObjectAnimator.ofFloat(barView, View.SCALE_Y, scales[i]);
            animator.setDuration(420 + i * 60L);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
            volumeAnimators[i] = animator;
        }
    }

    private void stopVolumeAnimation() {
        if (binding == null) {
            volumeAnimators = null;
            return;
        }
        if (volumeAnimators != null) {
            for (ObjectAnimator animator : volumeAnimators) {
                if (animator != null) {
                    animator.cancel();
                }
            }
            volumeAnimators = null;
        }
        binding.llVolumeBars.setVisibility(View.GONE);
        View[] barViews = new View[]{binding.viewVolumeBar1, binding.viewVolumeBar2, binding.viewVolumeBar3};
        for (View barView : barViews) {
            barView.setScaleY(1f);
        }
    }

    private void pulseCenterIcon(float targetScale) {
        if (binding == null) {
            return;
        }
        binding.ivCenterIcon.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(120)
                .withEndAction(() -> {
                    if (binding == null) {
                        return;
                    }
                    binding.ivCenterIcon.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
                })
                .start();
    }

    private void resetCenterIconScale() {
        if (binding == null) {
            return;
        }
        binding.ivCenterIcon.setScaleX(1f);
        binding.ivCenterIcon.setScaleY(1f);
    }

    @Override
    public void onDestroyView() {
        stopVoiceConversation();
        binding = null;
        super.onDestroyView();
    }
}
