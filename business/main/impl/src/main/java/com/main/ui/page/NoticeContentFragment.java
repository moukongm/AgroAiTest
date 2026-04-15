package com.main.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.agri.pest.client.model.response.PostResponseDto;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.main.impl.databinding.FragmentNoticeContentBinding;
import com.main.ui.adapter.NoticeAdapter;
import com.main.viewmodel.MessageViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(path = RouterPath.NOTICE_CONTENT_FRAGMENT)
public class NoticeContentFragment extends BaseFragment<FragmentNoticeContentBinding> {

    private static final String ARG_TYPE = "type";
    
    private NoticeAdapter noticeAdapter;
    FragmentNoticeContentBinding binding;
    private String messageType; // LIKE, COMMENT, SYSTEM, ALERT, 或 null(全部)

    private MessageViewModel viewModel;
    int count = 0;
    List<MessageResponseDto> dtoList = new ArrayList<>();
    int location =-1;

    public NoticeContentFragment() {
    }

    public static NoticeContentFragment newInstance(String type) {
        NoticeContentFragment fragment = new NoticeContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            messageType = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public @NotNull FragmentNoticeContentBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNoticeContentBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        // 初始化适配器
        // ogUtils.INSTANCE.d("xjl",messageType);
        NoticeMessageFragment fragment = (NoticeMessageFragment)requireParentFragment();
        viewModel = new ViewModelProvider(fragment.requireParentFragment()).get(MessageViewModel.class);
        noticeAdapter = new NoticeAdapter();
        binding.rvNoticeContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNoticeContent.setAdapter(noticeAdapter);
        noticeAdapter.setOnNoticeClickListener((lister,i)->{
            viewModel.isRead(lister,false);
            location = i;
        });
        initLiveData();

    }

    private void initLiveData() {
        LiveDataExtKt.observeNonNull(viewModel.getIsRead(),this,result->{
           if("yes".equals(result)){
               // 标记已读成功后，刷新当前列表
               refreshCurrentList();
           }
           else{
               ToastUtils.INSTANCE.showShort(requireActivity().getApplicationContext(),result);
           }
            return null;
        });
    }

    // 根据当前 Tab 类型刷新列表
    private void refreshCurrentList() {
        // 直接调用 ViewModel 刷新方法，LiveData 观察者会自动收到更新
        viewModel.getFirstMessageUser();
    }


    @Override
    public void initData() {

        if(messageType == null){
            List<MessageResponseDto> list = new ArrayList<>();
            LiveDataExtKt.observeNonNull(viewModel.getGetMessageXT(),this,result->{
                LogUtils.INSTANCE.d("xjl","null");
                count++;
                list.addAll(result);
                if(count == 2){
                    undateList(list);
                    count = 0;
                    list.clear();
                }
                return null;
            });
            LiveDataExtKt.observeNonNull(viewModel.getGetMessageWarn(),this,result->{
                list.addAll(result);
                count++;
                if(count == 2){
                    undateList(list);
                    count = 0;
                    list.clear();
                }
                return null;
            });
        }
        else{
            if(messageType.equals("SYSTEM")){
                LiveDataExtKt.observeNonNull(viewModel.getGetMessageXT(),this,result->{
                    LogUtils.INSTANCE.d("xjl","ncf");
                    noticeAdapter.setList(result);
                    dtoList = result;
                    return null;
                });
            }
            else if(messageType.equals("ALERT")){
                LiveDataExtKt.observeNonNull(viewModel.getGetMessageWarn(),this,result->{
                    noticeAdapter.setList(result);
                    dtoList = result;
                    return null;
                });
            }
        }
        binding.rvNoticeContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        // 标记全部已读成功后刷新列表
        LiveDataExtKt.observeNonNull(viewModel.getIsReadAll(),this,result->{

            refreshCurrentList();
            return null;
        });
    }

    private void undateList(List<MessageResponseDto> list) {
        //排序
        List<MessageResponseDto> newList = new ArrayList<>(list);
        newList.sort(Comparator.comparing(NoticeContentFragment::createdAtString,Comparator.nullsLast(String :: compareTo)).reversed());
        noticeAdapter.setList(newList);
    }

    private static String createdAtString(MessageResponseDto p) {
        if (p == null || p.getCreatedAt() == null) {
            return "";
        }
        return p.getCreatedAt().toString();
    }
}