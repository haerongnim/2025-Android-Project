package com.example.spendy_2.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R
import com.example.spendy_2.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 영수증 정보 데이터 클래스
    data class ReceiptInfo(
        val store: String = "",
        val amount: String = "",
        val date: Long = System.currentTimeMillis()
    )
    // 임시 저장 리스트
    private val receiptList = mutableListOf<ReceiptInfo>()
    private lateinit var receiptAdapter: ReceiptAdapter

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { processImage(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 임시 URI에서 이미지 처리
            // 실제 구현에서는 임시 파일을 사용해야 합니다
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        loadRecentTransactions()
    }

    private fun setupUI() {
        // 최근 거래내역 RecyclerView 설정
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)
        receiptAdapter = ReceiptAdapter(emptyList())
        binding.rvRecentTransactions.adapter = receiptAdapter
    }

    private fun setupClickListeners() {
        binding.btnScanReceipt.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnGalleryReceipt.setOnClickListener {
            openGallery()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("카메라 권한 필요")
            .setMessage("영수증을 촬영하기 위해 카메라 권한이 필요합니다.")
            .setPositiveButton("권한 허용") { _, _ ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openCamera() {
        // TODO: 카메라 구현
        Toast.makeText(context, "카메라 기능은 추후 구현됩니다", Toast.LENGTH_SHORT).show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun processImage(uri: Uri) {
        // ML Kit OCR 처리 구현
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val ocrText = visionText.text
                    // 총액 추출 (예: 12000, 12,000, ₩12000, 12000원 등)
                    val amountRegex = Regex("""(₩|\\b)[0-9,.]+(원)?""")
                    val amountMatch = amountRegex.find(ocrText)
                    val amount = amountMatch?.value?.replace(Regex("[^0-9]"), "") ?: ""
                    // 상호명 추출 개선: 첫 줄, 두 번째 줄 중 한글 2글자 이상, 숫자/특수문자 적은 쪽
                    val lines = ocrText.lines().filter { it.isNotBlank() }
                    val storeName = when {
                        lines.size >= 2 -> {
                            val candidates = lines.take(2).filter { it.length >= 2 }
                            candidates.minByOrNull { it.count { ch -> ch.isDigit() || !ch.isLetterOrDigit() } } ?: lines[0]
                        }
                        lines.isNotEmpty() -> lines[0]
                        else -> ""
                    }
                    // 데이터 저장
                    val receiptInfo = ReceiptInfo(storeName, amount)
                    receiptList.add(receiptInfo)
                    saveReceiptToFirestore(receiptInfo)
                    // 결과 표시
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("영수증 정보 추출 결과")
                        .setMessage("상호명: $storeName\n총액: $amount 원")
                        .setPositiveButton("확인", null)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "텍스트 인식 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(context, "이미지 처리 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Firestore에 영수증 정보 저장
    private fun saveReceiptToFirestore(receipt: ReceiptInfo) {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts")
            .add(receipt)
            .addOnSuccessListener {
                // 저장 성공 시 최근 내역 새로고침
                loadRecentTransactions()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Firestore 저장 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecentTransactions() {
        // Firestore에서 영수증 내역 불러오기 (최신순 20개)
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val receipts = result.toObjects(ReceiptInfo::class.java)
                receiptAdapter.updateData(receipts)
                // 이번 달 총액 계산
                val cal = java.util.Calendar.getInstance()
                val thisMonth = cal.get(java.util.Calendar.MONTH)
                val thisYear = cal.get(java.util.Calendar.YEAR)
                val monthlyTotal = receipts.filter {
                    val c = java.util.Calendar.getInstance()
                    c.timeInMillis = it.date
                    c.get(java.util.Calendar.MONTH) == thisMonth && c.get(java.util.Calendar.YEAR) == thisYear
                }.sumOf { it.amount.toLongOrNull() ?: 0L }
                binding.tvMonthlyTotal.text = "₩${String.format("%,d", monthlyTotal)}"
            }
            .addOnFailureListener {
                receiptAdapter.updateData(emptyList())
                binding.tvMonthlyTotal.text = "₩0"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 