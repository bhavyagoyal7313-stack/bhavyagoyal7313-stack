package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DoubtRepository
import com.example.data.local.DoubtEntity
import com.example.data.remote.TeacherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AskUiState {
    object Idle : AskUiState
    object Loading : AskUiState
    data class Success(val response: TeacherResponse) : AskUiState
    data class Error(val message: String) : AskUiState
}

class TutorViewModel(private val repository: DoubtRepository) : ViewModel() {

    private val _studentClass = MutableStateFlow("Class 10")
    val studentClass: StateFlow<String> = _studentClass.asStateFlow()

    private val _board = MutableStateFlow("CBSE")
    val board: StateFlow<String> = _board.asStateFlow()

    private val _subject = MutableStateFlow("Science")
    val subject: StateFlow<String> = _subject.asStateFlow()

    private val _questionText = MutableStateFlow("")
    val questionText: StateFlow<String> = _questionText.asStateFlow()

    private val _isHinglish = MutableStateFlow(false)
    val isHinglish: StateFlow<Boolean> = _isHinglish.asStateFlow()

    private val _askUiState = MutableStateFlow<AskUiState>(AskUiState.Idle)
    val askUiState: StateFlow<AskUiState> = _askUiState.asStateFlow()

    private val _selectedDoubt = MutableStateFlow<DoubtEntity?>(null)
    val selectedDoubt: StateFlow<DoubtEntity?> = _selectedDoubt.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Retrieve all doubts and filter by search query if present
    val historyDoubts: StateFlow<List<DoubtEntity>> = repository.allDoubts
        .combine(_searchQuery) { doubts, query ->
            if (query.isBlank()) {
                doubts
            } else {
                doubts.filter {
                    it.question.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true) ||
                    it.conceptExplanation.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedDoubts: StateFlow<List<DoubtEntity>> = repository.bookmarkedDoubts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStudentClass(value: String) { _studentClass.value = value }
    fun setBoard(value: String) { _board.value = value }
    fun setSubject(value: String) { _subject.value = value }
    fun setQuestionText(value: String) { _questionText.value = value }
    fun toggleHinglish() { _isHinglish.value = !_isHinglish.value }
    fun setSearchQuery(value: String) { _searchQuery.value = value }

    fun selectDoubt(doubt: DoubtEntity?) {
        _selectedDoubt.value = doubt
        if (doubt != null) {
            _askUiState.value = AskUiState.Success(
                TeacherResponse(
                    conceptExplanation = doubt.conceptExplanation,
                    stepByStepAnswer = doubt.stepByStepAnswer,
                    finalAnswer = doubt.finalAnswer,
                    examTips = doubt.examTips,
                    commonMistakes = doubt.commonMistakes
                )
            )
        } else {
            _askUiState.value = AskUiState.Idle
        }
    }

    fun resetState() {
        _selectedDoubt.value = null
        _askUiState.value = AskUiState.Idle
    }

    fun submitQuestion() {
        val q = _questionText.value.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _askUiState.value = AskUiState.Loading
            _selectedDoubt.value = null
            try {
                val response = repository.askGuruJi(
                    studentClass = _studentClass.value,
                    board = _board.value,
                    subject = _subject.value,
                    question = q,
                    isHinglish = _isHinglish.value
                )
                _askUiState.value = AskUiState.Success(response)
            } catch (e: Exception) {
                _askUiState.value = AskUiState.Error(e.localizedMessage ?: "Something went wrong. Please check your internet connection or API Key, Beta.")
            }
        }
    }

    fun toggleBookmark(doubt: DoubtEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(doubt)
            if (_selectedDoubt.value?.id == doubt.id) {
                _selectedDoubt.value = _selectedDoubt.value?.copy(isBookmarked = !doubt.isBookmarked)
            }
        }
    }

    fun deleteDoubt(id: Int) {
        viewModelScope.launch {
            repository.deleteDoubtById(id)
            if (_selectedDoubt.value?.id == id) {
                _selectedDoubt.value = null
                _askUiState.value = AskUiState.Idle
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _selectedDoubt.value = null
            _askUiState.value = AskUiState.Idle
        }
    }
}

class TutorViewModelFactory(private val repository: DoubtRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
