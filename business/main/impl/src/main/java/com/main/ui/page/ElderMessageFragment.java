package com.main.ui.page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.main.MessageCommentResponseDto;
import com.main.impl.R;
import com.main.impl.databinding.FragmentMessageBinding;
import com.main.impl.databinding.FragmentMessageElderBinding;
import com.main.ui.adapter.ElderMessageAdapter;
import com.main.ui.adapter.MessageAdapter;
import com.main.ui.adapter.NoticeRv1Adapter;
import com.main.viewmodel.MessageViewModel;
import com.uikit.base.BaseFragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ElderMessageFragment extends BaseFragment<FragmentMessageElderBinding> {
    FragmentMessageElderBinding binding;
    MessageViewModel viewModel;
    private ElderMessageAdapter messageAdapter;
    private boolean isSubFragmentOpen = false;
    List<MessageCommentResponseDto> commentlist = new ArrayList<>();
    List<MessageResponseDto> list = new ArrayList<>();

    @Override
    public @NotNull FragmentMessageElderBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMessageElderBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();

//        BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(this);

        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        binding.item1.ivThumb.setVisibility(View.INVISIBLE);
        binding.item1.tvTime.setVisibility(View.INVISIBLE);
        binding.item1.dotThumb.setVisibility(View.VISIBLE);

        binding.item2.consAvater.setBackgroundResource(R.drawable.ic_mes_xitong);
        binding.item2.ivAvatar.setVisibility(View.INVISIBLE);
        binding.item2.ivThumb.setVisibility(View.INVISIBLE);
        binding.item2.tvTitle.setText("系统消息");
        binding.item2.tvTime.setVisibility(View.INVISIBLE);

        // 初始化消息列表 RecyclerView
        messageAdapter = new ElderMessageAdapter();
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMessages.setAdapter(messageAdapter);


        messageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {

            }
        });

        setLivedata();
        //分页加载评论区
        binding.rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisible = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                if (lastVisible >= totalCount - 1) {
                    viewModel.getMoreMessageUser();
                }
            }
        });

        // 监听子 Fragment 的返回栈变化
        getChildFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // 当子 Fragment 的返回栈为空（即所有子 Fragment 都被弹出）时，恢复父 Fragment 的视图
                if (getChildFragmentManager().getBackStackEntryCount() == 0 && isSubFragmentOpen) {
                    LogUtils.INSTANCE.d("xjl", "onresume");
                    binding.cardMessageList.setVisibility(View.VISIBLE);
                    binding.messageHeader.setVisibility(View.VISIBLE);
                    binding.fragmentContainer.setVisibility(View.GONE);
                    isSubFragmentOpen = false;
                } else {
                    binding.fragmentContainer.setVisibility(View.VISIBLE);
                    binding.messageHeader.setVisibility(View.GONE);
                    binding.cardMessageList.setVisibility(View.GONE);
                }
            }
        });

        messageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                LogUtils.INSTANCE.d("qwertyuiop","positon"+position);
                if(commentlist.size()>position &&  commentlist.get(position)!=null){
                    Long postId = commentlist.get(position).getMessageResponseDto().getPostId();
                    Long id = commentlist.get(position).getMessageResponseDto().getId();
                    ARouter.getInstance()
                            .build(RouterPath.COMMUNITY_POST_DETAIL)
                            .withLong("post_id",postId)
                            .navigation();
                    //红点消失
                    viewModel.isRead(id,true,false);

                }

            }
        });

        binding.item1.consMainitem.setOnClickListener(v -> {
            LogUtils.INSTANCE.d("xjl", "show");
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,NoticeMessageFragment.InstanceNoticeMessageFragment(0))
                    .addToBackStack(null)
                    .commit();
            isSubFragmentOpen = true;
        });

        binding.item2.consMainitem.setOnClickListener(v -> {
            LogUtils.INSTANCE.d("xjl", "show");
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, NoticeMessageFragment.InstanceNoticeMessageFragment(1))
                    .addToBackStack(null)
                    .commit();
            isSubFragmentOpen = true;
        });

    }

    private void setLivedata() {
        LiveDataExtKt.observeNonNull(viewModel.getGetMessageComment(), this, result -> {
            commentlist.clear(); // 修复：先清空旧数据，避免列表无限增长
            for (int i = 0; i < result.size(); i++) {
                MessageResponseDto messageResponseDto = result.get(i);
                viewModel.getPostAvr(messageResponseDto.getPostId(), i);
                MessageCommentResponseDto messageCommentResponseDto = new MessageCommentResponseDto(messageResponseDto, null);
                commentlist.add(messageCommentResponseDto);
            }
//            list.clear();
//            list.addAll(result);
            messageAdapter.setList(commentlist);
            return null;
        });
        LiveDataExtKt.observeNonNull(viewModel.getGetPostAvr(), this, result -> {
            try {
                String[] split = result.split(",");
                if (split.length < 2) return null;
                int index = Integer.parseInt(split[1]);
                if (index < 0 || index >= commentlist.size()) return null;
                MessageCommentResponseDto messageCommentResponseDto = commentlist.get(index);
                MessageResponseDto messageResponseDto = messageCommentResponseDto.getMessageResponseDto();
                MessageCommentResponseDto newMes = new MessageCommentResponseDto(messageResponseDto, split[0]);
                commentlist.set(index, newMes);
                messageAdapter.setList(commentlist);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return null;
        });
        LiveDataExtKt.observeNonNull(viewModel.getHaveNotSeeTz(), this, result -> {
            LogUtils.INSTANCE.d("qwertyuiop", result);
            if ("yes".equals(result)) {
                binding.item1.dotThumb.setVisibility(View.VISIBLE);
            } else {
                binding.item1.dotThumb.setVisibility(View.GONE);
            }
            return null;
        });
        LiveDataExtKt.observeNonNull(viewModel.getHaveNotSeeXT(), this, result -> {
            if ("yes".equals(result)) {
                binding.item2.dotThumb.setVisibility(View.VISIBLE);
            } else {
                binding.item2.dotThumb.setVisibility(View.GONE);
            }
            return null;
        });
        LiveDataExtKt.observeNonNull(viewModel.getIsReadMF(), this, result -> {
            LogUtils.INSTANCE.d("qwertyuiop", result);
            if ("yes".equals(result)) {
                //红点消失
                viewModel.getFirstMessageUser();
            } else {
                Context ctx = getContext();
                if (ctx != null) {
                    ToastUtils.INSTANCE.showShort(ctx.getApplicationContext(), result);
                }
            }
            return null;
        });
    }


    @Override
    public void initData() {
        viewModel.getFirstMessageUser();
//       viewModel.ceshi();
    }

}
