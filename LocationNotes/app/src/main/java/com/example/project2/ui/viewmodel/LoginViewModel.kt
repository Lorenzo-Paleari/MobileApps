package com.example.project2.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    companion object { //statico
        private const val CORRECT_PIN = "2137"
    }

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    //accetta input di almeno 4 cifre e devono essere numeri
    fun onPinChange(newPin: String) {
        if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
            _pin.value = newPin
            _errorMessage.value = ""
        }
    }

    //controlla se è giusto
    fun validatePin(onSuccess: () -> Unit) {
        if (_pin.value == CORRECT_PIN) {
            _errorMessage.value = ""
            onSuccess()
        } else {
            _errorMessage.value = "Incorrect PIN"
        }
    }
}

