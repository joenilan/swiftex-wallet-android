package io.horizontalsystems.bankwallet.modules.notifications

import io.horizontalsystems.bankwallet.core.INotificationManager
import io.horizontalsystems.bankwallet.core.managers.BackgroundManager
import io.horizontalsystems.bankwallet.core.managers.PriceAlertManager
import io.horizontalsystems.bankwallet.entities.PriceAlert

class NotificationsInteractor(
        private val priceAlertManager: PriceAlertManager,
        backgroundManager: BackgroundManager,
        private val notificationManager: INotificationManager
) : NotificationsModule.IInteractor, BackgroundManager.Listener {

    init {
        backgroundManager.registerListener(this)
    }

    lateinit var delegate: NotificationsModule.IInteractorDelegate

    override val priceAlertsEnabled: Boolean
        get() = notificationManager.isEnabled

    override val priceAlerts: List<PriceAlert>
        get() = priceAlertManager.getPriceAlerts()

    override fun savePriceAlerts(priceAlerts: List<PriceAlert>) {
        priceAlertManager.savePriceAlerts(priceAlerts)
    }

    override fun willEnterForeground() {
        delegate.didEnterForeground()
    }
}
