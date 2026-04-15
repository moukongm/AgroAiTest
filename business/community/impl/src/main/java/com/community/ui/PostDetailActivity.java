package com.community.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.speech.VoiceRecognitionCallback;
import com.common.speech.VoiceRecognitionManager;
import com.common.utils.PermissionUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.permissionx.guolindev.callback.RequestCallback;

import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.ImageLoader;
import com.common.utils.ToastUtils;
import com.community.R;
import com.community.databinding.ActivityPostDetailBinding;
import com.community.ui.adapter.PostDetailCommentAdapter;
import com.community.ui.adapter.PostDetailImageAdapter;
import com.community.viewmodel.PostDetailViewModel;
import com.agri.pest.client.model.response.PostResponseDto;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.target.Target;
import coil.request.ImageRequest;
import coil.transform.CircleCropTransformation;

@Route(path = RouterPath.COMMUNITY_POST_DETAIL)
public class PostDetailActivity extends BaseActivity<ActivityPostDetailBinding> {

    private PostDetailViewModel viewModel;
    private PostDetailImageAdapter imageAdapter;
    private PostDetailCommentAdapter commentAdapter;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    public static final String EXTRA_POST_ID = "post_id";

    private long currentPostId = -1;
    private int maxImageHeight = 0;
    private boolean isRecording = false;
    private StringBuilder voiceInputBuffer = new StringBuilder();
    private int voiceInputStartPosition = -1;

