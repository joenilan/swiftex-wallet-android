package io.horizontalsystems.bankwallet.core.managers

import android.app.Activity
import android.app.KeyguardManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import io.horizontalsystems.bankwallet.BuildConfig
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.ISystemInfoManager

class SystemInfoManager : ISystemInfoManager {

    override val appVersion: String = BuildConfig.VERSION_NAME

    private val biometricManager = BiometricManager.from(App.instance)

    override val isSystemLockOff: Boolean
        get() {
            val keyguardManager = App.instance.getSystemService(Activity.KEYGUARD_SERVICE) as KeyguardManager
            return !keyguardManager.isDeviceSecure
        }

    override val biometricAuthSupported: Boolean
        get() = biometricManager.canAuthenticate() == BIOMETRIC_SUCCESS

}
