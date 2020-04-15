package com.borisenkoda.mobile.mvvmdatabinding.features.login

import androidx.lifecycle.ViewModelProvider
import com.borisenkoda.mobile.mvvmdatabinding.R
import com.borisenkoda.mobile.mvvmdatabinding.base.BaseActivity
import com.borisenkoda.mobile.mvvmdatabinding.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {


    override fun getViewModel(): LoginViewModel {
        return ViewModelProvider(
            this,
            LoginViewModelFactory(applicationContext)
        ).get(LoginViewModel::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

}