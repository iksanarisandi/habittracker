package com.habittracker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.habittracker.R
import com.habittracker.util.NotificationHelper

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        view.findViewById<Button>(R.id.btn_test_reminder).setOnClickListener {
            NotificationHelper.showNotification(requireContext(), "Test Habit", 999)
            Toast.makeText(requireContext(), "Sent test notification", Toast.LENGTH_SHORT).show()
        }
        
        return view
    }
}
