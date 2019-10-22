package com.foxek.usb_custom_hid_demo.device

import com.foxek.usb_custom_hid_demo.hardware.UsbHelper
import com.foxek.usb_custom_hid_demo.type.Empty
import com.foxek.usb_custom_hid_demo.type.Result
import com.foxek.usb_custom_hid_demo.type.Error
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class CustomDeviceImpl(
    private val usbHelper: UsbHelper
) : CustomDevice {

    companion object {
        const val VENDOR_ID = 1155
        const val PRODUCT_ID = 22352
        const val CUSTOM_HID_INTERFACE = 0x00
        const val BUTTON_REPORT_ID = 0x01
        const val LED_REPORT_ID = 0x02
        const val REPORT_SIZE = 2
    }

    override fun connect(): Result<Error, Empty> =
        usbHelper.enumerate(
            VENDOR_ID,
            PRODUCT_ID,
            CUSTOM_HID_INTERFACE
        )

    override fun disconnect() {
        usbHelper.close()
    }

    override fun isConnected(): Result<Error, Empty>  =
        usbHelper.isConnected()

    override fun setLedState(state: Boolean): Result<Error, Empty> {
        val report = ByteArray(2)

        report[0] = LED_REPORT_ID.toByte()
        report[1] = if (state) 1 else 0

        return usbHelper.writeReport(report)
    }

    override fun receive(): Observable<Result<Error, ByteArray>> {
        return Observable.fromCallable<Result<Error, ByteArray>> { usbHelper.readReport(REPORT_SIZE) }
            .subscribeOn(Schedulers.io())
    }
}