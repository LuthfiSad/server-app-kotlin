package com.example.serverapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverapp.R
import com.example.serverapp.databinding.ActivityMainBinding
import com.example.serverapp.data.storage.VerificationState
import com.example.serverapp.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ActiveCodesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Setup RecyclerView
        adapter = ActiveCodesAdapter { code ->
            // Handle code selection
            val intent = Intent(this, MethodSelectActivity::class.java)
            intent.putExtra("code", code)
            startActivity(intent)
        }

        binding.rvActiveCodes.layoutManager = LinearLayoutManager(this)
        binding.rvActiveCodes.adapter = adapter

        // Setup observers
        viewModel.serverRunning.observe(this) { isRunning ->
            binding.tvStatus.text = "Server Status: ${if (isRunning) "Running" else "Stopped"}"
            binding.btnToggleServer.text = if (isRunning) "Stop Server" else "Start Server"
            binding.btnInputCode.isEnabled = isRunning
        }

        viewModel.activeCodes.observe(this) { codes ->
            adapter.updateCodes(codes)
        }

        // Setup button listeners
        binding.btnToggleServer.setOnClickListener {
            if (viewModel.serverRunning.value == true) {
                viewModel.stopServer()
            } else {
                viewModel.startServer(applicationContext)
            }
        }

        binding.btnInputCode.setOnClickListener {
            startActivity(Intent(this, InputCodeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshActiveCodes()
    }

    // RecyclerView Adapter for active codes
    private inner class ActiveCodesAdapter(private val onCodeSelected: (String) -> Unit) :
        RecyclerView.Adapter<ActiveCodesAdapter.CodeViewHolder>() {

        private var codes: List<String> = emptyList()

        fun updateCodes(newCodes: List<String>) {
            codes = newCodes
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_active_code, parent, false)
            return CodeViewHolder(view)
        }

        override fun getItemCount(): Int = codes.size

        override fun onBindViewHolder(holder: CodeViewHolder, position: Int) {
            val code = codes[position]
            val state = VerificationState.getState(code)

            holder.tvCode.text = code
            holder.tvStatus.text = "Status: ${if (state?.isVerified == true) "Verified" else "Waiting"}"
            holder.tvMethod.text = "Method: ${state?.selectedMethod ?: "Not selected"}"

            holder.btnSelect.setOnClickListener {
                onCodeSelected(code)
            }
        }

        inner class CodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvCode: TextView = itemView.findViewById(R.id.tvCode)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            val tvMethod: TextView = itemView.findViewById(R.id.tvMethod)
            val btnSelect: Button = itemView.findViewById(R.id.btnSelect)
        }
    }
}