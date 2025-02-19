package com.proaktif.bluetoothchat.ui.home

import BluetoothRepository
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeView(
    modifier: Modifier = Modifier,
    navController: NavController // NavController'ı parametre olarak alıyoruz
) {
    val context = LocalContext.current
    val bluetoothRepository = remember { BluetoothRepository(context) }
    val pairedDevices by bluetoothRepository.pairedDevices.collectAsState()

    LaunchedEffect(Unit) {
        bluetoothRepository.getPairedDevices()
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Eşleşmiş Cihazlar", style = MaterialTheme.typography.headlineSmall)

        if (pairedDevices.isEmpty()) {
            Text(text = "Eşleşmiş cihaz bulunamadı.", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(pairedDevices) { device ->
                    DeviceItem(device) {
                        // Burada connectAndSendMessage'i coroutine ile çağırıyoruz
                        // "GlobalScope.launch" kullanabiliriz veya "LaunchedEffect" ile coroutine başlatabiliriz
                        CoroutineScope(Dispatchers.Main).launch {
                            val isConnected = bluetoothRepository.connectAndSendMessage(device, "Test mesajı")
                            if (isConnected) {
                                // Bağlantı başarılı ise ChatView'a git
                                navController.navigate("chat")
                            } else {
                                Toast.makeText(context, "Cihaza bağlanılamadı.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name ?: "Bilinmeyen Cihaz", style = MaterialTheme.typography.bodyLarge)
            Text(text = device.address, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
