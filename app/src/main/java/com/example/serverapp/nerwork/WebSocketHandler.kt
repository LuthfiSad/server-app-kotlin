package com.example.serverapp.nerwork

import com.example.serverapp.data.storage.VerificationState
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.ConcurrentHashMap

class WebSocketHandler {
    // Store active WebSocket sessions by code
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    suspend fun handleSession(code: String, session: WebSocketSession) {
        sessions[code] = session
        try {
            // Keep connection open until client disconnects
            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        // Handle incoming text frames if needed
                    }
                    else -> { /* Ignore other frame types */ }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            // Normal client disconnect
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            sessions.remove(code)
        }
    }

    suspend fun notifyClient(code: String) {
        val session = sessions[code] ?: return
        val state = VerificationState.getState(code) ?: return

        val message = """
            {
                "verified": ${state.isVerified},
                "method": ${if (state.selectedMethod != null) "\"${state.selectedMethod}\"" else "null"}
            }
        """.trimIndent()

        session.send(Frame.Text(message))
    }

    suspend fun closeSession(code: String) {
        sessions[code]?.close()
        sessions.remove(code)
    }
}