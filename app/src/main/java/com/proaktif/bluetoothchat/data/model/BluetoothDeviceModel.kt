package com.proaktif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceModel(
    val name: String?,
    val address: String,
    val device: BluetoothDevice
)