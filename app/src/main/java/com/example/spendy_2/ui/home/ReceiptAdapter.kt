package com.example.spendy_2.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import java.text.SimpleDateFormat
import java.util.*

class ReceiptAdapter(private var items: List<HomeFragment.ReceiptInfo>) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receipt, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<HomeFragment.ReceiptInfo>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun bind(item: HomeFragment.ReceiptInfo) {
            tvStoreName.text = item.store
            tvAmount.text = if (item.amount.isNotBlank()) "${item.amount}원" else "-"
            tvDate.text = dateFormat.format(Date(item.date))
        }
    }
} 