package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import androidx.lifecycle.MutableLiveData
import com.borisenkoda.mobile.mvvmdatabinding.base.BaseActivity
import java.lang.ref.WeakReference


class ForegroundActivityProvider {

    val onPauseStateLiveData = MutableLiveData(false)

    private var weekReference: WeakReference<BaseActivity<*, *>>? = null

    fun setActivity(baseMainActivity: BaseActivity<*, *>) {
        weekReference = WeakReference(baseMainActivity)
        onPauseStateLiveData.value = false
    }

    fun clear() {
        onPauseStateLiveData.value = true
        weekReference?.clear()
        weekReference = null
    }

    fun getActivity(): BaseActivity<*, *>? {
        return weekReference?.get()
    }

    companion object {
        val  instance : ForegroundActivityProvider by lazy {
            ForegroundActivityProvider()
        }
    }

}
