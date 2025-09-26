package com.example.muzpleer.model.cut

sealed  class ProcessingState {
    object Loading : ProcessingState()
    data class Success(val message: String) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}