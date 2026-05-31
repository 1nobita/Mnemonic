package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FutureTwinViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Initial AI greeting
        _messages.value = listOf(
            ChatMessage("Hello. I am FutureTwin. Tell me your plans, choices, or dilemmas, and I will simulate future outcomes for you.", isUser = false)
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(text, isUser = true)
        _messages.value = _messages.value + userMsg
        
        simulaterFuture(text)
    }

    private fun simulaterFuture(prompt: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Formatting conversation history
                val history = _messages.value.map { msg ->
                    val role = if (msg.isUser) "user" else "model"
                    /* The direct REST API format doesn't explicitly have role outside, 
                     * but we can just prepend "User:" and "FutureTwin:" to the prompt for context. 
                     * For a simpler integration, we send the whole chat text as history.
                     */
                    "${if (msg.isUser) "User" else "Future Twin"}: ${msg.text}"
                }.joinToString("\n")

                val fullPrompt = "You are FutureTwin, an AI life simulator. Simluate the consequences of the user's choices based on the following conversation:\n$history\nFuture Twin:"
                
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = fullPrompt)))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = "You are FutureTwin, an AI that predicts the consequences of user decisions, providing both practical outcomes and emotional impact (regret preview). Keep answers concise, insightful, and slightly futuristic.")))
                )
                
                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am unable to see the future right now."
                
                _messages.value = _messages.value + ChatMessage(responseText, isUser = false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
