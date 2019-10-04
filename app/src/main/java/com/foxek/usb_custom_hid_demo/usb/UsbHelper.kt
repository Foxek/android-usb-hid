package com.foxek.usb_custom_hid_demo.usb

import android.content.Context
import android.hardware.usb.*
import android.hardware.usb.UsbConstants.USB_DIR_IN
import com.foxek.usb_custom_hid_demo.usb.CustomDevice.Companion.CUSTOM_HID_INTERFACE
import com.foxek.usb_custom_hid_demo.usb.CustomDevice.Companion.PRODUCT_ID
import com.foxek.usb_custom_hid_demo.usb.CustomDevice.Companion.REPORT_SIZE
import com.foxek.usb_custom_hid_demo.usb.CustomDevice.Companion.VENDOR_ID
import java.nio.ByteBuffer

class UsbHelper(context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var usbConnection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var usbRequest: UsbRequest? = null
    private var usbInEndpoint: UsbEndpoint? = null
    private var usbOutEndpoint: UsbEndpoint? = null

    fun enumerate(): Boolean {
        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
            if ((device.vendorId == VENDOR_ID) and (device.productId == PRODUCT_ID)) {
                usbInterface = device.getInterface(CUSTOM_HID_INTERFACE)

                for (idx in 0..usbInterface!!.endpointCount) {
                    if (usbInterface?.getEndpoint(idx)?.direction == USB_DIR_IN)
                        usbInEndpoint = usbInterface?.getEndpoint(idx)
                    else
                        usbOutEndpoint = usbInterface?.getEndpoint(idx)
                }

                usbConnection = usbManager.openDevice(device)
                usbConnection?.claimInterface(usbInterface, true)

                usbRequest = UsbRequest()
                usbRequest?.initialize(usbConnection, usbInEndpoint)
            }
        }

        return usbConnection != null
    }

    fun sendReport(data: ByteArray) {
        usbConnection?.bulkTransfer(usbOutEndpoint, data, data.size, 0)
    }

    fun getReport(): ByteArray {
        val buffer = ByteBuffer.allocate(REPORT_SIZE)
        val report = ByteArray(buffer.remaining())

        if (usbRequest!!.queue(buffer, REPORT_SIZE)) {
            usbConnection?.requestWait()

            buffer.rewind()
            buffer.get(report, 0, report.size)
            buffer.clear()
        }

        return report
    }

    fun close() {
        usbRequest?.close()
        usbConnection?.releaseInterface(usbInterface)
        usbConnection?.close()
    }
}