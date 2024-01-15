package com.example.wifimonitor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.example.wifimonitor.databinding.FragmentSettingsBinding
import com.example.wifimonitor.utils.isStartOnBootComplete
import com.example.wifimonitor.utils.startOnBootComplete

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.startOnBootComplete.isChecked = requireContext().isStartOnBootComplete
        binding.batteryOptimizations.setOnClickListener {
            //Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            val flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            @SuppressLint("BatteryLife")
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "package:${requireContext().packageName}".toUri())
                .setFlags(flags)
            requireContext().startActivity(intent)
        }
        binding.overlayPermission.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${requireContext().packageName}".toUri())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            requireContext().startActivity(intent)
        }
        binding.startOnBootComplete.setOnCheckedChangeListener { _, isChecked ->
            requireContext().startOnBootComplete(isChecked)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.overlayPermission.isEnabled = !Settings.canDrawOverlays(requireContext())
        val powerManager = requireContext().getSystemService(PowerManager::class.java)
        binding.batteryOptimizations.isEnabled = !powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}