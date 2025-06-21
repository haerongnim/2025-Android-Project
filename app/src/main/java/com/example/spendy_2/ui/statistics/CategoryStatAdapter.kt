package com.example.spendy_2.ui.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import com.example.spendy_2.databinding.ItemCategoryStatBinding

class CategoryStatAdapter : RecyclerView.Adapter<CategoryStatAdapter.ViewHolder>() {
    
    private var items = listOf<CategoryStat>()
    
    fun updateData(newItems: List<CategoryStat>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryStatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    class ViewHolder(private val binding: ItemCategoryStatBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: CategoryStat) {
            binding.tvCategoryName.text = item.category
            binding.tvAmount.text = "₩${String.format("%,.0f", item.amount)}"
            binding.tvPercentage.text = "${String.format("%.1f", item.percentage)}%"
            
            // 카테고리별 아이콘 설정
            val iconRes = when (item.category) {
                "식품" -> R.drawable.ic_food
                "외식" -> R.drawable.ic_food
                "음료" -> R.drawable.ic_food
                "생활용품" -> R.drawable.ic_food
                "의류" -> R.drawable.ic_food
                "전자기기" -> R.drawable.ic_food
                "뷰티" -> R.drawable.ic_food
                "명품" -> R.drawable.ic_food
                else -> R.drawable.ic_food
            }
            binding.ivCategoryIcon.setImageResource(iconRes)
        }
    }
} 