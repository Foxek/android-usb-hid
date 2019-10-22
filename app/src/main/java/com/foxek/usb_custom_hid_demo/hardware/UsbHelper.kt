package com.foxek.usb_custom_hid_demo.hardware

import com.foxek.usb_custom_hid_demo.type.Empty
import com.foxek.usb_custom_hid_demo.type.Result
import com.foxek.usb_custom_hid_demo.type.Error

interface UsbHelper {

    fun enumerate(vid: Int, pid: Int, nInterface: Int): Result<Error, Empty>

    fun open(): Result<Error, Empty>

    fun close()

    fun isConnected(): Result<Error, Empty>

    fun writeReport(report: ByteArray): Result<Error, Empty>

    fun readReport(size: Int): Result<Error, ByteArray>
}