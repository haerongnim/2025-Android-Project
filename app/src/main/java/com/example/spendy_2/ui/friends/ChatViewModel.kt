package com.example.spendy_2.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class ChatViewModel : ViewModel() {
    
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _friendName = MutableLiveData<String>()
    val friendName: LiveData<String> = _friendName
    
    private val _friendPhone = MutableLiveData<String>()
    val friendPhone: LiveData<String> = _friendPhone
    
    private val chatMessages = mutableListOf<ChatMessage>()
    
    fun setFriendInfo(name: String, phone: String) {
        _friendName.value = name
        _friendPhone.value = phone
    }
    
    fun loadMockMessages() {
        if (chatMessages.isEmpty()) {
            val mockMessages = listOf(
                ChatMessage("안녕! 오늘 지출 어떻게 됐어?", System.currentTimeMillis() - 240000, true),
                ChatMessage("오늘 점심에 15000원 썼어", System.currentTimeMillis() - 180000, false)
            )
            chatMessages.addAll(mockMessages)
            _messages.value = chatMessages.toList()
        }
    }
    
    fun sendMessage(messageText: String) {
        val myMessage = ChatMessage(messageText, System.currentTimeMillis(), true)
        chatMessages.add(myMessage)
        _messages.value = chatMessages.toList()
    }
    
    fun addFriendResponse(response: String) {
        val friendMessage = ChatMessage(response, System.currentTimeMillis(), false)
        chatMessages.add(friendMessage)
        _messages.value = chatMessages.toList()
    }
    
    fun getMessages(): List<ChatMessage> = chatMessages.toList()
    
    data class ChatMessage(val message: String, val timestamp: Long, val isFromMe: Boolean)
} 