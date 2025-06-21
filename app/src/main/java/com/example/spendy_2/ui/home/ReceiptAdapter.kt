package com.example.spendy_2.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.spendy_2.ui.home.HomeFragment.ReceiptWithId

class ReceiptAdapter(
    private var items: List<ReceiptWithId>,
    private val onDeleteReceipt: (String) -> Unit,
    private val onItemClick: (ReceiptWithId) -> Unit
) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receipt, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReceiptWithId>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete_receipt)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun bind(item: ReceiptWithId) {
            val info = item.info
            tvStoreName.text = info.store
            tvAmount.text = if (info.totalAmount > 0) "${info.totalAmount}원" else "-"
            tvDate.text = dateFormat.format(Date(info.date))
            
            // 아이템 클릭 이벤트
            itemView.setOnClickListener {
                onItemClick(item)
            }
            
            btnDelete.setOnClickListener {
                onDeleteReceipt(item.id)
            }
        }
    }
} 