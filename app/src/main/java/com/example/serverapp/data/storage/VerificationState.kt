package com.example.serverapp.data.storage

import java.util.concurrent.ConcurrentHashMap

object VerificationState {
    // Stores current verification state by code
    private val stateMap = ConcurrentHashMap<String, State>()

    data class State(
        var isVerified: Boolean = false,
        var selectedMethod: String? = null,
        var submittedData: String? = null,
        var isProcessed: Boolean = false
    )

    fun addCode(code: String) {
        stateMap[code] = State()
    }

    fun verifyCode(code: String): Boolean {
        return stateMap.containsKey(code)
    }

    fun setVerified(code: String, verified: Boolean) {
        val state = stateMap[code]
        if (state != null) {
            state.isVerified = verified
            stateMap[code] = state
        }
    }

    fun setMethod(code: String, method: String) {
        val state = stateMap[code]
        if (state != null) {
            state.selectedMethod = method
            stateMap[code] = state
        }
    }

    fun setSubmittedData(code: String, data: String) {
        val state = stateMap[code]
        if (state != null) {
            state.submittedData = data
            state.isProcessed = true
            stateMap[code] = state
        }
    }

    fun getState(code: String): State? {
        return stateMap[code]
    }

    fun resetState(code: String) {
        stateMap.remove(code)
    }

    fun getAllCodes(): List<String> {
        return stateMap.keys().toList()
    }
}