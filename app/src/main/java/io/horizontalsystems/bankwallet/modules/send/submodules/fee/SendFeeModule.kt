package io.horizontalsystems.bankwallet.modules.send.submodules.fee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.FeeRatePriority
import io.horizontalsystems.bankwallet.core.factories.FeeRateProviderFactory
import io.horizontalsystems.bankwallet.entities.*
import io.horizontalsystems.bankwallet.modules.send.SendModule
import io.horizontalsystems.bankwallet.modules.send.SendModule.AmountInfo
import io.horizontalsystems.bankwallet.modules.send.SendPresenter
import java.math.BigDecimal


object SendFeeModule {

    class InsufficientFeeBalance(val coin: Coin, val coinProtocol: String, val feeCoin: Coin, val fee: CoinValue) : Exception()

    interface View {
        fun setPrimaryFee(feeAmount: String?)
        fun setSecondaryFee(feeAmount: String?)
        fun setInsufficientFeeBalanceError(insufficientFeeBalance: InsufficientFeeBalance?)
        fun setDuration(duration: Long)
        fun setFeePriority(priority: FeeRatePriority)
        fun showFeeRatePrioritySelector(feeRates: List<FeeRateInfoViewItem>)
    }

    interface ViewDelegate {
        fun onViewDidLoad()
        fun onChangeFeeRate(feeRateInfo: FeeRateInfo)
        fun onClickFeeRatePriority()
        fun onClear()
    }

    interface Interactor {
        fun getRate(coinCode: String)
        fun getFeeRates(): List<FeeRateInfo>?
        fun clear()
    }

    interface InteractorDelegate {
        fun onRateFetched(latestRate: Rate?)
    }

    interface FeeModule {
        val isValid: Boolean
        val feeRate: Long
        val primaryAmountInfo: AmountInfo
        val secondaryAmountInfo: AmountInfo?
        val duration: Long?

        fun setFee(fee: BigDecimal)
        fun setAvailableFeeBalance(availableFeeBalance: BigDecimal)
        fun setInputType(inputType: SendModule.InputType)
    }

    interface FeeModuleDelegate {
        fun onUpdateFeeRate(feeRate: Long)
    }

    data class FeeRateInfoViewItem(val feeRateInfo: FeeRateInfo, val selected: Boolean)


    class Factory(private val coin: Coin, private val moduleDelegate: SendPresenter) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            val view = SendFeeView()
            val feeRateProvider = FeeRateProviderFactory.provider(coin)
            val feeCoinData = App.feeCoinProvider.feeCoinData(coin)
            val feeCoin = feeCoinData?.first ?: coin

            val baseCurrency = App.currencyManager.baseCurrency
            val helper = SendFeePresenterHelper(App.numberFormatter, feeCoin, baseCurrency)
            val interactor = SendFeeInteractor(App.rateStorage, feeRateProvider, App.currencyManager)

            val presenter = SendFeePresenter(view, interactor, helper, coin, baseCurrency, feeCoinData)

            interactor.delegate = presenter
            presenter.moduleDelegate = moduleDelegate.feeModuleDelegate
            moduleDelegate.handler.feeModule = presenter

            return presenter as T
        }
    }

}
