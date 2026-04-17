package com.user.profile.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agri.pest.client.model.response.PostResponseDto;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.user.databinding.ActivityProfileBinding;
import com.user.databinding.ActivityProfileElderBinding;
import com.user.profile.ui.adapters.MinePostAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(path = RouterPath.USER_PROFILE_ACTIVITY)
public class ElderProfileFragment extends BaseFragment<ActivityProfileElderBinding> {

    private ProfileViewModel viewModel;
    private MinePostAdapter postAdapter;
    ActivityProfileElderBinding binding;
    List<PostResponseDto> list = new ArrayList<>();
    Boolean needRefresh = false;

    @NonNull
    @Override
    public ActivityProfileElderBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return ActivityProfileElderBinding.inflate(inflater, container, false);
    }

    private ProfileViewModel getSharedViewModel() {
        return new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Override
    public void initView() {
        // 使用 requireActivity() 获取 Activity scope，确保与 EditProfileActivity 共享同一个 ViewModel 实例
        // 避免每次进入页面创建新实例导致 LiveData 数据丢失
        viewModel = getSharedViewModel();
        // 初始化 ViewModel 的 Context，确保本地数据访问正常
        viewModel.initContext(requireActivity());
        binding = getBinding();
        postAdapter = new MinePostAdapter();
        binding.userPostRec.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.userPostRec.setAdapter(postAdapter);

        binding.ivEditprofile.setOnClickListener(v -> {
            // 使用 navigationForResult 跳转，等待结果返回
            needRefresh = true;
            ARouter.getInstance()
                    .build(RouterPath.USER_EDIT_PROFILE_ACTIVITY)
                    .navigation();
        });

        binding.userFanscnt.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.USER_FAVORITE_POST_ACTIVITY).navigation();
        });

        binding.tvFansLabel.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.USER_FAVORITE_POST_ACTIVITY).navigation();
        });

        binding.mineSet.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.USER_SETTING_PROFILE_ACTIVITY).navigation();
        });
        binding.tvHistoryLabel.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.DETECTION_HISTORY).navigation();
        });
        binding.userHistorycnt.setOnClickListener(v -> {
            ARouter.getInstance().build(RouterPath.DETECTION_HISTORY).navigation();
        });

        // 观察用户帖子列表
        viewModel.getMinePostsLivedata().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                this.list = list;
                postAdapter.setList(list);
            }
        });

        // 观察用户帖子列表
        viewModel.getHistoryCountLivedata().observe(getViewLifecycleOwner(), list -> {
            LogUtils.INSTANCE.d("historycount", list + "");
            if (list != null) {
                binding.userHistorycnt.setText(String.valueOf(list));
            }
        });

        // 观察头像
        viewModel.getAvatarLivedata().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                ImageLoader.INSTANCE.loadCircle(binding.userHead, avatarUrl);
            }
        });

        // 观察用户名
        viewModel.getNickNameLivedata().observe(getViewLifecycleOwner(), nickName -> {
            LogUtils.INSTANCE.d("ljx", nickName);
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
        LiveDataBus.getInstance().with(BusKey.PROFILE_CHANGED)
                .observe(getViewLifecycleOwner(), object -> {
                    if (object instanceof Boolean) {
                        Boolean b = (Boolean) object;
                        if (b) {
                            viewModel.getUserMes();
                            viewModel.getFirstPosts();
                        }
                    }

                });
        LiveDataBus.getInstance().with(BusKey.DETECTIONHISTORY).observe(getViewLifecycleOwner(), object -> {
            LogUtils.INSTANCE.d("poiuytrewq","wai");
            if (object instanceof Boolean) {
                Boolean b = (Boolean) object;
                if (b) {
                    LogUtils.INSTANCE.d("poiuytrewq","nei");
                    viewModel.getUserMes();
                }
            }
        });
        LiveDataBus.getInstance().with(BusKey.SENTPOST).observe(getViewLifecycleOwner(), object -> {
            if (object instanceof Boolean) {
                Boolean b = (Boolean) object;
                if (b) {
                    viewModel.getFirstPosts();
                }
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
        postAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                LogUtils.INSTANCE.d("postadapter", "positon" + position + "size" + list.size());
                if (list.size() > position && list.get(position) != null) {
                    Long postId = list.get(position).getId();
                    ARouter.getInstance()
                            .build(RouterPath.COMMUNITY_POST_DETAIL)
                            .withLong("post_id", postId)
                            .navigation();
                }

            }
        });

        postAdapter.setOnImageClickListener(new MinePostAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(Long id) {
                ARouter.getInstance()
                        .build(RouterPath.COMMUNITY_POST_DETAIL)
                        .withLong("post_id", id)
                        .navigation();
            }
        });

        LiveDataBus.getInstance().with(BusKey.SENTPOST)
                .observe(getViewLifecycleOwner(), object -> {
                    if ((Boolean) object) {
                        viewModel.getMinePostsLivedata().observe(getViewLifecycleOwner(), list -> {
                            if (list != null && !list.isEmpty()) {
                                this.list = list;
                                postAdapter.setList(list);
                            }
                        });
                    }
                });

        LiveDataBus.getInstance().with(BusKey.COLLECT)
                .observe(getViewLifecycleOwner(), object -> {
                    if((Boolean) object){
                        viewModel.getUserMes();
                    }
                });
    }

    @Override
    public void initData() {
        // 获取用户信息（头像、名字）
        viewModel.getUserMes();
        viewModel.getFirstPosts();
//        viewModel.getFirstFavoritePosts();
//        viewModel.starPost();
    }
}
