package com.lantianhcgp.readlater.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.db.entity.Tag
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(tags = tags, isLoading = false) }
            }
        }
    }
}
