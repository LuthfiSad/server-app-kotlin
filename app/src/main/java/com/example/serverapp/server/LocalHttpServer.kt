package com.example.serverapp.server

import android.content.Context
import android.util.Log
import com.example.serverapp.data.model.VerificationRequest
import com.example.serverapp.data.model.VerificationResult
import com.example.serverapp.data.storage.VerificationState
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.close
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocalHttpServer(private val context: Context) {
    private val TAG = "LocalHttpServer"
    private var server: ApplicationEngine? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val webSocketHandler = WebSocketHandler()
    private val serverPort = 8080

    fun start() {
        server = embeddedServer(Netty, port = serverPort) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            install(WebSockets)

            install(StatusPages) {
                exception<Throwable> { cause ->
                    call.respond(HttpStatusCode.InternalServerError, "Internal Server Error: ${cause.message}")
                    Log.e(TAG, "Server error", cause)
                }
            }

            routing {
                // WebSocket endpoint
                webSocket("/ws") {
                    val parameters = call.request.queryParameters
                    val code = parameters["code"]

                    if (code != null && VerificationState.verifyCode(code)) {
                        webSocketHandler.handleSession(code, this)
                    } else {
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid code"))
                    }
                }

                // Check status endpoint
                get("/check") {
                    val code = call.request.queryParameters["code"] ?: ""
                    val state = VerificationState.getState(code)

                    if (state != null) {
                        call.respond(
                            VerificationResult(
                                success = true,
                                message = "Status found",
                                verified = state.isVerified,
                                method = state.selectedMethod
                            )
                        )
                    } else {
                        call.respond(
                            VerificationResult(
                                success = false,
                                message = "Invalid code or not found",
                                verified = false
                            )
                        )
                    }
                }

                // Submit fingerprint data endpoint
                post("/submitfingerprint") {
                    val request = call.receiveParameters()
                    val code = request["code"] ?: ""
                    val data = request["data"] ?: ""

                    if (code.isNotEmpty() && VerificationState.verifyCode(code)) {
                        VerificationState.setSubmittedData(code, data)

                        // Log the fingerprint data
                        logVerificationData(code, "fingerprint", data)

                        call.respond(
                            VerificationResult(
                                success = true,
                                message = "Fingerprint data received successfully"
                            )
                        )
                    } else {
                        call.respond(
                            VerificationResult(
                                success = false,
                                message = "Invalid code or verification failed"
                            )
                        )
                    }
                }

                // Submit NFC data endpoint
                post("/submitnfc") {
                    val request = call.receiveParameters()
                    val code = request["code"] ?: ""
                    val data = request["data"] ?: ""

                    if (code.isNotEmpty() && VerificationState.verifyCode(code)) {
                        VerificationState.setSubmittedData(code, data)

                        // Log the NFC data
                        logVerificationData(code, "nfc", data)

                        call.respond(
                            VerificationResult(
                                success = true,
                                message = "NFC data received successfully"
                            )
                        )
                    } else {
                        call.respond(
                            VerificationResult(
                                success = false,
                                message = "Invalid code or verification failed"
                            )
                        )
                    }
                }
            }
        }

        server?.start(wait = false)
        Log.i(TAG, "Server started on port $serverPort")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        Log.i(TAG, "Server stopped")
    }

    fun notifyClient(code: String) {
        coroutineScope.launch {
            webSocketHandler.notifyClient(code)
        }
    }

    private fun logVerificationData(code: String, method: String, data: String) {
        try {
            val logDir = File(context.filesDir, "logs")
            if (!logDir.exists()) {
                logDir.mkdir()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val logFile = File(logDir, "${timestamp}_${method}_$code.log")

            logFile.writeText("Code: $code\nMethod: $method\nData: $data\nTimestamp: $timestamp")

            Log.i(TAG, "Verification data logged to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log verification data", e)
        }
    }
}