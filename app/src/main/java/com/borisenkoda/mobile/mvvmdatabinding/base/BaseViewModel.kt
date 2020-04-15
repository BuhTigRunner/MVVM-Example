package com.borisenkoda.mobile.mvvmdatabinding.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {


    protected fun <X, Y> LiveData<X>.mapWithIO(
        mapFunction: (X) -> Y
    ): LiveData<Y> {
        val result = MediatorLiveData<Y>()

        result.addSource(this, Observer<X> { x ->
            if (x == null) return@Observer
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    result.postValue(mapFunction(x))
                }
            }
        })
        return result
    }
}