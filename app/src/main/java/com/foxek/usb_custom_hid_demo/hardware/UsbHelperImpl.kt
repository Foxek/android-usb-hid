package com.foxek.usb_custom_hid_demo.hardware

import android.content.Context
import android.hardware.usb.*
import android.hardware.usb.UsbConstants.USB_DIR_IN

import com.foxek.usb_custom_hid_demo.type.Empty
import com.foxek.usb_custom_hid_demo.type.Result
import com.foxek.usb_custom_hid_demo.type.Error
import java.nio.ByteBuffer

class UsbHelperImpl(context: Context) : UsbHelper {

    companion object {
        private const val COMPLETE = true
        private const val COMPLETE_WITH_ERROR = -1
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private lateinit var usbInterface: UsbInterface
    private lateinit var inRequest: UsbRequest
    private lateinit var usbDevice: UsbDevice

    private var usbInEndpoint: UsbEndpoint? = null
    private var usbOutEndpoint: UsbEndpoint? = null

    private var usbConnection: UsbDeviceConnection? = null

    override fun enumerate(vid: Int, pid: Int, nInterface: Int): Result<Error, Empty> {
        usbDevice = findDevice(vid, pid) ?: return Result.Failure(Error.NoDeviceFoundError)

        usbInterface = usbDevice.getInterface(nInterface)

        for (num in 0 until usbInterface.endpointCount) {
            if (usbInterface.getEndpoint(num).direction == USB_DIR_IN)
                usbInEndpoint = usbInterface.getEndpoint(num)
            else
                usbOutEndpoint = usbInterface.getEndpoint(num)
        }

        return open()
    }

    override fun open(): Result<Error, Empty> {
        usbConnection = usbManager.openDevice(usbDevice)
        val result = usbConnection?.claimInterface(usbInterface, true)

        if ((result == null) or (result != COMPLETE))
            return Result.Failure(Error.ClaimInterfaceError)

        inRequest = UsbRequest()
        inRequest.initialize(usbConnection, usbInEndpoint)

        return Result.Success(Empty())
    }

    override fun close() {
        usbConnection?.let {
            inRequest.close()
            it.releaseInterface(usbInterface)
            it.close()
        }
    }

    override fun writeReport(report: ByteArray): Result<Error, Empty> {
        val result = usbConnection?.bulkTransfer(usbOutEndpoint, report, report.size, 0)

        if ((result == null) or (result == COMPLETE_WITH_ERROR))
            return Result.Failure(Error.UsbConnectionError)

        return Result.Success(Empty())
    }

    @Suppress("DEPRECATION")
    override fun readReport(size: Int): Result<Error, ByteArray> {
        val buffer = ByteBuffer.allocate(size)
        val report = ByteArray(size)

        usbConnection?.let {
            if (inRequest.queue(buffer, size)) {
                usbConnection?.requestWait()

                buffer.rewind()
                buffer.get(report, 0, report.size)
                buffer.clear()
            }
        } ?: return Result.Failure(Error.UsbConnectionError)

        return Result.Success(report)
    }

    override fun isConnected(): Result<Error, Empty> {
        usbConnection ?: return Result.Failure(Error.UsbConnectionError)

        return Result.Success(Empty())
    }

    private fun findDevice(vid: Int, pid: Int): UsbDevice? {
        usbManager.deviceList.values.forEach { device ->
            if ((device.vendorId == vid) and (device.productId == pid)) {
                return device
            }
        }
        return null
    }
}