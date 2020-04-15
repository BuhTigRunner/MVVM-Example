package com.borisenkoda.mobile.mvvmdatabinding.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import java.lang.NullPointerException
import androidx.databinding.library.baseAdapters.BR

abstract class BaseFragment<T : ViewDataBinding, S : BaseViewModel> : Fragment() {

    var binding: T? = null
    lateinit var vm: S

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = getViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding?.let {
            it.setVariable(BR.vm, vm)
            it.lifecycleOwner = viewLifecycleOwner
            it.executePendingBindings()
            return it.root
        }
        throw NullPointerException("DataBinding is null")
    }


    abstract fun getLayoutId(): Int


    abstract fun getViewModel(): S

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}