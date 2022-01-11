package com.borisenkoda.mobile.mvvmdatabinding.tools.bioauth

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricManager.*
import androidx.security.crypto.MasterKeys
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ScreenNavigator

interface BioAuthService {
    fun isAuthSettingsDone(): Boolean
    fun isDeviceAuthAvailable(): Boolean
    fun openLockScreenSettings()
    fun openBioAuthSettings()
    fun authRequest()
}

@RequiresApi(Build.VERSION_CODES.M)
class BioAuthServiceImpl(
    private val context: Context,
    private val screenNavigator: ScreenNavigator
) : BioAuthService {

    private val biometricManager by lazy {
        from(context)
    }

    private val keyguardManager by lazy {
        context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
    }

    private val masterKey by lazy {
        MasterKeys.getOrCreate(
            KeyGenParameterSpec.Builder(
                "key_alias_1",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setKeySize(256)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setUserAuthenticationRequired(true)
                val timeoutInSec = 10
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setUserAuthenticationParameters(
                    timeoutInSec,
                    KeyProperties.AUTH_DEVICE_CREDENTIAL or KeyProperties.AUTH_BIOMETRIC_STRONG
                ) else {
                    setUserAuthenticationValidityDurationSeconds(timeoutInSec)
                }
            }.build()
        )
    }

    override fun isAuthSettingsDone(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                biometricManager.canAuthenticate(BIOMETRIC_STRONG).let {
                    it == BIOMETRIC_SUCCESS || (keyguardManager.isDeviceSecure)
                }
            } else {
                false
            }
        }
    }


    override fun isDeviceAuthAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    override fun openLockScreenSettings() {
        screenNavigator.openLockScreenSettings()
    }

    override fun openBioAuthSettings() {
        screenNavigator.openBiometricSettings()
    }

    override fun authRequest() {
        screenNavigator.openBiometricAuth(masterKey)
    }

}

