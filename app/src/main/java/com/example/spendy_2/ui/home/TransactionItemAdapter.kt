package com.example.spendy_2.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import java.text.NumberFormat
import java.util.Locale

class TransactionItemAdapter(private val items: List<HomeFragment.ReceiptItem>) :
    RecyclerView.Adapter<TransactionItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_item_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.tv_item_price)

        fun bind(item: HomeFragment.ReceiptItem) {
            nameTextView.text = item.name
            val format = NumberFormat.getCurrencyInstance(Locale.KOREA)
            priceTextView.text = format.format(item.price)
        }
    }
} 