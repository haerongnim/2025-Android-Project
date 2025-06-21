package com.example.spendy_2.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import com.example.spendy_2.ui.home.HomeFragment.ReceiptItem

class ReceiptDetailAdapter : RecyclerView.Adapter<ReceiptDetailAdapter.ViewHolder>() {
    
    private var items = listOf<ReceiptItem>()
    
    fun updateData(newItems: List<ReceiptItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receipt_detail, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        fun bind(item: ReceiptItem) {
            tvName.text = item.name
            tvPrice.text = "₩${item.price}" // 가격 표시
        }
    }
} 