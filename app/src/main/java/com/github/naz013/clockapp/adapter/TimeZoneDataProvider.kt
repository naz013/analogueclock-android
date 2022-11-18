package com.github.naz013.clockapp.adapter

import com.github.naz013.clockapp.data.TimeZoneData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimeZoneDataProvider(
    private val initData: List<TimeZoneData>,
    private val scope: CoroutineScope,
    private val updateListener: (List<TimeZoneData>) -> Unit
) {

    fun onSearch(text: String) {
        val notSymbols = text.replace("+","").replace("-","")
        if (notSymbols.isEmpty()) {
            updateListener.invoke(initData)
            return
        }
        val upper = notSymbols.uppercase().toRegex()
        scope.launch {
            val filtered = withContext(Dispatchers.Default) {
                initData.filter {
                    it.displayName.uppercase().contains(upper) ||
                            it.shortName.uppercase().contains(upper)
                }
            }
            withContext(Dispatchers.Main) {
                updateListener.invoke(filtered)
            }
        }
    }

    fun updateItem(data: TimeZoneData) {
        scope.launch {
            withContext(Dispatchers.Default) {
                initData.filter { it.timeZone.id == data.timeZone.id }.forEach {
                    it.isSelected = data.isSelected
                }
            }
        }
    }
}