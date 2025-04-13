package com.example.serverapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.serverapp.data.storage.VerificationState
import com.example.serverapp.server.LocalHttpServer

class MainViewModel : ViewModel() {
    private var server: LocalHttpServer? = null

    private val _serverRunning = MutableLiveData<Boolean>(false)
    val serverRunning: LiveData<Boolean> = _serverRunning

    private val _activeCodes = MutableLiveData<List<String>>(emptyList())
    val activeCodes: LiveData<List<String>> = _activeCodes

    fun startServer(context: Context) {
        if (server == null) {
            server = LocalHttpServer(context)
            server?.start()
            _serverRunning.value = true
        }
    }

    fun stopServer() {
        server?.stop()
        server = null
        _serverRunning.value = false
    }

    fun refreshActiveCodes() {
        _activeCodes.value = VerificationState.getAllCodes()
    }

    fun addCode(code: String): Boolean {
        if (VerificationState.verifyCode(code)) {
            // Code already exists
            return false
        }

        VerificationState.addCode(code)
        refreshActiveCodes()
        return true
    }

    fun setVerificationMethod(code: String, method: String) {
        VerificationState.setVerified(code, true)
        VerificationState.setMethod(code, method)

        // Notify client of method selection
        server?.notifyClient(code)
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}