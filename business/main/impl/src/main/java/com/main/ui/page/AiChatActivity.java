package com.main.ui.page;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.LogUtils;
import com.main.impl.R;
import com.main.impl.databinding.ActivityAiChatBinding;

@Route(path = RouterPath.HOME_AICHAT)
public class AiChatActivity extends BaseActivity<ActivityAiChatBinding> {
    @NonNull
    @Override
    public ActivityAiChatBinding getViewBinding() {
        return ActivityAiChatBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        Fragment aiMainFragment =(Fragment) ARouter.getInstance()
                .build(RouterPath.DETECTION_AICHAT)
                .navigation();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_aichat, aiMainFragment)
                .commit();

        getBinding().back.cvInformationBack.setOnClickListener(v->{
            finish();
        });

    }

    @Override
    public void initData() {

    }
}
