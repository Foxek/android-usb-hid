package com.foxek.usb_custom_hid_demo.type

sealed class Error {

    object UsbConnectionError : Error()
    object ClaimInterfaceError : Error()
    object NoDeviceFoundError : Error()
    object ReadReportError : Error()
}