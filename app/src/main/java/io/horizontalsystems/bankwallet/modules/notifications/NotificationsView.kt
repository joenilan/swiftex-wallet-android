package io.horizontalsystems.bankwallet.modules.notifications

import androidx.lifecycle.MutableLiveData
import io.horizontalsystems.bankwallet.SingleLiveEvent
import io.horizontalsystems.bankwallet.entities.PriceAlert

class NotificationsView : NotificationsModule.IView {
    val itemsLiveData = MutableLiveData<List<NotificationsModule.PriceAlertViewItem>>()
    val toggleWarningLiveData = MutableLiveData<Boolean>()
    val showStateSelectorLiveEvent = SingleLiveEvent<Pair<Int, PriceAlert>>()

    override fun setItems(items: List<NotificationsModule.PriceAlertViewItem>) {
        itemsLiveData.postValue(items)
    }

    override fun showWarning() {
        toggleWarningLiveData.postValue(true)
    }

    override fun hideWarning() {
        toggleWarningLiveData.postValue(false)
    }

    override fun showStateSelector(itemPosition: Int, priceAlert: PriceAlert) {
        showStateSelectorLiveEvent.postValue(Pair(itemPosition, priceAlert))
    }
}
