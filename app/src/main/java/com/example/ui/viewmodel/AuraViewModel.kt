package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.MemoryEntity
import com.example.data.repository.MemoryRepository
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AuraViewModel(private val repository: MemoryRepository) : ViewModel() {
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        generateContextualSuggestions()
    }

    fun generateContextualSuggestions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Get time of day context
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val timeOfDay = when (hour) {
                    in 5..11 -> "Morning"
                    in 12..16 -> "Afternoon"
                    in 17..20 -> "Evening"
                    else -> "Night"
                }

                // Get quick summary of past memories
                val memories = repository.allMemories.firstOrNull() ?: emptyList()
                val topMemories = memories.take(3).map { "${it.mood}: ${it.title}" }.joinToString(", ")

                val prompt = "You are Aura, an ambient Android co-pilot. It is currently $timeOfDay. The user's recent memories are: $topMemories. Generate 3 short, proactive, intelligent suggestion cards for the user (e.g., 'Start 45-minute focus mode', 'Review your Proud memory', 'Delay late-night shopping'). Output only the suggestions, separated by a newline."
                
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    )
                )
                
                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                val generatedSuggestions = responseText.split("\n").map { it.trim().removePrefix("-").trim() }.filter { it.isNotEmpty() }
                
                if (generatedSuggestions.isNotEmpty()) {
                    _suggestions.value = generatedSuggestions
                } else {
                    _suggestions.value = listOf("Start Focus Mode", "Take a short walk", "Log a new memory")
                }
            } catch (e: Exception) {
                // Fallback suggestions
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                _suggestions.value = if (hour > 22) {
                    listOf("Sleep mode recommended", "Review your day", "Disconnect for the night")
                } else {
                     listOf("Start Focus Mode", "Take a short walk", "Log a new memory")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class AuraViewModelFactory(private val repository: MemoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
