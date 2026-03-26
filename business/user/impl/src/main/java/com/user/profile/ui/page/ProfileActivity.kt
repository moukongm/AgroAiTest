package com.user.profile.ui.page

import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.common.utils.setOnDebouncedClickListener
import com.user.R
import com.user.databinding.ActivityProfileBinding
import com.user.profile.viewmodel.ProfileViewModel

@Route(path = RouterPath.USER_PROFILE_ACTIVITY)
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {
    private lateinit var viewModel: ProfileViewModel

    override fun getViewBinding(): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {

        // TODO: Implement user profile
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.ivEditprofile.setOnDebouncedClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.profile_main, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.mineSet.setOnDebouncedClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.profile_main, SettingProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun initData() {
        viewModel.getUserMes()
    }
}