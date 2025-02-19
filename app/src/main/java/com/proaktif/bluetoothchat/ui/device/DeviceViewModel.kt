package com.proaktif.bluetoothchat.ui.device

import BluetoothRepository
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceViewModel(private val repository: BluetoothRepository) : ViewModel() {
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    fun loadDevices() {
        viewModelScope.launch {
            repository.getPairedDevices()
            _devices.value = repository.pairedDevices.value
        }
    }
}