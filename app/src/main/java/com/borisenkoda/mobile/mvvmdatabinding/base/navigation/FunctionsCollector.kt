package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import androidx.lifecycle.LiveData
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg

class FunctionsCollector(private val needCollectLiveData: LiveData<Boolean>) {

    private val functions: MutableList<() -> Unit> = mutableListOf()

    init {
        needCollectLiveData.observeForever { needCollect ->
            if (!needCollect) {
                functions.toList().forEach {
                    Logg.d { "que function executed" }
                    it()
                    functions.remove(it)
                }
            }
        }
    }

    fun executeFunction(func: () -> Unit) {
        if (needCollectLiveData.value == true) {
            functions.add(func)
        } else {
            func()
        }
    }


}