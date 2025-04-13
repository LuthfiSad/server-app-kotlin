package com.example.serverapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.serverapp.databinding.ActivityInputCodeBinding
import com.example.serverapp.viewmodel.MainViewModel

class InputCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputCodeBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text.toString().trim()

            if (code.isEmpty()) {
                showError("Please enter a verification code")
                return@setOnClickListener
            }

            if (viewModel.addCode(code)) {
                // Code added successfully, proceed to method selection
                val intent = Intent(this, MethodSelectActivity::class.java)
                intent.putExtra("code", code)
                startActivity(intent)
                finish()
            } else {
                showError("This code already exists. Please try another code or check active codes.")
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}