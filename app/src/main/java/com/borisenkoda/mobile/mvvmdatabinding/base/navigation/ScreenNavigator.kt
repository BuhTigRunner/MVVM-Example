package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.borisenkoda.mobile.mvvmdatabinding.R
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.Failure
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.FailureInterpreter
import androidx.core.content.ContextCompat.startActivity

import android.app.admin.DevicePolicyManager
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor


interface ScreenNavigator {
    fun openAlertDialog(failure: Failure)
    fun openSuccessDialog(okCallBack: () -> Unit)
    fun openBiometric()
}

class ScreenNavigatorImpl (
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

    override fun openSuccessDialog(okCallBack: () -> Unit) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.success))
                    .setMessage(getString(R.string.auth_success_message))
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ -> okCallBack() }
                    .show()
            }
        }

    }

    override fun openBiometric() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {

                /*val biometricManager = BiometricManager.from(this)
                when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS ->
                        Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                        Log.e("MY_APP_TAG", "No biometric features available on this device.")
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        // Prompts the user to create credentials that your app accepts.
                        val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                        }
                        startActivityForResult(enrollIntent, 100)
                    }
                }*/

                /*val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                this.startActivity(intent)*/

                var executor: Executor
                var biometricPrompt: BiometricPrompt
                var promptInfo: BiometricPrompt.PromptInfo

                executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int,
                                                           errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(applicationContext,
                                "Authentication error: $errString", Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            Toast.makeText(applicationContext,
                                "Authentication succeeded!", Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(applicationContext, "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    })

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for my app")
                    .setSubtitle("Log in using your biometric credential")
                    .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}


