package com.foxek.usb_custom_hid_demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.foxek.usb_custom_hid_demo.usb.CustomDevice
import com.foxek.usb_custom_hid_demo.usb.CustomDevice.Companion.BUTTON_REPORT_ID

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val customDevice = CustomDevice(application.applicationContext)
    private val disposable = CompositeDisposable()

    val buttonState = MutableLiveData<Boolean>()
    val usbOperationError = MutableLiveData<Boolean>()

    fun setLedState(state: Boolean) {
        if (!customDevice.setLedState(state))
            usbOperationError.postValue(true)
    }

    fun handleConnection() {
        if (customDevice.isConnected)
            customDevice.disconnect()
        else {
            if (customDevice.connect()) {
                observeUsbRequest()
                usbOperationError.postValue(false)
            } else
                usbOperationError.postValue(true)
        }
    }


    private fun observeUsbRequest() {
        disposable.add(
            customDevice.receive()
                .observeOn(Schedulers.computation())
                .repeat()
                .subscribe({
                    when (it[0]) {
                        BUTTON_REPORT_ID.toByte() -> handleButtonResponse(it)

                        /* handle other report ID*/
                    }

                }, {
                    usbOperationError.postValue(true)
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
