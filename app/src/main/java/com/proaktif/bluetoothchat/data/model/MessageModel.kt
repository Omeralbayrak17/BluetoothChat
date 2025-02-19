package com.proaktif.bluetoothchat.data.model

data class Message(
    val text: String,
    val sender: String,
    val timestamp: Long
)