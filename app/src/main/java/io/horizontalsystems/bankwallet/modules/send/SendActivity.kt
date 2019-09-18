package io.horizontalsystems.bankwallet.modules.send

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.modules.qrscanner.QRScannerModule
import io.horizontalsystems.bankwallet.modules.send.submodules.address.SendAddressFragment
import io.horizontalsystems.bankwallet.modules.send.submodules.address.SendAddressViewModel
import io.horizontalsystems.bankwallet.modules.send.submodules.amount.SendAmountFragment
import io.horizontalsystems.bankwallet.modules.send.submodules.amount.SendAmountViewModel
import io.horizontalsystems.bankwallet.modules.send.submodules.confirmation.ConfirmationFragment
import io.horizontalsystems.bankwallet.modules.send.submodules.fee.SendFeeFragment
import io.horizontalsystems.bankwallet.modules.send.submodules.fee.SendFeeViewModel
import io.horizontalsystems.bankwallet.modules.send.submodules.memo.SendMemoFragment
import io.horizontalsystems.bankwallet.modules.send.submodules.memo.SendMemoViewModel
import io.horizontalsystems.bankwallet.modules.send.submodules.sendbutton.ProceedButtonView
import io.horizontalsystems.bankwallet.ui.extensions.TopMenuItem
import io.horizontalsystems.bankwallet.viewHelpers.HudHelper
import io.horizontalsystems.bankwallet.viewHelpers.LayoutHelper
import kotlinx.android.synthetic.main.activity_send.*

class SendActivity : BaseActivity() {

    private lateinit var mainPresenter: SendModule.IViewDelegate
    private lateinit var mainViewModel: SendViewModel

    private var proceedButtonView: ProceedButtonView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val wallet: Wallet = intent.getParcelableExtra(WALLET) ?: run { finish(); return }

        val iconRes = LayoutHelper.getCoinDrawableResource(wallet.coin.code)

        shadowlessToolbar.bind(
                title = getString(R.string.Send_Title, wallet.coin.code),
                leftBtnItem = TopMenuItem(iconRes),
                rightBtnItem = TopMenuItem(R.drawable.close, onClick = { onBackPressed() })
        )

        mainViewModel = ViewModelProviders.of(this).get(SendViewModel::class.java)
        mainPresenter = mainViewModel.init(wallet)

        mainPresenter.onViewDidLoad()

        mainViewModel.inputItems.observe(this, Observer { inputItems ->
            addInputItems(wallet, inputItems)
        })


        mainViewModel.showSendConfirmation.observe(this, Observer {
            hideSoftKeyboard()

            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right,
                                         R.anim.slide_in_from_right, R.anim.slide_out_to_right)
                    .add(R.id.rootView, ConfirmationFragment())
                    .addToBackStack("confirmFragment")
                    .commit()
        })

        mainViewModel.closeWithSuccess.observe(this, Observer {
            HudHelper.showSuccessMessage(R.string.Send_Success)
            finish()
        })

        mainViewModel.sendButtonEnabled.observe(this, Observer { enabled ->
            proceedButtonView?.updateState(enabled)
        })

        mainViewModel.scanQrCode.observe(this, Observer {
            QRScannerModule.start(this)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        scanResult?.contents?.let {
            mainViewModel.delegate.onAddressScan(it)
        }
    }

    private fun addInputItems(wallet: Wallet, inputItems: List<SendModule.Input>) {
        inputItems.forEach { input ->
            when (input) {
                SendModule.Input.Amount -> {
                    //add amount view
                    val amountViewModel = ViewModelProviders.of(this).get(SendAmountViewModel::class.java)
                    val amountPresenter = amountViewModel.init(wallet, mainViewModel.amountModuleDelegate)

                    mainPresenter.handler.amountModule = amountPresenter

                    val sendAmountFragment = SendAmountFragment(amountViewModel)
                    supportFragmentManager.beginTransaction().add(R.id.sendLinearLayout, sendAmountFragment).commitNow()
                }
                SendModule.Input.Address -> {
                    //add address view
                    val addressViewModel = ViewModelProviders.of(this).get(SendAddressViewModel::class.java)
                    val addressPresenter = addressViewModel.init(wallet.coin, mainViewModel.addressModuleDelegate)

                    mainPresenter.handler.addressModule = addressPresenter

                    val sendAddressFragment = SendAddressFragment(addressViewModel)
                    supportFragmentManager.beginTransaction().add(R.id.sendLinearLayout, sendAddressFragment)
                            .commitNow()
                }
                is SendModule.Input.Fee -> {
                    //add fee view
                    val feeViewModel = ViewModelProviders.of(this).get(SendFeeViewModel::class.java)
                    val feePresenter = feeViewModel.init(wallet.coin, mainViewModel.feeModuleDelegate)

                    mainPresenter.handler.feeModule = feePresenter

                    val sendFeeFragment = SendFeeFragment(feeViewModel, input.isAdjustable)
                    supportFragmentManager.beginTransaction().add(R.id.sendLinearLayout, sendFeeFragment).commitNow()
                }
                is SendModule.Input.Memo -> {
                    //add memo view
                    val memoViewModel = ViewModelProviders.of(this).get(SendMemoViewModel::class.java)
                    val memoPresenter = memoViewModel.init(input.maxLength)

                    mainPresenter.handler.memoModule = memoPresenter

                    val sendMemoFragment = SendMemoFragment(memoViewModel)
                    supportFragmentManager.beginTransaction().add(R.id.sendLinearLayout, sendMemoFragment).commitNow()
                }
                SendModule.Input.ProceedButton -> {
                    //add send button
                    proceedButtonView = ProceedButtonView(this)
                    proceedButtonView?.bind { mainPresenter.onProceedClicked() }
                    sendLinearLayout.addView(proceedButtonView)
                }
            }
        }

        mainPresenter.onModulesDidLoad()
    }

    companion object {
        const val WALLET = "wallet_key"
    }

}