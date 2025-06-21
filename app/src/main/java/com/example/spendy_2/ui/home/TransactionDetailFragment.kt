package com.example.spendy_2.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendy_2.R
import com.example.spendy_2.databinding.FragmentTransactionDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private var currentTransactionId: String? = null
    private var currentMemo: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        currentTransactionId = arguments?.getString("transaction_id")
        
        if (currentTransactionId == null) {
            Toast.makeText(context, "거래 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupUI()
        loadTransactionData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        
        binding.btnEditMemo.setOnClickListener {
            showMemoEditDialog()
        }

        binding.rvItems.layoutManager = LinearLayoutManager(context)
    }

    private fun loadTransactionData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").document(currentTransactionId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val transaction = document.toObject<HomeFragment.ReceiptInfo>()
                    if (transaction != null) {
                        bindDataToViews(transaction)
                    } else {
                         Toast.makeText(context, "거래 정보를 변환할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "거래 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                 Toast.makeText(context, "거래 정보 로딩 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bindDataToViews(transaction: HomeFragment.ReceiptInfo) {
        // 가게명
        binding.tvStoreName.text = transaction.store

        // 총액
        val format = NumberFormat.getCurrencyInstance(Locale.KOREA)
        binding.tvTotalAmount.text = format.format(transaction.totalAmount)

        // 위치
        binding.tvLocation.text = if (transaction.address.isNotEmpty()) transaction.address else "위치 정보 없음"

        // 카테고리
        binding.tvCategory.text = transaction.category
        
        // 메모
        currentMemo = transaction.memo ?: ""
        binding.tvMemo.text = if (currentMemo.isNotEmpty()) currentMemo else "메모 없음"
        
        // 구매 품목
        if (transaction.items.isNotEmpty()) {
            val itemAdapter = TransactionItemAdapter(transaction.items)
            binding.rvItems.adapter = itemAdapter
        }
    }

    private fun showMemoEditDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_memo, null)
        
        val etMemo = dialogView.findViewById<EditText>(R.id.et_memo)
        etMemo.setText(currentMemo)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<View>(R.id.btn_save).setOnClickListener {
            val newMemo = etMemo.text.toString().trim()
            saveMemo(newMemo)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun saveMemo(memo: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").document(currentTransactionId!!)
            .update("memo", memo)
            .addOnSuccessListener {
                currentMemo = memo
                binding.tvMemo.text = if (memo.isNotEmpty()) memo else "메모 없음"
                Toast.makeText(requireContext(), "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "메모 저장 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("거래 삭제")
            .setMessage("이 거래를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteTransaction() {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").document(currentTransactionId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "거래가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "삭제 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 