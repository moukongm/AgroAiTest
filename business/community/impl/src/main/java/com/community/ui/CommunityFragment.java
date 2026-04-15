package com.community.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
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

import java.util.Collections;

public class CommunityFragment extends BaseFragment<FragmentCommunityBinding> {

    private CommunityViewModel viewModel;
    private PostAdapter postAdapter;
    private FabDragHelper fabDragHelper;
    private SearchFragment searchFragment;

    public interface OnNavigationControlListener {
        void hideBottomNavigation();
        void showBottomNavigation();
    }

    private OnNavigationControlListener navListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationControlListener) {
            navListener = (OnNavigationControlListener) context;
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
                            viewModel.loadMorePosts();
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
                viewModel.toggleLike(item.getId());
            }
        });

        getBinding().fabCommunityPublish.setOnClickListener(v -> {
            ToastUtils.INSTANCE.showShort(requireContext(), "发布功能开发中");
        });

        MMKVInstance communityStorage = MMKVUtils.INSTANCE.custom("community_module");
        fabDragHelper = new FabDragHelper(getBinding().fabCommunityPublish, communityStorage, "fab");
        fabDragHelper.attach();

        setupSearch();

    }

    @Override
    public void initData() {

        viewModel.getPostsLiveData().observe(this, posts -> {
            postAdapter.setList(posts != null ? posts : Collections.emptyList());
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

    private void setupSearch() {
        getBinding().layoutSearch.setOnClickListener(v -> {
            if (searchFragment == null || !searchFragment.isAdded()) {
                searchFragment = new SearchFragment();
                hideNav();

                searchFragment.setSearchListener(new SearchFragment.SearchListener() {
                    @Override
                    public void onSearch(String keyword) {
                        viewModel.searchPosts(keyword);
                        closeSearchFragment();
                    }

                    @Override
                    public void onClose() {
                        closeSearchFragment();
                    }
                });
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(getBinding().fragmentContainer.getId(), searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void closeSearchFragment() {
        if (searchFragment != null && searchFragment.isAdded()) {
            getActivity().getSupportFragmentManager().popBackStack();
            searchFragment = null;
        }
        showNav();
    }


    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshPosts();
    }
}
