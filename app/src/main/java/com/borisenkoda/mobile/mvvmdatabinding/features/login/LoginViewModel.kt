package com.borisenkoda.mobile.mvvmdatabinding.features.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.borisenkoda.mobile.mvvmdatabinding.base.BaseViewModel
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.combineLatest
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.map
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ScreenNavigator
import com.borisenkoda.mobile.mvvmdatabinding.models.AuthState
import com.borisenkoda.mobile.mvvmdatabinding.models.User
import kotlinx.coroutines.*


class LoginViewModel(private val user: User, private val screenNavigator: ScreenNavigator) :
    BaseViewModel() {

    val progressVisibility by lazy {
        user.authState().map {
            it == AuthState.IN_PROCESS
        }
    }

    val enabledEditText by lazy {
        progressVisibility.map {
            it != true
        }
    }

    val login = MutableLiveData<String>()

    val password = MutableLiveData<String>()

    val enterEnabled by lazy {
        login.combineLatest(password).combineLatest(progressVisibility).map {
            login.value?.isNotBlank() == true && password.value?.isNotBlank() == true && progressVisibility.value != true
        }
    }

    fun onClickEnter() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Logg.d { "current thread: ${Thread.currentThread()}" }
                user.login(login.value!!, password.value!!)
            }.either(
                {
                    Logg.d { "failure: $it, ${Thread.currentThread()}" }
                    screenNavigator.openAlertDialog(it)
                }
            ) {
                Logg.d { "auth success! thread: ${Thread.currentThread()}" }
                screenNavigator.openSuccessDialog {
                    Logg.d { "auth success! ok handled" }
                }
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
        Logg.d { "onCleared()" }
    }


}
