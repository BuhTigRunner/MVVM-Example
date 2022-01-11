package com.borisenkoda.mobile.mvvmdatabinding.features.login

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.borisenkoda.mobile.mvvmdatabinding.base.BaseViewModel
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.combineLatest
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ScreenNavigator
import com.borisenkoda.mobile.mvvmdatabinding.models.AuthState
import com.borisenkoda.mobile.mvvmdatabinding.models.User
import com.borisenkoda.mobile.mvvmdatabinding.tools.bioauth.BioAuthService
import com.borisenkoda.mobile.mvvmdatabinding.tools.bioauth.BioAuthServiceImpl
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.map
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map


class LoginViewModel(
    private val user: User,
    private val screenNavigator: ScreenNavigator,
    private val context: Context
) :
    BaseViewModel() {


    val bioAuthService: BioAuthService by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BioAuthServiceImpl(context, screenNavigator)
        } else {
            object : BioAuthService {
                override fun isAuthSettingsDone(): Boolean {
                    return false
                }

                override fun isDeviceAuthAvailable(): Boolean {
                    return false
                }

                override fun openLockScreenSettings() {
                    TODO("Not yet implemented")
                }

                override fun openBioAuthSettings() {
                    TODO("Not yet implemented")
                }

                override fun authRequest(okCallBack: () -> Unit) {
                    TODO("Not yet implemented")
                }

                override fun readValue(key: String): String? {
                    TODO("Not yet implemented")
                }

                override fun writeValue(key: String, value: String): Boolean {
                    TODO("Not yet implemented")
                }


            }
        }
    }

    val progressVisibility by lazy {
        user.authState().map { it == AuthState.IN_PROCESS }.asLiveData()
    }

    val qrCodeChecked = MutableLiveData<Boolean>()
    val qrButtonEnabled = MutableLiveData<Boolean>()
    val saveSecretButtonEnabled = qrButtonEnabled.map { it }

    val enabledEditText by lazy {
        progressVisibility.map {
            it != true
        }
    }

    val login = MutableLiveData<String>()

    val password = MutableLiveData<String>()

    val secret = MutableLiveData<String>()

    val enterEnabled by lazy {
        login.combineLatest(password).combineLatest(progressVisibility).map {
            login.value?.isNotBlank() == true && password.value?.isNotBlank() == true && progressVisibility.value != true
        }
    }

    init {
        Logg.d { "ss hasDeviceCredentialCapability: ${hasDeviceCredentialCapability(context)}" }
        Logg.d { "ss hasBiometricWeakCapability: ${hasBiometricWeakCapability(context)}" }
        /*viewModelScope.launch {
            bioAuthService.isAuthSettingsDone().also {
                Logg.d { "isAuthSettingsDone: $it" }
                qrCodeChecked.value = it
                qrButtonEnabled.value = it
            }
        }*/

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

    fun onClickQr() {
        with(bioAuthService) {
            authRequest {
                readValue("key").also {
                    secret.value = it
                }
            }
        }
    }

    fun onClickSaveSecret() {
        with(bioAuthService) {
            authRequest {
                writeValue("key", secret.value ?: "")
                secret.value = ""
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logg.d { "onCleared()" }
    }

    fun hasDeviceCredentialCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(DEVICE_CREDENTIAL)
    }

    fun hasBiometricWeakCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK)
    }

    fun onQrCheckedChanged(isChecked: Boolean) {
        Logg.d { "isChecked: $isChecked" }
        viewModelScope.launch {
            if (isChecked) {
                qrButtonEnabled.value = bioAuthService.isAuthSettingsDone()
                prepareBioAuth()
            } else {
                qrButtonEnabled.value = false
            }
        }

    }

    private fun prepareBioAuth() {
        with(bioAuthService) {
            if (!isAuthSettingsDone()) {
                if (isDeviceAuthAvailable()) {
                    openBioAuthSettings()
                } else {
                    screenNavigator.showDeprecatedAndroidVersionError()
                }
            }
        }
    }


}
