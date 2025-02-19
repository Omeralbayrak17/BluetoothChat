package com.proaktif.bluetoothchat.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothClient(private val device: BluetoothDevice) {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(uuid)

    fun connect(): Boolean {
        return try {
            socket?.connect()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun sendMessage(message: String) {
        try {
            val outputStream: OutputStream? = socket?.outputStream
            outputStream?.write(message.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun close() {
        socket?.close()
    }
}