package com.user

import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.agri.pest.client.api.ServiceCode
import com.user.databinding.ActivityLoginBinding

@Route(path = RouterPath.USER_LOGIN_ACTIVITY)
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private lateinit var viewModel: LoginViewModel

    override fun getViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // 登录按钮点击事件
        binding.btnLogin.setOnClickListener {
            val account = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(account, password)
        }

        // 注册按钮点击事件
        binding.btnRegister.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入手机号和密码进行注册", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 注册Demo，username可选不传
            viewModel.register(phone, password)
        }
    }

    override fun initData() {
        // 观察登录结果
        viewModel.loginResultLiveData.observe(this) { response ->
            if (response.code == ServiceCode.SUCCESS) {
                binding.tvResult.text = "登录成功！\nToken: ${response.data?.token}\nUserId: ${response.data?.userId}"
                Toast.makeText(this, "登录成功: ${response.message}", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvResult.text = "登录失败: Code=${response.code}, Msg=${response.message}"
                Toast.makeText(this, "登录失败: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察注册结果
        viewModel.registerResultLiveData.observe(this) { response ->
            if (response.code == ServiceCode.SUCCESS) {
                binding.tvResult.text = "注册成功！\nToken: ${response.data?.token}\nUserId: ${response.data?.userId}"
                Toast.makeText(this, "注册成功: ${response.message}", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvResult.text = "注册失败: Code=${response.code}, Msg=${response.message}"
                Toast.makeText(this, "注册失败: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察网络错误
        viewModel.errorLiveData.observe(this) { errorMsg ->
            binding.tvResult.text = "请求异常: $errorMsg"
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
