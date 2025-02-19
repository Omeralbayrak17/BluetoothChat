package com.proaktif.bluetoothchat.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()

    Column {
        LazyColumn {
            items(messages) { message ->
                Text("${message.sender}: ${message.text}")
            }
        }
        TextField(
            value = "",
            onValueChange = { viewModel.sendMessage(it) },
            placeholder = { Text("Mesaj yaz...") }
        )
    }
}