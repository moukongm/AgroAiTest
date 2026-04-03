package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.user.R;
import com.user.databinding.FragmentStarProfileBinding;
import com.user.profile.model.StarFavoriteMutiItem;
import com.user.profile.ui.adapters.StarFavoriteListMapper;
import com.user.profile.ui.adapters.StarFavoriteMultiAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

public class FavoritePostFragment extends BaseFragment<FragmentStarProfileBinding> {
    FragmentStarProfileBinding binding;
    ProfileViewModel viewModel;
    private StarFavoriteMultiAdapter starAdapter;

    @NonNull
    @Override
    public FragmentStarProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentStarProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        Log.d("ljx", "initview");
        binding = getBinding();
        Fragment parent = requireParentFragment();
        if (parent instanceof ProfileFragment) {
            viewModel = new ViewModelProvider(parent).get(ProfileViewModel.class);
        } else {
            // 兼容：尝试从爷爷辈获取
            viewModel = new ViewModelProvider(parent.requireParentFragment()).get(ProfileViewModel.class);
        }
        starAdapter = new StarFavoriteMultiAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvStarPosts.setLayoutManager(layoutManager);
        binding.rvStarPosts.setAdapter(starAdapter);

        viewModel.getMineFavoritePostsLivedata().observe(this, posts -> {
            if (posts != null && !posts.isEmpty()) {
                binding.consInformationContent.setBackgroundResource(R.drawable.shape_stp_bgmain);
            }
            starAdapter.setList(StarFavoriteListMapper.toMultiList(posts));
        });

        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        binding.rvStarPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) {
                    return;
                }
                int lastVisible = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                if (lastVisible >= totalCount - 1) {
                    viewModel.getMoreFavoritePosts();
                }
            }
        });
    }

    @Override
    public void initData() {
    }
}
