package com.proaktif.bluetoothchat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothServer(private val onMessageReceived: (String) -> Unit) {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val serverSocket: BluetoothServerSocket? =
        BluetoothAdapter.getDefaultAdapter()?.listenUsingRfcommWithServiceRecord("BluetoothChat", uuid)

    suspend fun startListening() = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = serverSocket?.accept()
            val inputStream = socket?.inputStream
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                bytes = inputStream?.read(buffer) ?: -1
                if (bytes > 0) {
                    val message = String(buffer, 0, bytes)
                    onMessageReceived(message)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}