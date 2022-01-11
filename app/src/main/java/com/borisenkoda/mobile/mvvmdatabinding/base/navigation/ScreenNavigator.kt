package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.borisenkoda.mobile.mvvmdatabinding.R
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.Failure
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.FailureInterpreter
import android.content.Context
import android.os.Build
import android.provider.Settings.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg
import java.lang.System


interface ScreenNavigator {
    fun openAlertDialog(failure: Failure)
    fun openSuccessDialog(okCallBack: () -> Unit)
    fun openBiometricSettings()
    fun openBiometricAuth(masterKey: String)
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

    override fun openBiometricAuth(masterKey: String) {
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
                            Logg.d { "onAuthenticationSucceeded " + result.cryptoObject }
                            // now it's safe to init the signature using the password key
                            //decrypt(signature, keyEntry.privateKey)
                            decrypt(this@apply, masterKey)
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

    private fun decrypt(context: Context, masterKey: String) {
        val securedSharedPrefsPasVers = EncryptedSharedPreferences.create(
            "values_secured",   //xml file name
            masterKey,   //master key
            context,   //context
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  //key encryption technique
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM //value encryption technique
        )

        Logg.d { "sigStr last value: " + securedSharedPrefsPasVers.getString("key", "nothing") }
        securedSharedPrefsPasVers.edit().apply {
            putString("key", "last time: ${System.currentTimeMillis().toString()}")
        }.apply()

    }

    override fun openLockScreenSettings() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                startActivity(Intent(ACTION_SECURITY_SETTINGS))
            }
        }
    }
}


