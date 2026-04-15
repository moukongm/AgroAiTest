package com.community.ui;

import android.app.Activity;
import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.NavigationController;
import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;
import com.common.utils.ToastUtils;
import com.community.databinding.FragmentCommunityBinding;
import com.community.databinding.FragmentSearchBinding;
import com.community.ui.adapter.PostAdapter;
import com.community.viewmodel.CommunityViewModel;
import com.community.viewmodel.SearchViewModel;

import java.util.Collections;

public class CommunityFragment extends BaseFragment<FragmentCommunityBinding> {

    private static final int REQUEST_CODE_POST_PUBLISH = 2001;

    private CommunityViewModel viewModel;
    private SearchViewModel searchViewModel;
    private PostAdapter postAdapter;
    private FabDragHelper fabDragHelper;
    private SearchFragment searchFragment;
    private VoiceSearchFragment voiceSearchFragment;

    private boolean isSearchMode = false;

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (isSearchMode) {
                exitSearchMode();
                return;
            }
            setEnabled(false);
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    };

    private NavigationController navListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationController) {
            navListener = (NavigationController) context;
        }
    }

    private void hideNav() {
        if (navListener != null) navListener.hideBottomNavigation();
    }

    private void showNav() {
        if (navListener != null) navListener.showBottomNavigation();
    }

    @Override
    public FragmentCommunityBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentCommunityBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        postAdapter = new PostAdapter();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        getBinding().rvCommunityFeed.setLayoutManager(layoutManager);
        getBinding().rvCommunityFeed.setAdapter(postAdapter);

        getBinding().rvCommunityFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                    if (lm instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager sglm = (StaggeredGridLayoutManager) lm;
                        int[] lastVisibleItemPositions = sglm.findLastVisibleItemPositions(null);
                        int maxLastVisibleItem = 0;
                        for (int pos : lastVisibleItemPositions) {
                            if (pos > maxLastVisibleItem) maxLastVisibleItem = pos;
                        }
                        if (sglm.getItemCount() <= maxLastVisibleItem + 3) {
                            if (isSearchMode) {
                                searchViewModel.loadMoreSearchResults();
                            } else {
                                viewModel.loadMorePosts();
                            }
                        }
                    }
                }
            }
        });

        postAdapter.setOnItemClickListener((item, position) -> {
            if (item != null) {
                ARouter.getInstance()
                        .build(RouterPath.COMMUNITY_POST_DETAIL)
                        .withLong(PostDetailActivity.EXTRA_POST_ID, item.getId())
                        .navigation();
            }
        });

        postAdapter.setOnLikeClickListener((item, position) -> {
            if (item != null && item.getId() != null) {
                if (isSearchMode) {
                    viewModel.toggleLike(item.getId(), searchViewModel);
                } else {
                    viewModel.toggleLike(item.getId(), null);
                }
            }
        });

        getBinding().fabCommunityPublish.setOnClickListener(v -> {
            hideNav();
            launchPostPublish();
        });

        MMKVInstance communityStorage = MMKVUtils.INSTANCE.custom("community_module");
        fabDragHelper = new FabDragHelper(getBinding().fabCommunityPublish, communityStorage, "fab");
        fabDragHelper.attach();

        getBinding().btnBack.setOnClickListener(v -> exitSearchMode());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);

        setupSearch();

    }

    @Override
    public void initData() {
        setupVoiceSearchResultListener();

        viewModel.getPostsLiveData().observe(this, posts -> {
            if (!isSearchMode) {
                postAdapter.setList(posts != null ? posts : Collections.emptyList());
            }
        });

        searchViewModel.getSearchResultsLiveData().observe(this, posts -> {
            if (isSearchMode && posts != null) {
                postAdapter.setList(posts);
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (!isSearchMode && isLoading != null && isLoading) {
                showLoading("加载中...");
            } else {
                hideLoading();
            }
        });

        searchViewModel.getSearchLoadingLiveData().observe(this, isLoading -> {
            if (isSearchMode && isLoading != null && isLoading) {
                showLoading("搜索中...");
            } else {
                hideLoading();
            }
        });

        viewModel.getErrorLiveData().observe(this, errorMsg -> {
            if (!isSearchMode && errorMsg != null && !errorMsg.isEmpty()) {
                ToastUtils.INSTANCE.showShort(requireContext(), errorMsg);
            }
        });

        viewModel.getToastLiveData().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.INSTANCE.showShort(requireContext(), msg);
            }
        });
        LiveDataBus.getInstance().with(BusKey.PROFILE_CHANGED)
                .observe(getViewLifecycleOwner(),object->{
                    if(object instanceof Boolean){
                        Boolean b = (Boolean) object;
                        if(b){
                            viewModel.loadPosts();
                        }
                    }

                });
    }

    private void launchPostPublish() {
        ARouter.getInstance()
                .build(RouterPath.COMMUNITY_POST_PUBLISH)
                .navigation(requireActivity(), REQUEST_CODE_POST_PUBLISH);
    }

    private void setupSearch() {
        getBinding().layoutSearch.setOnClickListener(v -> {
            if (searchFragment == null || !searchFragment.isAdded()) {
                searchFragment = new SearchFragment();
                hideNav();

                searchFragment.setSearchListener(new SearchFragment.SearchListener() {
                    @Override
                    public void onSearch(String keyword) {
                        enterSearchMode(keyword);
                    }

                    @Override
                    public void onClose() {
                        closeSearchFragment();
                    }

                    @Override
                    public void onShowSearchResult(String keyword) {
                    }
                });

                searchFragment.setVoiceSearchListener(() -> openVoiceSearch());
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(getBinding().fragmentContainer.getId(), searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void enterSearchMode(String keyword) {
        isSearchMode = true;
        closeSearchFragment(false);

        getBinding().btnBack.setVisibility(View.VISIBLE);
        getBinding().layoutSearch.setVisibility(View.VISIBLE);
        getBinding().etCommunitySearch.setText(keyword);
        searchViewModel.searchPosts(keyword);
    }

    private void exitSearchMode() {
        isSearchMode = false;

        showNav();

        getBinding().btnBack.setVisibility(View.GONE);
        getBinding().etCommunitySearch.setText("请输入关键词");
        getBinding().etCommunitySearch.setAlpha(0.6f);
        getBinding().etCommunitySearch.setTextColor(0x99619189);

        viewModel.loadPosts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_POST_PUBLISH && resultCode == Activity.RESULT_OK) {
            showNav();
            viewModel.refreshPosts();
        } else if (requestCode == REQUEST_CODE_POST_PUBLISH) {
            showNav();
        }
    }

    private void closeSearchFragment() {
        closeSearchFragment(true);
    }

    private void openVoiceSearch() {

        // 添加 VoiceSearchFragment
        if (voiceSearchFragment == null || !voiceSearchFragment.isAdded()) {
            voiceSearchFragment = new VoiceSearchFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(getBinding().fragmentContainer.getId(), voiceSearchFragment)
                    .addToBackStack("voice_search")
                    .commit();
            voiceSearchFragment = null;
        }
    }


    private void closeSearchFragment(boolean showNavigation) {
        if (searchFragment != null && searchFragment.isAdded()) {
            getActivity().getSupportFragmentManager().popBackStack();
            searchFragment = null;
        }
        if (showNavigation) {
            showNav();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!isSearchMode) {
            showNav();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSearchMode) {
            showNav();
            viewModel.refreshPosts();
        }
    }

    private void setupVoiceSearchResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                VoiceSearchFragment.ACTION_VOICE_SEARCH,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String keyword = result.getString(VoiceSearchFragment.KEY_VOICE_SEARCH_RESULT);
                    if (keyword != null && !keyword.isEmpty()) {
                        searchViewModel.saveToHistory(keyword);
                        enterSearchMode(keyword);
                    }
                });
    }
}
