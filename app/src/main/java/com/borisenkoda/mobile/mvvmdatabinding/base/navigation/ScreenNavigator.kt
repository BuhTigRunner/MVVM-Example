package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.borisenkoda.mobile.mvvmdatabinding.R
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.Failure
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.FailureInterpreter
import android.os.Build
import android.provider.Settings.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg


interface ScreenNavigator {
    fun openAlertDialog(failure: Failure)
    fun openMessageDialog(title: String, message: String, okCallBack: () -> Unit)
    fun openSuccessDialog(okCallBack: () -> Unit)
    fun openBiometricSettings()
    fun openBiometricAuth(okCallBack: () -> Unit)
    fun openLockScreenSettings()
    fun showDeprecatedAndroidVersionError()
}

class ScreenNavigatorImpl(
    private val foregroundActivityProvider: ForegroundActivityProvider,
    private val failureInterpreter: FailureInterpreter
) : ScreenNavigator {

    private val functionsCollector: FunctionsCollector by lazy {
        FunctionsCollector(foregroundActivityProvider.onPauseStateLiveData)
    }

    private fun runOrPostpone(function: () -> Unit) {
        functionsCollector.executeFunction(function)
    }


    override fun openAlertDialog(failure: Failure) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(failureInterpreter.getFailureDescription(failure).message)
                    .show()
            }
        }

    }

    override fun showDeprecatedAndroidVersionError() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage("Для активации функции необходима версия Android 6.0 или выше")
                    .show()
            }
        }
    }

    override fun openSuccessDialog(okCallBack: () -> Unit) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.success))
                    .setMessage(getString(R.string.auth_success_message))
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ -> okCallBack() }
                    .show()
            }
        }

    }


    override fun openBiometricSettings() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(Intent(ACTION_BIOMETRIC_ENROLL))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    startActivity(Intent(ACTION_FINGERPRINT_ENROLL))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startActivity(Intent(ACTION_SECURITY_SETTINGS))
                }
            }
        }
    }

    override fun openBiometricAuth(okCallBack: () -> Unit) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                val prompt = BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {
                        // override the required methods...
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Logg.w { "onAuthenticationError $errorCode $errString" }
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            okCallBack()
                            Logg.d { "onAuthenticationSucceeded " + result.cryptoObject }
                            // now it's safe to init the signature using the password key
                            //decrypt(signature, keyEntry.privateKey)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Logg.w { "onAuthenticationFailed" }
                        }
                    })
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Разблокируйте устройство")
                    .setSubtitle("Пожалуйста, введите код для продолжения")
                    .setDeviceCredentialAllowed(true)
                    .build()
                prompt.authenticate(promptInfo)
            }
        }
    }

    override fun openMessageDialog(title: String, message: String, okCallBack: () -> Unit) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ -> okCallBack() }
                    .show()
            }
        }
    }


    override fun openLockScreenSettings() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                startActivity(Intent(ACTION_SECURITY_SETTINGS))
            }
        }
    }
}


