package com.example.spendy_2.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import com.google.android.material.button.MaterialButton

class FriendAdapter(
    private var items: List<FriendsFragment.Contact>,
    private val onAddFriendClick: (FriendsFragment.Contact) -> Unit,
    private val onViewStatsClick: (FriendsFragment.Contact) -> Unit,
    private val onChatClick: (FriendsFragment.Contact) -> Unit
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

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_friend_name)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_friend_status)
        private val tvTotalSpent: TextView = itemView.findViewById(R.id.tv_friend_total_spent)
        private val btnAdd: View? = itemView.findViewById(R.id.btn_add_friend)
        private val btnViewStats: MaterialButton = itemView.findViewById(R.id.btn_view_stats)
        private val btnChat: MaterialButton = itemView.findViewById(R.id.btn_chat)
        
        fun bind(item: FriendsFragment.Contact) {
            tvName.text = item.name
            tvStatus.text = "온라인"
            
            // 각 친구별로 다른 임시 지출 정보
            val mockSpending = when (item.name) {
                "김민수" -> "이번 달 지출: ₩450,000"
                "이지영" -> "이번 달 지출: ₩320,000"
                "박준호" -> "이번 달 지출: ₩280,000"
                "최수진" -> "이번 달 지출: ₩520,000"
                "정현우" -> "이번 달 지출: ₩380,000"
                "한소영" -> "이번 달 지출: ₩290,000"
                "임태현" -> "이번 달 지출: ₩410,000"
                "송미라" -> "이번 달 지출: ₩350,000"
                else -> "이번 달 지출: ₩0"
            }
            tvTotalSpent.text = mockSpending
            
            // 추가 버튼은 친구 목록에서는 숨김
            btnAdd?.visibility = View.GONE
            
            // 통계 보기 버튼 클릭 리스너
            btnViewStats.setOnClickListener {
                onViewStatsClick(item)
            }
            
            // 채팅 버튼 클릭 리스너
            btnChat.setOnClickListener {
                onChatClick(item)
            }
        }
    }
} 