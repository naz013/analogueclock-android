package com.github.naz013.clockapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.naz013.clockapp.adapter.BiDirectionalContract
import com.github.naz013.clockapp.adapter.ClockRecyclerAdapter
import com.github.naz013.clockapp.adapter.TimeZoneDataProvider
import com.github.naz013.clockapp.adapter.TimeZoneRecyclerAdapter
import com.github.naz013.clockapp.animation.TextAnimator
import com.github.naz013.clockapp.animation.TimeAnimator
import com.github.naz013.clockapp.data.ClockData
import com.github.naz013.analoguewatch.TimeData
import com.github.naz013.clockapp.data.TimeZoneData
import com.github.naz013.clockapp.databinding.ActivityMainBinding
import com.github.naz013.clockapp.databinding.DialogSettingsBinding
import com.github.naz013.clockapp.databinding.DialogTimeZonesBinding
import com.github.naz013.clockapp.util.takeWithDots
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel = MainActivityViewModel()
    private val adapter = ClockRecyclerAdapter {
        viewModel.onItemClick(it)
    }
    private val textAnimator = TextAnimator {
        binding.timeView.text = it
    }
    private val timeAnimator = TimeAnimator({ binding.clockView.setTime(it) }) {
        viewModel.resumeTimer()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsButton.setOnClickListener { showSettings() }
        binding.addButton.setOnClickListener { viewModel.loadTimeZones() }
        binding.uiModeSwitch.setOnClickListener { viewModel.toggleUiMode() }

        binding.clocksList.adapter = adapter
        binding.clocksList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lifecycle.addObserver(viewModel)
        lifecycle.addObserver(textAnimator)
        lifecycle.addObserver(timeAnimator)

        viewModel.time.observe(this) { binding.clockView.setTime(it) }
        viewModel.mainClock.observe(this) { showMainClock(it) }
        viewModel.uiMode.observe(this) { updateUiMode(it) }
        viewModel.clocks.observe(this) { adapter.updateItems(it) }
        viewModel.timeZones.observe(this) { showTimeZonesDialog(it) }
        viewModel.animateClock.observe(this) { updateClock(it) }
    }

    private fun updateClock(data: TimeData) {
        val oldTime = binding.clockView.getTime()
        timeAnimator.animate(oldTime, data.copy(second = data.second + 1))
    }

    private fun showTimeZonesDialog(list: List<TimeZoneData>) {
        val dialogTimeZonesBinding = DialogTimeZonesBinding.inflate(layoutInflater)

        val contract = BiDirectionalContract<TimeZoneData, List<TimeZoneData>>()
        val dataProvider = TimeZoneDataProvider(list, viewModel.viewModelScope) {
            contract.notifyChild(it)
        }
        val adapter = TimeZoneRecyclerAdapter {
            viewModel.toggleTimeZone(it)
            contract.notifyParent(it)
        }.apply { updateItems(list) }
        contract.listenChild { dataProvider.updateItem(it) }
        contract.listenParent { adapter.updateItems(it) }

        dialogTimeZonesBinding.timeZoneList.layoutManager = LinearLayoutManager(this)
        dialogTimeZonesBinding.timeZoneList.adapter = adapter
        dialogTimeZonesBinding.searchView.doOnTextChanged { text, _, _, _ ->
            dataProvider.onSearch(text?.toString() ?: "")
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogTimeZonesBinding.root)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .create()
            .show()
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
        val oldTime = binding.timeView.text?.toString() ?: ""
        if (oldTime.isEmpty()) {
            binding.timeView.text = data.time
        } else {
            textAnimator.animate(oldTime, data.time)
        }
        binding.clockNameView.text = data.displayName.takeWithDots(Params.MAIN_CLOCK_NAME_LENGTH)
        binding.amPmView.text = data.amPm ?: "PM"
        if (data.amPm == null) {
            binding.amPmView.visibility = View.INVISIBLE
        } else {
            binding.amPmView.visibility = View.VISIBLE
        }
    }

    private fun showSettings() {
        val dialogSettingsBinding = DialogSettingsBinding.inflate(layoutInflater)

        dialogSettingsBinding.timeFormatSwitch.isChecked = viewModel.is12HourFormatEnabled()
        dialogSettingsBinding.timeFormatSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleHourFormat(isChecked)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogSettingsBinding.root)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .create()
            .show()
    }
}