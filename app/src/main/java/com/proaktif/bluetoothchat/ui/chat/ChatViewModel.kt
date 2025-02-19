package com.proaktif.bluetoothchat.ui.chat

import androidx.lifecycle.ViewModel
import com.proaktif.bluetoothchat.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(message: String) {
        _messages.value += Message(message, "Me", System.currentTimeMillis())
    }

    fun receiveMessage(message: String) {
        _messages.value += Message(message, "Other", System.currentTimeMillis())
    }
}