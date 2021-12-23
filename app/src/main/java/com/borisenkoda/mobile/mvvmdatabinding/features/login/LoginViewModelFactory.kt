package com.borisenkoda.mobile.mvvmdatabinding.features.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.FailureInterpreterImpl
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ForegroundActivityProvider
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ScreenNavigatorImpl
import com.borisenkoda.mobile.mvvmdatabinding.models.UserTestImpl

class LoginViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(
            user = UserTestImpl.instance,
            screenNavigator = ScreenNavigatorImpl(
                foregroundActivityProvider = ForegroundActivityProvider.instance,
                failureInterpreter = FailureInterpreterImpl(appContext)
            ),
            context = appContext
        ) as T
    }
}