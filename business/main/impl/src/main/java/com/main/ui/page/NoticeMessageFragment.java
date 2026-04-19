package com.main.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.main.data.Repository;
import com.main.impl.R;
import com.main.impl.databinding.FragmentMessageNoticeBinding;
import com.main.ui.adapter.NoticeRv1Adapter;
import com.main.viewmodel.MessageViewModel;
import com.uikit.base.BaseFragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Route(path = RouterPath.NOTICE_MESSAGE_FRAGMENT)
public class NoticeMessageFragment extends BaseFragment<FragmentMessageNoticeBinding> {

    private NoticeRv1Adapter noticeRv1Adapter;
    private final Repository repository = new Repository();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private static final String BUNDLE = "tab";
    
    private BaseFragmentPagerAdapter pagerAdapter;
    private List<String> categories = Arrays.asList("全部", "系统通知", "预警通知", "用户调研");
    
    // Tab 对应的消息类型：null 表示全部，其他对应 MessageResponseDto 的 type 字段
    private final String[] tabTypes = {null, "SYSTEM", "ALERT", "COMMENT"};
    List<NoticeContentFragment> fragmentList = new ArrayList<>();

    int tab  = 0;
    
    // 全部消息数据
    private List<MessageResponseDto> allMessages;
    
    FragmentMessageNoticeBinding binding;
    MessageViewModel viewModel;
    View lastView = null;
    View lastPageView = null;

    public NoticeMessageFragment() {

    }
    public static NoticeMessageFragment InstanceNoticeMessageFragment(int i){
        NoticeMessageFragment noticeMessageFragment = new NoticeMessageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE,i);
        noticeMessageFragment.setArguments(bundle);
        return noticeMessageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            tab = getArguments().getInt(BUNDLE);
        }
    }

    @Override
    public @NotNull FragmentMessageNoticeBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMessageNoticeBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        hideBottomNav();
        LogUtils.INSTANCE.d("xjl","showcontenet");
        binding = getBinding();
        binding.mainpageWarning.setEnabled(true);
        
        // 拦截系统返回手势，让右滑只退回到 MessageFragment，而不是退出 App
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 自定义返回逻辑：只退回到父 Fragment，不退出 App
                getParentFragmentManager().popBackStack();
            }
        });
        viewModel = new ViewModelProvider(requireParentFragment()).get(MessageViewModel.class);
        // 初始化顶部分类标签 RecyclerView
        noticeRv1Adapter = new NoticeRv1Adapter();
        binding.rv1Notice.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rv1Notice.setAdapter(noticeRv1Adapter);
        noticeRv1Adapter.setList(categories);

        noticeRv1Adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                view.setSelected(true);
                if(lastView != null) {
                    lastView.setSelected(false);
                }
                lastView = view;
                // Tab 点击切换 ViewPager2 页面
                binding.viewpagerNotice.setCurrentItem(position, false);
            }
        });
        // 初始化 ViewPager2
        initViewPager2();
        LogUtils.INSTANCE.d("opopop",tab+"");
        binding.viewpagerNotice.setCurrentItem(tab, false);
        // 使用 post() 确保在布局完成后再获取 View
        binding.rv1Notice.post(() -> {
            View viewByPosition = binding.rv1Notice.findViewHolderForAdapterPosition(tab) != null
                    ? binding.rv1Notice.findViewHolderForAdapterPosition(tab).itemView : null;
            if(viewByPosition != null){
                viewByPosition.setSelected(true);
                lastPageView = viewByPosition;
            }


        });


        // 返回按钮
        binding.back.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.mainpageWarning.setOnClickListener(v -> {
            viewModel.isReadAll();
            binding.mainpageWarning.setEnabled(false);
        });

        LiveDataExtKt.observeNonNull(viewModel.getIsReadAllEnable(),this,result->{
            binding.mainpageWarning.setEnabled(true);
            return null;
        });
    }

    private void initViewPager2() {
        // 创建 Fragment 列表（4 个 Tab 对应 4 个 Fragment）,使得一一对应
        for (int i = 0; i < categories.size(); i++) {
            String type = tabTypes[i];
            if (type == null) {
                // 全部
                fragmentList.add(NoticeContentFragment.newInstance(null));
            } else {
                fragmentList.add(NoticeContentFragment.newInstance(type));
            }
        }
        // 创建并设置适配器
        pagerAdapter = new BaseFragmentPagerAdapter(this);
        pagerAdapter.setFragments(fragmentList);
        binding.viewpagerNotice.setAdapter(pagerAdapter);

        binding.viewpagerNotice.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 同步 Tab 选中状态
                if (noticeRv1Adapter.getItemCount() > position) {
                    View view = binding.rv1Notice.findViewHolderForAdapterPosition(position) != null
                            ? binding.rv1Notice.findViewHolderForAdapterPosition(position).itemView : null;
                    if (view != null) {
                        view.setSelected(true);
                        if (lastPageView != null) {
                            lastPageView.setSelected(false);
                        }
                        lastPageView = view;
                    }
                }
            }
        });
        
        // 优化：设置预加载页数
        binding.viewpagerNotice.setOffscreenPageLimit(2);
    }

    @Override
    public void initData() {


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomNav();
    }
}