package com.user.profile.ui.page;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agri.pest.client.model.response.PostResponseDto;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.LogUtils;
import com.user.R;
import com.user.databinding.FragmentStarProfileBinding;
import com.user.profile.model.StarFavoriteMutiItem;
import com.user.profile.ui.adapters.StarFavoriteListMapper;
import com.user.profile.ui.adapters.StarFavoriteMultiAdapter;
import com.user.profile.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

@Route(path = RouterPath.USER_FAVORITE_POST_ACTIVITY)
public class FavoritePostActivity extends BaseActivity<FragmentStarProfileBinding> {
    FragmentStarProfileBinding binding;
    ProfileViewModel viewModel;
    private StarFavoriteMultiAdapter starAdapter;
    List<StarFavoriteMutiItem> multiList = new ArrayList<>();

    @NonNull
    @Override
    public FragmentStarProfileBinding getViewBinding() {
        return FragmentStarProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        Log.d("ljx", "initview");
        binding = getBinding();
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        starAdapter = new StarFavoriteMultiAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvStarPosts.setLayoutManager(layoutManager);
        binding.rvStarPosts.setAdapter(starAdapter);

        viewModel.getMineFavoritePostsLivedata().observe(this, posts -> {
            if (posts != null && !posts.isEmpty()) {
                binding.consInformationContent.setBackgroundResource(R.drawable.shape_stp_bgmain);
            }
            else{
                binding.consInformationContent.setBackgroundResource(R.drawable.bg_starandshiie_profile);
            }
             multiList = StarFavoriteListMapper.toMultiList(posts);
            starAdapter.setList(multiList);
            hideLoading();
        });

        binding.bg.cvInformationBack.setOnClickListener(v -> {
            hideLoading();
            finish();
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
        starAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                LogUtils.INSTANCE.d("postadapter","positon"+position+"size"+multiList.size());
                if(multiList.size()>position &&  multiList.get(position )!=null){
                    Long postId = multiList.get(position ).getPost().getId();
                    ARouter.getInstance()
                            .build(RouterPath.COMMUNITY_POST_DETAIL)
                            .withLong("post_id",postId)
                            .navigation();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initData() {
        viewModel.getFirstFavoritePosts();
        showLoading("加载中");
    }



}
