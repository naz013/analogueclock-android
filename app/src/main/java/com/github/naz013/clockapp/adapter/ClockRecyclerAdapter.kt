package com.github.naz013.clockapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.naz013.clockapp.Params
import com.github.naz013.clockapp.data.ClockData
import com.github.naz013.clockapp.databinding.ListItemClockBinding
import com.github.naz013.clockapp.util.takeWithDots

class ClockRecyclerAdapter(
    private val clickListener: (ClockData) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var items: List<ClockData> = emptyList()

    fun updateItems(list: List<ClockData>) {
        this.items = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemClockBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) { clickListener.invoke(items[it]) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setClock(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolder(
    private val binding: ListItemClockBinding,
    private val clickListener: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun setClock(clockData: ClockData) {
        binding.timeView.text = clockData.time
        binding.clockNameView.text = clockData.displayName.takeWithDots(Params.CLOCK_NAME_LENGTH)
        binding.amPmView.text = clockData.amPm
        binding.offsetView.text = clockData.offset
        if (clockData.amPm == null) {
            binding.amPmView.visibility = View.INVISIBLE
        } else {
            binding.amPmView.visibility = View.VISIBLE
        }
        binding.root.setOnClickListener { clickListener.invoke(adapterPosition) }
    }
}