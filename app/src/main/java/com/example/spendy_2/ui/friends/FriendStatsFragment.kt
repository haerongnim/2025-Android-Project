package com.example.spendy_2.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.databinding.FragmentFriendStatsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color
import java.text.NumberFormat
import java.util.*

class FriendStatsFragment : Fragment() {

    private var _binding: FragmentFriendStatsBinding? = null
    private val binding get() = _binding!!
    
    private var friendContact: FriendsFragment.Contact? = null
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val name = it.getString("friend_name", "")
            val phone = it.getString("friend_phone", "")
            friendContact = FriendsFragment.Contact(name, phone)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecyclerView()
        loadFriendStats()
        loadRecentTransactions()
    }

    private fun setupUI() {
        // 뒤로가기 버튼 설정
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // 친구 이름 설정
        binding.tvFriendName.text = "${friendContact?.name}님의 소비 통계"
    }

    private fun setupRecyclerView() {
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(emptyList())
        binding.rvRecentTransactions.adapter = transactionAdapter
    }

    private fun loadFriendStats() {
        // 친구별 임시 통계 데이터
        val friendName = friendContact?.name ?: ""
        val entries = mutableListOf<PieEntry>()
        
        when (friendName) {
            "김민수" -> {
                entries.add(PieEntry(35f, "음식"))
                entries.add(PieEntry(25f, "교통"))
                entries.add(PieEntry(20f, "쇼핑"))
                entries.add(PieEntry(15f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩450,000"
            }
            "이지영" -> {
                entries.add(PieEntry(40f, "음식"))
                entries.add(PieEntry(20f, "교통"))
                entries.add(PieEntry(25f, "쇼핑"))
                entries.add(PieEntry(10f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩320,000"
            }
            "박준호" -> {
                entries.add(PieEntry(30f, "음식"))
                entries.add(PieEntry(30f, "교통"))
                entries.add(PieEntry(15f, "쇼핑"))
                entries.add(PieEntry(20f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩280,000"
            }
            "최수진" -> {
                entries.add(PieEntry(25f, "음식"))
                entries.add(PieEntry(15f, "교통"))
                entries.add(PieEntry(40f, "쇼핑"))
                entries.add(PieEntry(15f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩520,000"
            }
            "정현우" -> {
                entries.add(PieEntry(35f, "음식"))
                entries.add(PieEntry(25f, "교통"))
                entries.add(PieEntry(20f, "쇼핑"))
                entries.add(PieEntry(15f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩380,000"
            }
            "한소영" -> {
                entries.add(PieEntry(45f, "음식"))
                entries.add(PieEntry(20f, "교통"))
                entries.add(PieEntry(20f, "쇼핑"))
                entries.add(PieEntry(10f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩290,000"
            }
            "임태현" -> {
                entries.add(PieEntry(30f, "음식"))
                entries.add(PieEntry(25f, "교통"))
                entries.add(PieEntry(25f, "쇼핑"))
                entries.add(PieEntry(15f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩410,000"
            }
            "송미라" -> {
                entries.add(PieEntry(35f, "음식"))
                entries.add(PieEntry(20f, "교통"))
                entries.add(PieEntry(30f, "쇼핑"))
                entries.add(PieEntry(10f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩350,000"
            }
            else -> {
                entries.add(PieEntry(35f, "음식"))
                entries.add(PieEntry(25f, "교통"))
                entries.add(PieEntry(20f, "쇼핑"))
                entries.add(PieEntry(15f, "엔터테인먼트"))
                entries.add(PieEntry(5f, "기타"))
                binding.tvTotalAmount.text = "총 지출: ₩0"
            }
        }
        
        val dataSet = PieDataSet(entries, "카테고리별 지출")
        dataSet.colors = listOf(
            Color.rgb(255, 99, 132),
            Color.rgb(54, 162, 235),
            Color.rgb(255, 205, 86),
            Color.rgb(75, 192, 192),
            Color.rgb(153, 102, 255)
        )
        
        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    private fun loadRecentTransactions() {
        val friendName = friendContact?.name ?: ""
        val transactions = when (friendName) {
            "김민수" -> listOf(
                Transaction("스타벅스", "음식", 4500, "2024-01-15"),
                Transaction("지하철", "교통", 1350, "2024-01-15"),
                Transaction("올리브영", "쇼핑", 25000, "2024-01-14"),
                Transaction("영화관", "엔터테인먼트", 12000, "2024-01-13")
            )
            "이지영" -> listOf(
                Transaction("맥도날드", "음식", 8500, "2024-01-15"),
                Transaction("버스", "교통", 1200, "2024-01-15"),
                Transaction("편의점", "쇼핑", 3500, "2024-01-14"),
                Transaction("카페", "음식", 5500, "2024-01-13")
            )
            "박준호" -> listOf(
                Transaction("피자헛", "음식", 18000, "2024-01-15"),
                Transaction("택시", "교통", 8000, "2024-01-15"),
                Transaction("서점", "쇼핑", 15000, "2024-01-14"),
                Transaction("PC방", "엔터테인먼트", 3000, "2024-01-13")
            )
            "최수진" -> listOf(
                Transaction("분식집", "음식", 6000, "2024-01-15"),
                Transaction("지하철", "교통", 1350, "2024-01-15"),
                Transaction("ZARA", "쇼핑", 89000, "2024-01-14"),
                Transaction("네일샵", "쇼핑", 45000, "2024-01-13")
            )
            "정현우" -> listOf(
                Transaction("치킨집", "음식", 22000, "2024-01-15"),
                Transaction("버스", "교통", 1200, "2024-01-15"),
                Transaction("마트", "쇼핑", 35000, "2024-01-14"),
                Transaction("노래방", "엔터테인먼트", 15000, "2024-01-13")
            )
            "한소영" -> listOf(
                Transaction("중국집", "음식", 12000, "2024-01-15"),
                Transaction("지하철", "교통", 1350, "2024-01-15"),
                Transaction("약국", "쇼핑", 8000, "2024-01-14"),
                Transaction("카페", "음식", 4500, "2024-01-13")
            )
            "임태현" -> listOf(
                Transaction("햄버거집", "음식", 9500, "2024-01-15"),
                Transaction("택시", "교통", 12000, "2024-01-15"),
                Transaction("전자상가", "쇼핑", 150000, "2024-01-14"),
                Transaction("볼링장", "엔터테인먼트", 25000, "2024-01-13")
            )
            "송미라" -> listOf(
                Transaction("일식집", "음식", 28000, "2024-01-15"),
                Transaction("버스", "교통", 1200, "2024-01-15"),
                Transaction("화장품점", "쇼핑", 65000, "2024-01-14"),
                Transaction("영화관", "엔터테인먼트", 12000, "2024-01-13")
            )
            else -> emptyList()
        }
        
        transactionAdapter.updateData(transactions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 거래내역 데이터 클래스
    data class Transaction(
        val store: String,
        val category: String,
        val amount: Int,
        val date: String
    )

    // 거래내역 어댑터
    inner class TransactionAdapter(
        private var transactions: List<Transaction>
    ) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                com.example.spendy_2.R.layout.item_transaction, parent, false
            )
            return TransactionViewHolder(view)
        }

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            holder.bind(transactions[position])
        }

        override fun getItemCount(): Int = transactions.size

        fun updateData(newTransactions: List<Transaction>) {
            transactions = newTransactions
            notifyDataSetChanged()
        }

        inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvStore: TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_store_name)
            private val tvCategory: TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_category)
            private val tvAmount: TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_amount)
            private val tvDate: TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_date)

            fun bind(transaction: Transaction) {
                tvStore.text = transaction.store
                tvCategory.text = transaction.category
                tvAmount.text = NumberFormat.getCurrencyInstance(Locale.KOREA).format(transaction.amount.toLong())
                tvDate.text = transaction.date
            }
        }
    }
} 