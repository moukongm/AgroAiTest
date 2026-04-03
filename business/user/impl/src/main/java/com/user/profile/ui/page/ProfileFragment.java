package com.user.profile.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.user.R;
import com.user.databinding.ActivityProfileBinding;
import com.user.profile.ui.adapters.MinePostAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

@Route(path = RouterPath.USER_PROFILE_ACTIVITY)
public class ProfileFragment extends BaseFragment<ActivityProfileBinding> {

    private ProfileViewModel viewModel;
    private MinePostAdapter postAdapter;
    ActivityProfileBinding binding;

    @NonNull
    @Override
    public ActivityProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return ActivityProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = getBinding();
        // 初始化帖子列表 RecyclerView
        postAdapter = new MinePostAdapter();
        binding.userPostRec.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.userPostRec.setAdapter(postAdapter);

        // 点击事件
        binding.ivEditprofile.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_main, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.userFanscnt.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_main, new FavoritePostFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.tvFansLabel.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_main, new FavoritePostFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.mineSet.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_main, new SettingProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 观察用户帖子列表
        viewModel.getMinePostsLivedata().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                postAdapter.setList(list);
            }
        });

          // 观察头像
        viewModel.getAvatarLivedata().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
               ImageLoader.INSTANCE.loadCircle(binding.userHead,avatarUrl);
            }
        });

        // 观察用户名
        viewModel.getNickNameLivedata().observe(getViewLifecycleOwner(), nickName -> {
            LogUtils.INSTANCE.d("ljx",nickName);
            if (nickName != null && !nickName.isEmpty()) {
                binding.userName.setText(nickName);
            }
        });

        // 观察收藏数
        viewModel.getFavoritesCountLivedata().observe(getViewLifecycleOwner(), favoritesCount -> {
            if (favoritesCount != null) {
                binding.userFanscnt.setText(String.valueOf(favoritesCount));
            }
        });
        // 滑动监听：滑到底部加载更多
        binding.userPostRec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisible = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                if (lastVisible >= totalCount - 1) {
                    viewModel.getMorePosts();
                }
            }
        });
    }

    @Override
    public void initData() {
        // 获取用户信息（头像、名字）
        viewModel.getUserMes();
        viewModel.getFirstPosts();
        viewModel.getFirstFavoritePosts();
//        viewModel.starPost();
    }
}
