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
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 영수증 상품 정보 데이터 클래스
    data class ReceiptItem(
        val name: String = "",
        val price: Int = 0
    )
    // 영수증 정보 데이터 클래스
    data class ReceiptInfo(
        val store: String = "",
        val items: List<ReceiptItem> = emptyList(),
        val totalAmount: Int = 0,
        val date: Long = System.currentTimeMillis()
    )
    // 임시 저장 리스트
    private val receiptList = mutableListOf<ReceiptInfo>()
    private lateinit var receiptAdapter: ReceiptAdapter

    // Firestore 문서 ID와 ReceiptInfo 매핑용 데이터 클래스
    data class ReceiptWithId(val id: String, val info: ReceiptInfo)
    private var receiptWithIdList = mutableListOf<ReceiptWithId>()

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

    // CLOVA OCR REST API 공식 연동 함수 (2024)
    private val CLOVA_OCR_URL = "https://4bjje9ywby.apigw.ntruss.com/custom/v1/42614/f3dc4f524ec262ead5a026fa7a1bb2eb71b36b84d680b7bd9355d73b04ad54b4/document/receipt"
    private val CLOVA_OCR_SECRET = "aVFScGhhd1JLRndhQWlFT3FvbmxMR3BEb1l0cm5XYXI="

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
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)
        receiptAdapter = ReceiptAdapter(emptyList()) { docId ->
            // 삭제 다이얼로그 표시
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("거래 삭제")
                .setMessage("이 거래를 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    deleteReceiptFromFirestore(docId)
                }
                .setNegativeButton("취소", null)
                .show()
        }
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

    // Bitmap을 흑백(Grayscale)으로 변환
    private fun toGrayscale(src: android.graphics.Bitmap): android.graphics.Bitmap {
        val width = src.width
        val height = src.height
        val bmpGrayscale = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmpGrayscale)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(src, 0f, 0f, paint)
        return bmpGrayscale
    }

    // Bitmap을 base64로 변환
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    // CLOVA OCR REST 호출 (Document/Receipt)
    private fun runClovaOcrRest(bitmap: Bitmap, onResult: (String) -> Unit) {
        val imageBase64 = bitmapToBase64(bitmap)
        val json = org.json.JSONObject().apply {
            put("version", "V2")
            put("requestId", java.util.UUID.randomUUID().toString())
            put("timestamp", System.currentTimeMillis())
            put("images", org.json.JSONArray().apply {
                put(org.json.JSONObject().apply {
                    put("name", "sample_image")
                    put("format", "png")
                    put("data", imageBase64)
                })
            })
        }
        val client = okhttp3.OkHttpClient()
        val body = okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = okhttp3.Request.Builder()
            .url(CLOVA_OCR_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-OCR-SECRET", CLOVA_OCR_SECRET)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                requireActivity().runOnUiThread {
                    onResult("CLOVA OCR 실패: ${e.localizedMessage}")
                }
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val result = response.body?.string() ?: ""
                requireActivity().runOnUiThread {
                    onResult(result)
                }
            }
        })
    }

    private fun processImage(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val grayBitmap = toGrayscale(bitmap)
            runClovaOcrRest(grayBitmap) { clovaResult ->
                // CLOVA OCR 결과에서 날짜/총액/가게명/품목 자동 추출 (실제 JSON 구조에 맞게)
                var parsedDate: Long = System.currentTimeMillis()
                var parsedTotal: Int = 0
                var parsedStore: String = ""
                var parsedItems: List<ReceiptItem> = emptyList()
                var dateStrForDisplay = ""
                try {
                    val json = org.json.JSONObject(clovaResult)
                    val images = json.getJSONArray("images")
                    if (images.length() > 0) {
                        val receipt = images.getJSONObject(0).optJSONObject("receipt")
                        val result = receipt?.optJSONObject("result")
                        // 가게명
                        val storeInfo = result?.optJSONObject("storeInfo")
                        val nameObj = storeInfo?.optJSONObject("name")
                        parsedStore = nameObj?.optString("text", "") ?: ""
                        // 날짜
                        val paymentInfo = result?.optJSONObject("paymentInfo")
                        val dateObj = paymentInfo?.optJSONObject("date")
                        val dateStr = dateObj?.optString("text", "") ?: ""
                        dateStrForDisplay = dateStr
                        if (dateStr.isNotEmpty()) {
                            try {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                                parsedDate = sdf.parse(dateStr)?.time ?: parsedDate
                            } catch (_: Exception) {}
                        }
                        // 총액
                        val totalPriceObj = result?.optJSONObject("totalPrice")
                        val priceObj = totalPriceObj?.optJSONObject("price")
                        val totalPriceStr = priceObj?.optString("text", "") ?: ""
                        parsedTotal = totalPriceStr.replace(",", "").toIntOrNull() ?: 0
                        // 품목
                        val subResults = result?.optJSONArray("subResults")
                        val itemList = mutableListOf<ReceiptItem>()
                        if (subResults != null && subResults.length() > 0) {
                            for (i in 0 until subResults.length()) {
                                val sub = subResults.getJSONObject(i)
                                val itemsArr = sub.optJSONArray("items")
                                if (itemsArr != null) {
                                    for (j in 0 until itemsArr.length()) {
                                        val itemObj = itemsArr.getJSONObject(j)
                                        val itemName = itemObj.optJSONObject("name")?.optString("text", "") ?: ""
                                        val itemPriceStr = itemObj.optJSONObject("price")?.optJSONObject("price")?.optString("text", "") ?: ""
                                        val itemPrice = itemPriceStr.replace(",", "").toIntOrNull() ?: 0
                                        if (itemName.isNotBlank()) {
                                            itemList.add(ReceiptItem(itemName, itemPrice))
                                        }
                                    }
                                }
                            }
                        }
                        parsedItems = itemList
                    }
                } catch (e: Exception) {
                    // 파싱 실패 시 무시하고 기본값 사용
                }
                // Firestore에 저장
                val receiptInfo = ReceiptInfo(
                    store = parsedStore,
                    items = parsedItems,
                    totalAmount = parsedTotal,
                    date = parsedDate
                )
                saveReceiptToFirestore(receiptInfo)
                // 예쁘게 포맷팅
                val sb = StringBuilder()
                sb.appendLine("[영수증 인식 결과]")
                sb.appendLine()
                sb.appendLine("가게명: ${if (parsedStore.isNotBlank()) parsedStore else "-"}")
                sb.appendLine("날짜: ${if (dateStrForDisplay.isNotBlank()) dateStrForDisplay else "-"}")
                sb.appendLine("총액: ${if (parsedTotal > 0) String.format("%,d원", parsedTotal) else "-"}")
                sb.appendLine()
                sb.appendLine("[구매 품목]")
                if (parsedItems.isNotEmpty()) {
                    parsedItems.forEach {
                        sb.appendLine("- ${it.name}  ${if (it.price > 0) String.format("%,d원", it.price) else "-"}")
                    }
                } else {
                    sb.appendLine("(품목 정보 없음)")
                }
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("영수증 인식 결과 및 저장 완료")
                    .setMessage(sb.toString() + "\n\n[JSON 원문]\n" + clovaResult)
                    .setPositiveButton("확인", null)
                    .show()
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

    // Firestore에서 거래(영수증) 삭제
    private fun deleteReceiptFromFirestore(docId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show()
                loadRecentTransactions()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "삭제 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // 거래 내역 불러올 때 ReceiptWithId 리스트를 넘김
    private fun loadRecentTransactions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<ReceiptWithId>()
                for (doc in result.documents) {
                    val info = doc.toObject(ReceiptInfo::class.java)
                    if (info != null) {
                        list.add(ReceiptWithId(doc.id, info))
                    }
                }
                receiptWithIdList = list
                receiptAdapter.updateData(list)
                // 이번 달 총액 계산
                val cal = java.util.Calendar.getInstance()
                val thisMonth = cal.get(java.util.Calendar.MONTH)
                val thisYear = cal.get(java.util.Calendar.YEAR)
                val monthlyTotal = list.filter {
                    val c = java.util.Calendar.getInstance()
                    c.timeInMillis = it.info.date
                    c.get(java.util.Calendar.MONTH) == thisMonth && c.get(java.util.Calendar.YEAR) == thisYear
                }.sumOf { it.info.totalAmount }
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