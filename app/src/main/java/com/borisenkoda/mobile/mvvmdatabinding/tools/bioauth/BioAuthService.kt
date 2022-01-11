package com.borisenkoda.mobile.mvvmdatabinding.tools.bioauth

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricManager.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.borisenkoda.mobile.mvvmdatabinding.base.navigation.ScreenNavigator
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg
import java.lang.Exception
import java.io.File
import java.security.KeyStore


interface BioAuthService {
    fun isAuthSettingsDone(): Boolean
    fun isDeviceAuthAvailable(): Boolean
    fun openLockScreenSettings()
    fun openBioAuthSettings()
    fun authRequest(okCallBack: () -> Unit)
    fun readValue(key: String): String?
    fun writeValue(key: String, value: String): Boolean
}

private const val FILE_SETTINGS_NAME: String = "qr_settings"

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

    private val keyGenParameterSpec by lazy {
        KeyGenParameterSpec.Builder(
            "key_alias_1",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setKeySize(256)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setUserConfirmationRequired(true)
            }
            val timeoutInSec = 10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setUserAuthenticationParameters(
                timeoutInSec,
                KeyProperties.AUTH_DEVICE_CREDENTIAL or KeyProperties.AUTH_BIOMETRIC_STRONG
            ) else {
                setUserAuthenticationValidityDurationSeconds(timeoutInSec)
            }
        }.build()
    }

    private val masterKey: String by lazy {
        MasterKeys.getOrCreate(keyGenParameterSpec)
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

    override fun authRequest(okCallBack: () -> Unit) {
        screenNavigator.openBiometricAuth(okCallBack)
    }

    override fun readValue(key: String): String? {
        return getSecuredSharedPrefs().getString(key, null)
    }

    override fun writeValue(key: String, value: String): Boolean {
        return try {
            getSecuredSharedPrefs().edit().putString(key, value).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getSecuredSharedPrefs(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                FILE_SETTINGS_NAME,   //xml file name
                masterKey,   //master key
                context,   //context
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  //key encryption technique
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM //value encryption technique
            )
        } catch (e: Exception) {
            Logg.e { "getSecuredSharedPrefs error: $e" }
            deletePreferences()
            deleteKeyStore()
            EncryptedSharedPreferences.create(
                FILE_SETTINGS_NAME,   //xml file name
                masterKey,   //master key
                context,   //context
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  //key encryption technique
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM //value encryption technique
            )
        }
    }

    private fun deleteKeyStore() {
        KeyStore.getInstance("AndroidKeyStore").also {
            it.load(null)
            it.deleteEntry(masterKey)
        }
    }

    private fun deletePreferences() {
        val filePath = "${context.filesDir.parent}/shared_prefs/$FILE_SETTINGS_NAME.xml"
        val deletePrefFile = File(filePath)
        deletePrefFile.delete()
    }
}

