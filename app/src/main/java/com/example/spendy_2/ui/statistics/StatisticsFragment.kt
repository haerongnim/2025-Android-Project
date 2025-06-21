package com.example.spendy_2.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendy_2.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.spendy_2.ui.home.HomeFragment.ReceiptInfo

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryStatAdapter: CategoryStatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        setupRecyclerView()
        loadStatistics()
    }

    private fun setupCharts() {
        setupLineChart()
        setupPieChart()
    }

    private fun setupLineChart() {
        val lineChart = binding.lineChart
        
        // 차트 기본 설정
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        
        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        
        // Y축 설정
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        
        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false
        
        // 범례 설정
        lineChart.legend.isEnabled = true
    }

    private fun setupPieChart() {
        val pieChart = binding.pieChart
        
        // 차트 기본 설정
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "카테고리"
    }

    private fun setupRecyclerView() {
        binding.rvCategoryStats.layoutManager = LinearLayoutManager(context)
        categoryStatAdapter = CategoryStatAdapter()
        binding.rvCategoryStats.adapter = categoryStatAdapter
    }

    private fun loadStatistics() {
        // Firestore에서 영수증 데이터 불러오기
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").get()
            .addOnSuccessListener { result ->
                val receipts = result.toObjects(ReceiptInfo::class.java)
                loadMonthlyExpenseData(receipts)
                loadCategoryData(receipts)
            }
            .addOnFailureListener {
                // 실패 시 임시 데이터 사용
                loadMonthlyExpenseData(emptyList())
                loadCategoryData(emptyList())
            }
    }

    private fun loadMonthlyExpenseData(receipts: List<ReceiptInfo>) {
        val entries = mutableListOf<Entry>()
        if (receipts.isEmpty()) {
            // 임시 데이터
            entries.add(Entry(1f, 150000f))
            entries.add(Entry(2f, 180000f))
            entries.add(Entry(3f, 120000f))
            entries.add(Entry(4f, 200000f))
            entries.add(Entry(5f, 160000f))
            entries.add(Entry(6f, 140000f))
        } else {
            // 월별 합계 계산
            val monthlyMap = receipts.groupBy {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = it.date
                cal.get(java.util.Calendar.MONTH) + 1 // 1~12월
            }.mapValues { entry ->
                entry.value.map { it.totalAmount.toFloat() }.sum()
            }
            (1..12).forEach { month ->
                val value = monthlyMap[month] ?: 0f
                entries.add(Entry(month.toFloat(), value))
            }
        }
        val dataSet = LineDataSet(entries, "월별 지출")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.BLUE
        dataSet.fillAlpha = 50
        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun loadCategoryData(receipts: List<ReceiptInfo>) {
        val entries = mutableListOf<PieEntry>()
        if (receipts.isEmpty()) {
            // 임시 데이터
            entries.add(PieEntry(35f, "식품"))
            entries.add(PieEntry(25f, "외식"))
            entries.add(PieEntry(20f, "음료"))
            entries.add(PieEntry(15f, "생활용품"))
            entries.add(PieEntry(5f, "기타"))
        } else {
            // 실제 카테고리 필드 사용
            val categoryMap = mutableMapOf<String, Float>()
            val categoryCount = mutableMapOf<String, Int>()
            
            receipts.forEach { receipt ->
                val category = receipt.category.ifEmpty { "기타" }
                val amount = receipt.totalAmount.toFloat()
                
                categoryMap[category] = categoryMap.getOrDefault(category, 0f) + amount
                categoryCount[category] = categoryCount.getOrDefault(category, 0) + 1
            }
            
            // 카테고리별 통계 로그
            android.util.Log.d("Statistics", "카테고리 통계:")
            categoryMap.forEach { (category, totalAmount) ->
                val count = categoryCount[category] ?: 0
                android.util.Log.d("Statistics", "$category: ${String.format("%,.0f", totalAmount)}원 ($count)건")
            }
            
            categoryMap.forEach { (category, amount) ->
                entries.add(PieEntry(amount, category))
            }
            
            // 어댑터 업데이트
            val totalAmount = categoryMap.values.sum()
            val categoryStats = categoryMap.map { (category, amount) ->
                val percentage = if (totalAmount > 0) (amount / totalAmount) * 100 else 0f
                val count = categoryCount[category] ?: 0
                CategoryStat(category, amount, percentage, count)
            }.sortedByDescending { it.amount }
            
            categoryStatAdapter.updateData(categoryStats)
        }
        
        val dataSet = PieDataSet(entries, "카테고리")
        dataSet.colors = listOf(
            Color.rgb(64, 89, 128),   // 식품
            Color.rgb(149, 165, 124), // 외식
            Color.rgb(217, 184, 162), // 음료
            Color.rgb(191, 134, 134), // 생활용품
            Color.rgb(179, 102, 134), // 의류
            Color.rgb(255, 159, 64),  // 전자기기
            Color.rgb(255, 99, 132),  // 뷰티
            Color.rgb(153, 102, 255), // 명품
            Color.rgb(201, 203, 207)  // 기타
        )
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
