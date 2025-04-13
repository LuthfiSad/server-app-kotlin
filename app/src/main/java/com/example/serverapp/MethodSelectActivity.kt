package com.example.serverapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.serverapp.databinding.ActivityMethodSelectBinding
import com.example.serverapp.viewmodel.MainViewModel

class MethodSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMethodSelectBinding
    private lateinit var viewModel: MainViewModel
    private var code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMethodSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Get code from intent
        code = intent.getStringExtra("code") ?: ""
        if (code.isEmpty()) {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvCodeDisplay.text = "Code: $code"

        // Setup click listeners
        binding.cvFingerprint.setOnClickListener {
            selectMethod("fingerprint")
        }

        binding.cvNfc.setOnClickListener {
            selectMethod("nfc")
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun selectMethod(method: String) {
        viewModel.setVerificationMethod(code, method)

        Toast.makeText(
            this,
            "Selected $method verification for code $code",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}