    @Override
    public ActivityPostDetailBinding getViewBinding() {
        return ActivityPostDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
        imageAdapter = new PostDetailImageAdapter();
        imageAdapter.setList(new ArrayList<>());

        commentAdapter = new PostDetailCommentAdapter();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnLike.setOnClickListener(v -> toggleLike());

        binding.btnCollect.setOnClickListener(v -> toggleCollect());

        binding.vpPostImages.setAdapter(imageAdapter);
        binding.vpPostImages.setOffscreenPageLimit(1);
        binding.vpPostImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position);
            }
        });

        setupBottomSheet();
        setupCommentInput();
    }
    private void toggleLike() {
        if (currentPostId <= 0) {
            ToastUtils.INSTANCE.showShort(this, "帖子加载中");
            return;
        }
        viewModel.toggleLike(currentPostId);
    }

    private void toggleCollect() {
        if (currentPostId <= 0) {
            ToastUtils.INSTANCE.showShort(this, "帖子加载中");
            return;
        }
        viewModel.toggleCollect(currentPostId);
    }

    private void updateLikeUI(boolean isLiked, int likeCount) {
        if (isLiked) {
            binding.btnLike.setImageResource(R.drawable.ic_like);
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_unlike);
        }
        binding.tvLikeCount.setText(String.valueOf(likeCount));
    }

    private void updateCollectUI(boolean isCollected, int favoriteCount) {
        if (isCollected) {
            binding.btnCollect.setImageResource(R.drawable.ic_collect);
        } else {
            binding.btnCollect.setImageResource(R.drawable.ic_uncollect);
        }
        binding.tvFavoriteCount.setText(String.valueOf(favoriteCount));
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setHideable(false);

        binding.rvComments.post(() -> {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int twoThirdsHeight = (int) (screenHeight * 0.65);
            int collapsedHeight = dpToPx(48) + dpToPx(80);

            ViewGroup.LayoutParams params = binding.layoutBottomSheet.getLayoutParams();
            params.height = twoThirdsHeight;
            binding.layoutBottomSheet.setLayoutParams(params);

            bottomSheetBehavior.setPeekHeight(collapsedHeight);
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        binding.btnToggleSheet.setRotation(180);
                        expandContent();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        binding.btnToggleSheet.setRotation(0);
                        collapseContent();
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        binding.btnToggleSheet.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        binding.ivCommentIcon.setOnClickListener(v -> {
            if (currentPostId <= 0) {
                ToastUtils.INSTANCE.showShort(this, "帖子加载中");
                return;
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
    }

    private void expandContent() {
        ViewGroup.LayoutParams params = binding.layoutViewPager.getLayoutParams();
        params.height = 0;
        binding.layoutViewPager.setLayoutParams(params);
    }

    private void collapseContent() {
        ViewGroup.LayoutParams params = binding.layoutViewPager.getLayoutParams();
        params.height = maxImageHeight > 0 ? maxImageHeight : dpToPx(300);
        binding.layoutViewPager.setLayoutParams(params);
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupCommentInput() {
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);

        binding.btnSend.setOnClickListener(v -> sendComment());

        binding.etComment.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment();
                return true;
            }
            return false;
        });

        setupVoiceInput();
    }

    private void setupVoiceInput() {
        binding.btnInputMic.setOnClickListener(v -> {
            if (!VoiceRecognitionManager.INSTANCE.isInitialized()) {
                ToastUtils.INSTANCE.showShort(this, "语音识别未初始化");
                return;
            }

            if (isRecording) {
                stopVoiceRecording();
            } else {
                showKeyboard();
                startVoiceRecording();
            }
        });
    }

    private void startVoiceRecording() {
        PermissionUtils.INSTANCE.requestRecordAudio(this, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                runOnUiThread(() -> {
                    if (allGranted) {
                        isRecording = true;
                        voiceInputBuffer.setLength(0);
                        voiceInputStartPosition = binding.etComment.getSelectionStart();
                        AnimationDrawable animation = (AnimationDrawable) ContextCompat.getDrawable(PostDetailActivity.this, R.drawable.voice_anim);
                        binding.btnInputMic.setImageDrawable(animation);
                        animation.start();
                        ToastUtils.INSTANCE.showShort(PostDetailActivity.this, "开始说话...");

                        VoiceRecognitionManager.INSTANCE.setCallback(new VoiceRecognitionCallback() {
                            @Override
                            public void onBeginOfSpeech() {
                            }

                            @Override
                            public void onEndOfSpeech() {
                                runOnUiThread(() -> {
                                    binding.btnInputMic.setImageResource(R.drawable.ic_microphone);
                                    isRecording = false;
                                });
                            }

                            @Override
                            public void onPartialResult(String text) {
                                runOnUiThread(() -> {
                                    if (text != null && !text.isEmpty()) {
                                        // 科大讯飞 onPartialResult 返回实时片段结果
                                        // 先清空之前的内容再加新的，避免重复
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(text);
                                        updateVoiceInputText();
                                    }
                                });
                            }

                            @Override
                            public void onFinalResult(String text) {
                                runOnUiThread(() -> {
                                    // onFinalResult 只返回标点符号确认，最终结果已在 onPartialResult 累积
                                    // 如果有缓存内容，则保留；如果没有，则使用返回的文本
                                    String existingText = voiceInputBuffer.toString();
                                    if (existingText.isEmpty() && text != null && !text.isEmpty()) {
                                        voiceInputBuffer.setLength(0);
                                        voiceInputBuffer.append(text);
                                        updateVoiceInputText();
                                    }
                                    // 如果 existingText 有内容，说明 onPartialResult 已经更新过了，不需要再处理
                                    isRecording = false;
                                    binding.btnInputMic.setImageResource(R.drawable.ic_microphone);
                                });
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                runOnUiThread(() -> {
                                    binding.btnInputMic.setImageResource(R.drawable.ic_microphone);
                                    isRecording = false;
                                    ToastUtils.INSTANCE.showShort(PostDetailActivity.this, "识别失败: " + errorMsg);
                                });
                            }

                            @Override
                            public void onVolumeChanged(int volume) {
                            }
                        });
                        VoiceRecognitionManager.INSTANCE.startListening(PostDetailActivity.this);
                    } else {
                        ToastUtils.INSTANCE.showShort(PostDetailActivity.this, "需要麦克风权限才能使用语音输入");
                    }
                });
            }
        });
    }

    private void stopVoiceRecording() {
        VoiceRecognitionManager.INSTANCE.stopListening();
        isRecording = false;
        if (binding.btnInputMic.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) binding.btnInputMic.getDrawable()).stop();
        }
        binding.btnInputMic.setImageResource(R.drawable.ic_microphone);
        hideKeyboard();
    }

    private void updateVoiceInputText() {
        EditText et = binding.etComment;
        int cursorPos = voiceInputStartPosition;
        if (cursorPos < 0) cursorPos = 0;

        String currentText = et.getText() != null ? et.getText().toString() : "";
        String beforeVoice = cursorPos <= currentText.length()
                ? currentText.substring(0, cursorPos) : currentText;
        String afterVoice = cursorPos < currentText.length()
                ? currentText.substring(cursorPos) : "";

        et.setText(beforeVoice + voiceInputBuffer.toString() + afterVoice);
        et.setSelection(beforeVoice.length() + voiceInputBuffer.length());
    }

    private void sendComment() {
        String content = binding.etComment.getText() != null
                ? binding.etComment.getText().toString().trim() : "";
        if (content.isEmpty()) {
            ToastUtils.INSTANCE.showShort(this, "请输入评论内容");
            return;
        }
        if (currentPostId > 0) {
            viewModel.sendComment(currentPostId, content);
            binding.etComment.setText("");
                }
            }

            @Override
            public void initData() {
                viewModel.getPostLiveData().observe(this, this::bindPost);

                viewModel.getCommentsLiveData().observe(this, comments -> {
                    if (comments != null) {
                        commentAdapter.setList(comments);
                        binding.tvCommentHeader.setText(comments.size() + "条评论");
                    }
                });

                viewModel.getLoadingLiveData().observe(this, isLoading -> {
                    if (isLoading != null && isLoading) {
                        showLoading("加载中...");
                    } else {
                        hideLoading();
                    }
                });

                viewModel.getErrorLiveData().observe(this, errorMsg -> {
                    if (errorMsg != null && !errorMsg.isEmpty()) {
                        ToastUtils.INSTANCE.showShort(this, errorMsg);
                    }
                });

                viewModel.getCommentSendSuccessLiveData().observe(this, success -> {
                    if (success != null && success) {
                        ToastUtils.INSTANCE.showShort(this, "评论发送成功");
                    }
                });

                viewModel.getLikeLoadingLiveData().observe(this, isLoading -> {
                    binding.btnLike.setEnabled(isLoading == null || !isLoading);
                });

                viewModel.getCollectLoadingLiveData().observe(this, isLoading -> {
                    binding.btnCollect.setEnabled(isLoading == null || !isLoading);
                });

                long postId = getIntent().getLongExtra(EXTRA_POST_ID, -1);
                if (postId > 0) {
                    currentPostId = postId;
                    viewModel.loadPost(postId);
                    viewModel.loadComments(postId);
                } else {
                    ToastUtils.INSTANCE.showShort(this, "无效的帖子");
                    finish();
                }
            }

            private void bindPost(PostResponseDto post) {
                try {
                    if (post == null) {
                        return;
                    }
                    binding.tvPostTitle.setText(post.getTitle());
                    binding.tvPostContent.setText(post.getContent());
                    binding.tvAuthorName.setText(post.getAuthorName());

                    Integer likeCount = post.getLikeCount();
                    int currentLikeCount = likeCount != null ? likeCount : 0;
                    boolean currentIsLiked = post.isLiked() != null && post.isLiked();
                    updateLikeUI(currentIsLiked, currentLikeCount);

                    Integer favoriteCount = post.getFavoriteCount();
                    int currentFavoriteCount = favoriteCount != null ? favoriteCount : 0;
                    boolean currentIsFavorited = post.isFavorited() != null && post.isFavorited();
                    updateCollectUI(currentIsFavorited, currentFavoriteCount);

                    String authorAvatar = post.getAuthorAvatar();
                    if (authorAvatar != null && !authorAvatar.isEmpty()) {
                        ImageRequest avatarRequest = new ImageRequest.Builder(this)
                                .data(authorAvatar)
                                .placeholder(R.drawable.bg_community_post_avatar)
                                .error(R.drawable.bg_community_post_avatar)
                                .target(binding.ivAuthorAvatar)
                                .transformations(new CircleCropTransformation())
                                .build();
                        Coil.imageLoader(this).enqueue(avatarRequest);
                    }

                    List<String> images = post.getImages();
                    if (images != null && !images.isEmpty()) {
                        List<String> validImages = new ArrayList<>();
                        for (String img : images) {
                            if (img != null && !img.trim().isEmpty()) {
                                validImages.add(img);
                            }
                        }
                        if (validImages.isEmpty()) {
                            imageAdapter.setList(new ArrayList<>());
                            binding.layoutViewPager.setVisibility(View.GONE);
                            binding.layoutPageIndicator.removeAllViews();
                        } else {
                            int screenWidth = getResources().getDisplayMetrics().widthPixels;
                            binding.layoutViewPager.setVisibility(View.VISIBLE);
                            binding.layoutPageIndicator.removeAllViews();
                            setupPageIndicator(validImages.size(), 0);
                            maxImageHeight = (int) (screenWidth * 0.75f);
                            applyViewPagerHeight(screenWidth);
                            imageAdapter.setList(validImages);
                            calculateMaxImageHeight(validImages, 0, screenWidth);
                        }
                    } else {
                        imageAdapter.setList(new ArrayList<>());
                        binding.layoutViewPager.setVisibility(View.GONE);
                        binding.layoutPageIndicator.removeAllViews();
                    }
                    Integer commentCount = post.getCommentCount();
                    binding.tvCommentHeader.setText((commentCount != null ? commentCount : 0) + "条评论");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void calculateMaxImageHeight(List<String> images, int index, int screenWidth) {
                if (index >= images.size()) {
                    applyViewPagerHeight(screenWidth);
                    imageAdapter.setList(images);
                    return;
                }
                String url = images.get(index);
                Target target = new Target() {
                    @Override
                    public void onStart(Drawable placeholder) {
                    }

                    @Override
                    public void onSuccess(Drawable result) {
                        int intrinsicHeight = result.getIntrinsicHeight();
                        int intrinsicWidth = result.getIntrinsicWidth();
                        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                            int scaledHeight = screenWidth * intrinsicHeight / intrinsicWidth;
                            if (scaledHeight > maxImageHeight) {
                                maxImageHeight = scaledHeight;
                            }
                        }
                        calculateMaxImageHeight(images, index + 1, screenWidth);
                    }

                    @Override
                    public void onError(Drawable error) {
                        calculateMaxImageHeight(images, index + 1, screenWidth);
                    }
                };
                ImageRequest request = new ImageRequest.Builder(this)
                        .data(url)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .target(target)
                        .build();
                coil.ImageLoader imageLoader = coil.Coil.imageLoader(this);
                imageLoader.enqueue(request);
            }

            private void applyViewPagerHeight(int screenWidth) {
                ViewGroup.LayoutParams params = binding.layoutViewPager.getLayoutParams();
                if (maxImageHeight > 0) {
                    params.height = maxImageHeight;
                } else {
                    params.height = (int) (screenWidth * 0.75f);
                }
                binding.layoutViewPager.setLayoutParams(params);
            }

            private void setupPageIndicator(int count, int selected) {
                binding.layoutPageIndicator.removeAllViews();
                binding.layoutPageIndicator.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
                float density = getResources().getDisplayMetrics().density;
                for (int i = 0; i < count; i++) {
                    View dot = new View(this);
                    int size = (int) (4 * density + 0.5f);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                    lp.setMargins(size, 0, size, 0);
                    dot.setLayoutParams(lp);
                    dot.setBackgroundResource(i == selected
                            ? R.drawable.dot_post_detail_selected
                            : R.drawable.dot_post_detail_normal);
                    binding.layoutPageIndicator.addView(dot);
                }
            }

            private void updatePageIndicator(int selected) {
                int count = binding.layoutPageIndicator.getChildCount();
                if (selected < 0 || selected >= count) return;
                for (int i = 0; i < count; i++) {
                    binding.layoutPageIndicator.getChildAt(i).setBackgroundResource(
                            i == selected
                                    ? R.drawable.dot_post_detail_selected
                                    : R.drawable.dot_post_detail_normal);
                }
            }

    private void showKeyboard() {
        // 让 EditText 获得焦点
        binding.etComment.requestFocus();
        // 弹出软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null && imm != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


    @Override
            protected void onResume() {
                super.onResume();
                int collapsedHeight = dpToPx(48) + dpToPx(80);
                bottomSheetBehavior.setPeekHeight(collapsedHeight);
            }

            @Override
            protected void onPause() {
                super.onPause();
                if (isRecording) {
                    stopVoiceRecording();
                }
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                VoiceRecognitionManager.INSTANCE.stopListening();
                VoiceRecognitionManager.INSTANCE.setCallback(null);
            }
}