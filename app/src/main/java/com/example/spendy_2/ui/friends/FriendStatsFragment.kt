package com.example.spendy_2.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.databinding.FragmentFriendStatsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

class FriendStatsFragment : Fragment() {

    private var _binding: FragmentFriendStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendContact: FriendsFragment.Contact
    private lateinit var transactionAdapter: TransactionAdapter

    companion object {
        private const val ARG_FRIEND_CONTACT = "friend_contact"

        fun newInstance(contact: FriendsFragment.Contact): FriendStatsFragment {
            val fragment = FriendStatsFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_CONTACT + "_name", contact.name)
            args.putString(ARG_FRIEND_CONTACT + "_phone", contact.phone)
            fragment.arguments = args
            return fragment
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
        
        // 전달받은 친구 정보 설정
        arguments?.let { args ->
            val name = args.getString(ARG_FRIEND_CONTACT + "_name", "친구")
            val phone = args.getString(ARG_FRIEND_CONTACT + "_phone", "")
            friendContact = FriendsFragment.Contact(name, phone)
        } ?: run {
            friendContact = FriendsFragment.Contact("친구", "")
        }

        setupUI()
        setupCharts()
        loadFriendData()
    }

    private fun setupUI() {
        // 친구 정보 설정
        binding.tvFriendName.text = friendContact.name
        binding.tvFriendStatus.text = friendContact.phone

        // 툴바 설정
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbar.title = "${friendContact.name}님의 통계"

        // 거래내역 리사이클러뷰 설정
        binding.rvFriendTransactions.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(emptyList())
        binding.rvFriendTransactions.adapter = transactionAdapter
    }

    private fun setupCharts() {
        // TODO: 차트 기능 임시 비활성화
        // setupLineChart()
        // setupPieChart()
    }

    private fun setupLineChart() {
        // TODO: 차트 기능 임시 비활성화
        /*
        val lineChart = binding.lineChart
        
        // 차트 기본 설정
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            private val months = arrayOf("1월", "2월", "3월", "4월", "5월", "6월", 
                                       "7월", "8월", "9월", "10월", "11월", "12월")
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < months.size) months[value.toInt()] else ""
            }
        }

        // Y축 설정
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return NumberFormat.getCurrencyInstance(Locale.KOREA).format(value.toLong())
            }
        }

        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = true
        */
    }

    private fun setupPieChart() {
        // TODO: 차트 기능 임시 비활성화
        /*
        val pieChart = binding.pieChart
        
        // 차트 기본 설정
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.legend.isEnabled = true
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        pieChart.setHoleColor(android.graphics.Color.WHITE)
        pieChart.setTransparentCircleColor(android.graphics.Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "카테고리"
        */
    }

    private fun loadFriendData() {
        // TODO: Firebase에서 친구의 실제 데이터 로드
        // 임시 데이터로 차트 표시
        loadMockData()
    }

    private fun loadMockData() {
        // 월별 지출 데이터 (임시)
        val monthlyData = listOf(
            Entry(0f, 150000f),
            Entry(1f, 180000f),
            Entry(2f, 220000f),
            Entry(3f, 190000f),
            Entry(4f, 250000f),
            Entry(5f, 280000f),
            Entry(6f, 320000f),
            Entry(7f, 290000f),
            Entry(8f, 260000f),
            Entry(9f, 240000f),
            Entry(10f, 200000f),
            Entry(11f, 180000f)
        )

        // TODO: 차트 데이터 설정 임시 비활성화
        /*
        val lineDataSet = LineDataSet(monthlyData, "월별 지출")
        lineDataSet.color = requireContext().getColor(com.google.android.material.R.color.design_default_color_primary)
        lineDataSet.setCircleColor(requireContext().getColor(com.google.android.material.R.color.design_default_color_primary))
        lineDataSet.lineWidth = 3f
        lineDataSet.circleRadius = 5f
        lineDataSet.setDrawCircleHole(true)
        lineDataSet.valueTextSize = 10f
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillColor = requireContext().getColor(com.google.android.material.R.color.design_default_color_primary)
        lineDataSet.fillAlpha = 50

        val lineData = LineData(lineDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()

        // 카테고리별 지출 데이터 (임시)
        val pieEntries = listOf(
            PieEntry(35f, "식비"),
            PieEntry(25f, "교통"),
            PieEntry(20f, "쇼핑"),
            PieEntry(15f, "문화생활"),
            PieEntry(5f, "기타")
        )

        val pieDataSet = PieDataSet(pieEntries, "카테고리")
        pieDataSet.colors = listOf(
            android.graphics.Color.rgb(255, 99, 132),
            android.graphics.Color.rgb(54, 162, 235),
            android.graphics.Color.rgb(255, 205, 86),
            android.graphics.Color.rgb(75, 192, 192),
            android.graphics.Color.rgb(153, 102, 255)
        )

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(android.graphics.Color.WHITE)

        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
        */

        // 이번 달 총 지출
        val currentMonthTotal = monthlyData.last().y
        binding.tvFriendMonthlyTotal.text = NumberFormat.getCurrencyInstance(Locale.KOREA).format(currentMonthTotal.toLong())

        // 임시 거래내역
        val mockTransactions = listOf(
            Transaction("스타벅스", "식비", 4500, "2024-01-15"),
            Transaction("지하철", "교통", 1350, "2024-01-15"),
            Transaction("올리브영", "쇼핑", 25000, "2024-01-14"),
            Transaction("영화관", "문화생활", 12000, "2024-01-13")
        )
        transactionAdapter.updateData(mockTransactions)
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