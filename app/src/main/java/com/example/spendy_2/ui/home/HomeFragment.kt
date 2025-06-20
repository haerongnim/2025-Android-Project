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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val date: Long = System.currentTimeMillis(),
        val address: String = "",
        val lat: Double = 0.0, // 위도
        val lng: Double = 0.0  // 경도
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
                        // 주소
                        val addressesArr = storeInfo?.optJSONArray("addresses")
                        var parsedAddress = ""
                        if (addressesArr != null && addressesArr.length() > 0) {
                            parsedAddress = addressesArr.getJSONObject(0).optString("text", "")
                        }
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
                        // 주소 → 좌표 변환 (코루틴 launch)
                        if (parsedAddress.isNotBlank()) {
                            GlobalScope.launch(Dispatchers.Main) {
                                // 실제 Geocoding API 사용 (정확한 위치)
                                val (lat, lng) = kotlinx.coroutines.withContext(Dispatchers.IO) {
                                    getLatLngFromAddress(parsedAddress)
                                }
                                
                                // Geocoding 실패 시 테스트용 좌표 사용
                                val finalLat = if (lat == 0.0 && lng == 0.0) {
                                    getTestLatLngFromAddress(parsedAddress).first
                                } else lat
                                val finalLng = if (lat == 0.0 && lng == 0.0) {
                                    getTestLatLngFromAddress(parsedAddress).second
                                } else lng
                                
                                val receiptInfo = ReceiptInfo(
                                    store = parsedStore,
                                    items = parsedItems,
                                    totalAmount = parsedTotal,
                                    date = parsedDate,
                                    address = parsedAddress,
                                    lat = finalLat,
                                    lng = finalLng
                                )
                                saveReceiptToFirestore(receiptInfo)
                                // 예쁘게 포맷팅
                                val sb = StringBuilder()
                                sb.appendLine("[영수증 인식 결과]")
                                sb.appendLine()
                                sb.appendLine("가게명: ${if (parsedStore.isNotBlank()) parsedStore else "-"}")
                                sb.appendLine("날짜: ${if (dateStrForDisplay.isNotBlank()) dateStrForDisplay else "-"}")
                                sb.appendLine("총액: ${if (parsedTotal > 0) String.format("%,d원", parsedTotal) else "-"}")
                                sb.appendLine("주소: ${if (parsedAddress.isNotBlank()) parsedAddress else "-"}")
                                sb.appendLine("위도: $finalLat, 경도: $finalLng")
                                if (lat == 0.0 && lng == 0.0) {
                                    sb.appendLine("(Geocoding 실패로 테스트 좌표 사용)")
                                } else {
                                    sb.appendLine("(정확한 Geocoding 좌표)")
                                }
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
                            return@runClovaOcrRest
                        }
                        // 주소가 없으면 기존 방식
                        val receiptInfo = ReceiptInfo(
                            store = parsedStore,
                            items = parsedItems,
                            totalAmount = parsedTotal,
                            date = parsedDate,
                            address = parsedAddress,
                            lat = 0.0,
                            lng = 0.0
                        )
                        saveReceiptToFirestore(receiptInfo)
                        // 예쁘게 포맷팅
                        val sb = StringBuilder()
                        sb.appendLine("[영수증 인식 결과]")
                        sb.appendLine()
                        sb.appendLine("가게명: ${if (parsedStore.isNotBlank()) parsedStore else "-"}")
                        sb.appendLine("날짜: ${if (dateStrForDisplay.isNotBlank()) dateStrForDisplay else "-"}")
                        sb.appendLine("총액: ${if (parsedTotal > 0) String.format("%,d원", parsedTotal) else "-"}")
                        sb.appendLine("주소: ${if (parsedAddress.isNotBlank()) parsedAddress else "-"}")
                        sb.appendLine("위도: 0.0, 경도: 0.0")
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
                        return@runClovaOcrRest
                    }
                } catch (e: Exception) {
                    // 파싱 실패 시 무시하고 기본값 사용
                }
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

    // 카카오 Geocoding API를 이용해 주소를 위도/경도로 변환하는 함수
    private suspend fun getLatLngFromAddress(address: String): Pair<Double, Double> {
        return try {
            val apiKey = "KakaoAK a43aa2abb1d988b7e8c15c509e791f3a"
            val url = "https://dapi.kakao.com/v2/local/search/address.json?query=" +
                java.net.URLEncoder.encode(address, "UTF-8")
            val request = okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .build()
            val client = okhttp3.OkHttpClient()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return 0.0 to 0.0

            android.util.Log.d("KakaoGeocoding", "주소: $address\n요청 URL: $url\n응답: $body")

            val json = org.json.JSONObject(body)
            val documents = json.optJSONArray("documents")
            if (documents != null && documents.length() > 0) {
                val doc = documents.getJSONObject(0)
                val lat = doc.optString("y", "0.0").toDoubleOrNull() ?: 0.0
                val lng = doc.optString("x", "0.0").toDoubleOrNull() ?: 0.0
                android.util.Log.d("KakaoGeocoding", "파싱된 위도: $lat, 경도: $lng")
                return lat to lng
            }
            0.0 to 0.0
        } catch (e: Exception) {
            android.util.Log.e("KakaoGeocoding", "Geocoding 실패: ${e.localizedMessage}", e)
            0.0 to 0.0
        }
    }

    // 테스트용 좌표 반환 함수 (더 정확한 주소 매칭)
    private fun getTestLatLngFromAddress(address: String): Pair<Double, Double> {
        // 주소에 따라 더 정확한 테스트 좌표 반환
        return when {
            // 부산 지역
            address.contains("부산") && address.contains("금정구") -> 35.2429 to 129.0929  // 금정구 중심
            address.contains("부산") && address.contains("해운대구") -> 35.1630 to 129.1630  // 해운대구 중심
            address.contains("부산") && address.contains("동래구") -> 35.2055 to 129.0785  // 동래구 중심
            address.contains("부산") && address.contains("부산진구") -> 35.1627 to 129.0532  // 부산진구 중심
            address.contains("부산") -> 35.1796 to 129.0756  // 부산 중심
            
            // 서울 지역
            address.contains("서울") && address.contains("강남구") -> 37.4980 to 127.0276  // 강남구 중심
            address.contains("서울") && address.contains("서초구") -> 37.4837 to 127.0324  // 서초구 중심
            address.contains("서울") && address.contains("마포구") -> 37.5572 to 126.9254  // 마포구 중심
            address.contains("서울") && address.contains("중구") -> 37.5636 to 126.9834  // 중구 중심
            address.contains("서울") && address.contains("용산구") -> 37.5384 to 126.9654  // 용산구 중심
            address.contains("서울") -> 37.5665 to 126.9780  // 서울 중심
            
            // 대구 지역
            address.contains("대구") && address.contains("중구") -> 35.8714 to 128.6014  // 대구 중구
            address.contains("대구") && address.contains("동구") -> 35.8860 to 128.6320  // 대구 동구
            address.contains("대구") -> 35.8714 to 128.6014  // 대구 중심
            
            // 인천 지역
            address.contains("인천") && address.contains("미추홀구") -> 37.4563 to 126.7052  // 인천 미추홀구
            address.contains("인천") && address.contains("연수구") -> 37.4100 to 126.6780  // 인천 연수구
            address.contains("인천") -> 37.4563 to 126.7052  // 인천 중심
            
            // 광주 지역
            address.contains("광주") && address.contains("서구") -> 35.1595 to 126.8526  // 광주 서구
            address.contains("광주") && address.contains("동구") -> 35.1460 to 126.9230  // 광주 동구
            address.contains("광주") -> 35.1595 to 126.8526  // 광주 중심
            
            // 대전 지역
            address.contains("대전") && address.contains("중구") -> 36.3504 to 127.3845  // 대전 중구
            address.contains("대전") && address.contains("유성구") -> 36.3620 to 127.3560  // 대전 유성구
            address.contains("대전") -> 36.3504 to 127.3845  // 대전 중심
            
            // 울산 지역
            address.contains("울산") && address.contains("남구") -> 35.5384 to 129.3114  // 울산 남구
            address.contains("울산") && address.contains("동구") -> 35.5040 to 129.4160  // 울산 동구
            address.contains("울산") -> 35.5384 to 129.3114  // 울산 중심
            
            // 세종 지역
            address.contains("세종") -> 36.4800 to 127.2890  // 세종 중심
            
            // 특정 지역 키워드
            address.contains("강남") -> 37.4980 to 127.0276  // 강남역
            address.contains("홍대") -> 37.5572 to 126.9254  // 홍대입구역
            address.contains("명동") -> 37.5636 to 126.9834  // 명동
            address.contains("동대문") -> 37.5663 to 127.0097  // 동대문
            address.contains("잠실") -> 37.5139 to 127.1006  // 잠실역
            address.contains("강남역") -> 37.4980 to 127.0276  // 강남역
            address.contains("홍대입구") -> 37.5572 to 126.9254  // 홍대입구역
            address.contains("잠실역") -> 37.5139 to 127.1006  // 잠실역
            
            else -> {
                // 주소에서 시/도 정보 추출하여 대략적인 위치 반환
                val city = when {
                    address.contains("부산") -> 35.1796 to 129.0756
                    address.contains("서울") -> 37.5665 to 126.9780
                    address.contains("대구") -> 35.8714 to 128.6014
                    address.contains("인천") -> 37.4563 to 126.7052
                    address.contains("광주") -> 35.1595 to 126.8526
                    address.contains("대전") -> 36.3504 to 127.3845
                    address.contains("울산") -> 35.5384 to 129.3114
                    address.contains("세종") -> 36.4800 to 127.2890
                    else -> {
                        // 한국 내 랜덤 좌표 (더 현실적인 범위)
                        val randomLat = 35.0 + (Math.random() * 5.0)  // 35.0 ~ 40.0
                        val randomLng = 126.0 + (Math.random() * 5.0)  // 126.0 ~ 131.0
                        randomLat to randomLng
                    }
                }
                city
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 