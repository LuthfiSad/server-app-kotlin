package com.example.serverapp.network

import com.example.serverapp.data.model.VerificationRequest
import com.example.serverapp.data.model.VerificationResult
import com.example.serverapp.data.storage.VerificationState
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class ApiHandler {
    fun configureRouting(application: Application) {
        application.routing {
            // Check code status endpoint
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
                            message = "Invalid code",
                            verified = false
                        )
                    )
                }
            }

            // Submit code endpoint (if needed)
            post("/submitcode") {
                val request = call.receive<VerificationRequest>()
                val code = request.code

                if (code.isNotEmpty()) {
                    VerificationState.addCode(code)
                    call.respond(
                        VerificationResult(
                            success = true,
                            message = "Code registered successfully"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        VerificationResult(
                            success = false,
                            message = "Invalid code format"
                        )
                    )
                }
            }

            // Submit method selection endpoint
            post("/submitmethod") {
                val request = call.receive<VerificationRequest>()
                val code = request.code
                val method = request.method

                if (code.isNotEmpty() && !method.isNullOrEmpty() &&
                    VerificationState.verifyCode(code)) {
                    VerificationState.setMethod(code, method)
                    call.respond(
                        VerificationResult(
                            success = true,
                            message = "Method selected successfully"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        VerificationResult(
                            success = false,
                            message = "Invalid code or method"
                        )
                    )
                }
            }
        }
    }
}