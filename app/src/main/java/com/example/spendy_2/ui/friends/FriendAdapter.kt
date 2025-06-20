package com.example.spendy_2.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R

class FriendAdapter(
    private var items: List<FriendsFragment.Contact>,
    private val onAddFriendClick: (FriendsFragment.Contact) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FriendsFragment.Contact>) {
        items = newItems
        notifyDataSetChanged()
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_friend_name)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_friend_status)
        private val tvTotalSpent: TextView = itemView.findViewById(R.id.tv_friend_total_spent)
        // 추가 버튼은 친구 목록에서는 숨김
        fun bind(item: FriendsFragment.Contact) {
            tvName.text = item.name
            tvStatus.text = item.phone
            tvTotalSpent.text = ""
            val btnAdd = itemView.findViewById<View?>(R.id.btn_add_friend)
            btnAdd?.visibility = View.GONE
        }
    }
} 