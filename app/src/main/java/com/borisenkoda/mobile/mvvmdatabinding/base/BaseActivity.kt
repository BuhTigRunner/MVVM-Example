package com.borisenkoda.mobile.mvvmdatabinding.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ForegroundActivityProvider
import androidx.databinding.library.baseAdapters.BR

abstract class BaseActivity<T : ViewDataBinding, S : BaseViewModel> : AppCompatActivity() {

    protected var binding: T? = null

    lateinit var vm: S

    private val foregroundActivityProvider = ForegroundActivityProvider.instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        vm = getViewModel()
        binding?.apply {
            setVariable(BR.vm, vm)
            lifecycleOwner = this@BaseActivity
            executePendingBindings()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    abstract fun getViewModel(): S

    @LayoutRes
    abstract fun getLayoutId(): Int


    override fun onResume() {
        super.onResume()
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        foregroundActivityProvider.clear()
    }

}