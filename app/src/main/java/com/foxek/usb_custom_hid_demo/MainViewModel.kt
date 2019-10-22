package com.foxek.usb_custom_hid_demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.foxek.usb_custom_hid_demo.device.CustomDeviceImpl
import com.foxek.usb_custom_hid_demo.device.CustomDeviceImpl.Companion.BUTTON_REPORT_ID
import com.foxek.usb_custom_hid_demo.hardware.UsbHelperImpl
import com.foxek.usb_custom_hid_demo.type.Error
import com.foxek.usb_custom_hid_demo.type.Empty

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val customDevice =
        CustomDeviceImpl(UsbHelperImpl(application.applicationContext))
    private val disposable = CompositeDisposable()

    val buttonState = MutableLiveData<Boolean>()
    val usbOperationError = MutableLiveData<Error>()
    val usbOperationSuccess = MutableLiveData<Empty>()

    fun changeLedButtonPressed(state: Boolean) {
        customDevice.setLedState(state).handle(::handleError, ::handleChangeLed)
    }

    fun connectButtonPressed() {
        if (customDevice.isConnected().isSuccess)
            customDevice.disconnect()
        else {
            customDevice.connect().handle(::handleError, ::handleConnect)
        }
    }

    private fun handleError(error: Error) {
        usbOperationError.postValue(error)
    }

    private fun handleChangeLed(success: Empty){
        usbOperationSuccess.postValue(success)
    }

    private fun handleConnect(success: Empty){
        observeUsbRequest()
        usbOperationSuccess.postValue(success)
    }

    private fun handleReport(report: ByteArray){
        when (report[0]) {
            BUTTON_REPORT_ID.toByte() -> handleButtonResponse(report)
            /* handle other report ID*/
        }
    }

    private fun observeUsbRequest() {
        disposable.add(
            customDevice.receive()
                .observeOn(Schedulers.computation())
                .repeat()
                .subscribe({
                    it.handle(::handleError, ::handleReport)
                }, {
                    usbOperationError.postValue(Error.ReadReportError)
                })
        )
    }

    private fun handleButtonResponse(response: ByteArray) {
        if (response[1] == 0.toByte())
            buttonState.postValue(false)
        else
            buttonState.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        customDevice.disconnect()
    }
}
