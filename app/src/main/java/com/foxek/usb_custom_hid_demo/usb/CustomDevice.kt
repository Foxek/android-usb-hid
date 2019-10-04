package com.foxek.usb_custom_hid_demo.usb

import android.content.Context
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class CustomDevice(context: Context) {

    private val usbHelper = UsbHelper(context)
    var isConnected = false

    companion object {
        const val VENDOR_ID = 1155
        const val PRODUCT_ID = 22352
        const val CUSTOM_HID_INTERFACE = 0x00
        const val BUTTON_REPORT_ID = 0x01
        const val LED_REPORT_ID = 0x02
        const val REPORT_SIZE = 2
    }

    fun connect(): Boolean {
        isConnected = usbHelper.enumerate()
        return isConnected
    }

    fun disconnect() {
        usbHelper.close()
        isConnected = false
    }

    fun setLedState(state: Boolean): Boolean {
        if (!isConnected)
            return false

        val report = ByteArray(2)

        report[0] = LED_REPORT_ID.toByte()
        report[1] = if (state) 1 else 0

        usbHelper.sendReport(report)

        return true
    }

    fun receive(): Observable<ByteArray> {
        return Observable.fromCallable<ByteArray> { usbHelper.getReport() }
            .subscribeOn(Schedulers.io())
    }

}