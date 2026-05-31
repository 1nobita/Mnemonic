package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryEntity
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MnemViewModel(private val repository: MemoryRepository) : ViewModel() {
    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMemory(title: String, mood: String, notes: String, location: String = "Unknown") {
        viewModelScope.launch {
            repository.insert(
                MemoryEntity(
                    title = title,
                    mood = mood,
                    notes = notes,
                    location = location
                )
            )
        }
    }

    fun deleteMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

class MnemViewModelFactory(private val repository: MemoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MnemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MnemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
