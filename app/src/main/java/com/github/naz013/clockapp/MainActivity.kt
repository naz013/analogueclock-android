package com.github.naz013.clockapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.naz013.clockapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel = MainActivityViewModel()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsButton.setOnClickListener { showSettings() }
        binding.addButton.setOnClickListener { viewModel.loadTimeZones() }
        binding.uiModeSwitch.setOnClickListener { viewModel.toggleUiMode() }

        lifecycle.addObserver(viewModel)

        viewModel.time.observe(this) { binding.clockView.setMillis(it) }
        viewModel.mainClock.observe(this) { showMainClock(it) }
        viewModel.uiMode.observe(this) { updateUiMode(it) }
    }

    private fun updateUiMode(uiMode: Int) {
        AppCompatDelegate.setDefaultNightMode(uiMode)
        when (uiMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                binding.uiModeSwitch.setImageResource(R.drawable.ic_sun)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                binding.uiModeSwitch.setImageResource(R.drawable.ic_moon)
            }
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                binding.uiModeSwitch.setImageResource(R.drawable.ic_android)
            }
        }
    }

    private fun showMainClock(data: ClockData) {
        binding.timeView.text = data.time
        binding.clockNameView.text = data.location + " | " + data.timeZone
        binding.amPmView.text = data.amPm
    }

    private fun showSettings() {

    }
}