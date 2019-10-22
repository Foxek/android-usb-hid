package com.foxek.usb_custom_hid_demo.device

import com.foxek.usb_custom_hid_demo.type.Empty
import com.foxek.usb_custom_hid_demo.type.Result
import com.foxek.usb_custom_hid_demo.type.Error
import io.reactivex.Observable

interface CustomDevice {

    fun connect(): Result<Error, Empty>

    fun disconnect()

    fun isConnected(): Result<Error, Empty>

    fun setLedState(state: Boolean): Result<Error, Empty>

    fun receive(): Observable<Result<Error, ByteArray>>
}