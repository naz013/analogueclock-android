package com.github.naz013.clockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.naz013.clockapp.Params
import com.github.naz013.clockapp.R
import com.github.naz013.clockapp.data.TimeZoneData
import com.github.naz013.clockapp.databinding.ListItemTimezoneBinding
import com.github.naz013.clockapp.util.takeWithDots

class TimeZoneRecyclerAdapter(
    private val clickListener: (TimeZoneData) -> Unit
) : RecyclerView.Adapter<TimeZoneRecyclerAdapter.ViewHolder>() {

    private var items: List<TimeZoneData> = emptyList()

    fun updateItems(list: List<TimeZoneData>) {
        this.items = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemTimezoneBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) {
            val clockData = items[it]
            clockData.isSelected = !clockData.isSelected
            clickListener.invoke(clockData)
            notifyItemChanged(it)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setClock(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(
        private val binding: ListItemTimezoneBinding,
        private val clickListener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setClock(data: TimeZoneData) {
            binding.timeZoneNameView.text = data.displayName.takeWithDots(Params.MAIN_CLOCK_NAME_LENGTH)
            if (data.isSelected) {
                binding.selectionView.setImageResource(R.drawable.ic_eye)
            } else {
                binding.selectionView.setImageResource(R.drawable.ic_eye_off)
            }
            binding.selectionView.setOnClickListener { clickListener.invoke(adapterPosition) }
        }
    }
}