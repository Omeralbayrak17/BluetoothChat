package com.proaktif.bluetoothchat.ui.device

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun DeviceListScreen(viewModel: DeviceViewModel, onDeviceSelected: (BluetoothDevice) -> Unit) {
    val devices by viewModel.devices.collectAsState()

    LazyColumn {
        items(devices) { device ->
            Button(onClick = { onDeviceSelected(device) }) {
                Text(text = device.name ?: "Bilinmeyen Cihaz")
            }
        }
    }
}