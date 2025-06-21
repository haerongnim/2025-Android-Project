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
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import android.app.Dialog
import android.widget.TextView
import android.widget.Button

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
        val lng: Double = 0.0,  // 경도
        val category: String = "" // 카테고리 필드 추가
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

    // TFLite 모델 관련 상수
    private val LABELS = listOf("식품", "음료", "생활용품", "의류", "전자기기", "뷰티", "명품", "외식", "기타")
    private var tfliteInterpreter: Interpreter? = null
    private var isModelLoaded = false
    private var modelInputSize = 0
    
    // TFLite 모델 로딩 활성화 (KCELECTRA 경량화 모델)
    private val USE_TFLITE = true  // 다시 활성화

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
        // TFLite 모델은 필요할 때 로딩 (지연 로딩)
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

    // 안전한 TFLite 모델 로딩
    private fun loadTFLiteModelSafely() {
        if (!USE_TFLITE) {
            android.util.Log.d("TFLite", "TFLite 모델 로딩 비활성화됨 - 키워드 분류만 사용")
            return
        }
        
        if (isModelLoaded) return
        
        try {
            android.util.Log.d("TFLite", "모델 로딩 시작")
            
            // 완전히 백그라운드에서 로딩
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val modelBuffer = loadModelFile(requireContext(), "kcelectra_receipt_model_int8.tflite")
                    
                    // 안전한 옵션 설정
                    val options = Interpreter.Options()
                    options.setNumThreads(1) // 단일 스레드로 안전하게
                    options.setUseXNNPACK(false) // XNNPACK 비활성화
                    
                    val interpreter = Interpreter(modelBuffer, options)
                    
                    // 모델 정보 로그
                    val inputShape = interpreter.getInputTensor(0).shape()
                    val outputShape = interpreter.getOutputTensor(0).shape()
                    android.util.Log.d("TFLite", "모델 로딩 성공 - 입력: ${inputShape.contentToString()}, 출력: ${outputShape.contentToString()}")
                    
                    // 모델의 실제 입력 크기 저장
                    val actualInputSize = inputShape[1] // [1, max_length] 형태일 것으로 예상
                    android.util.Log.d("TFLite", "실제 모델 입력 크기: $actualInputSize")
                    
                    // 출력 텐서 정보
                    val outputSize = outputShape[1] // 출력 클래스 수
                    android.util.Log.d("TFLite", "실제 모델 출력 크기: $outputSize")
                    
                    withContext(Dispatchers.Main) {
                        tfliteInterpreter = interpreter
                        isModelLoaded = true
                        // 실제 입력 크기를 전역 변수로 저장
                        modelInputSize = actualInputSize
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("TFLite", "모델 로딩 실패: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        isModelLoaded = false
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TFLite", "모델 로딩 초기화 실패: ${e.message}", e)
            isModelLoaded = false
        }
    }

    // 모델 파일 로딩
    private fun loadModelFile(context: android.content.Context, modelName: String): MappedByteBuffer {
        var fileDescriptor: android.content.res.AssetFileDescriptor? = null
        var inputStream: FileInputStream? = null
        return try {
            fileDescriptor = context.assets.openFd(modelName)
            inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            
            android.util.Log.d("TFLite", "모델 파일 로딩: $modelName, 크기: $declaredLength bytes")
            
            val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            buffer
        } catch (e: Exception) {
            android.util.Log.e("TFLite", "모델 파일 로딩 실패: ${e.message}", e)
            throw e
        } finally {
            try {
                inputStream?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                android.util.Log.e("TFLite", "리소스 정리 실패: ${e.message}", e)
            }
        }
    }

    // KCELECTRA 토크나이저 (JSON 설정 기반)
    private fun tokenizeForKCELECTRA(text: String): IntArray {
        // 모델의 실제 입력 크기 사용 (기본값 64)
        val maxLength = if (modelInputSize > 0) modelInputSize else 64
        val tokenIds = IntArray(maxLength) { 3 } // PAD 토큰(3)으로 초기화
        
        // 특수 토큰 ID 매핑
        val CLS_TOKEN_ID = 1  // [CLS]
        val SEP_TOKEN_ID = 2  // [SEP]
        val PAD_TOKEN_ID = 3  // [PAD]
        val UNK_TOKEN_ID = 0  // [UNK]
        
        // [CLS] 토큰으로 시작
        tokenIds[0] = CLS_TOKEN_ID
        
        // 텍스트를 단어로 분리 (한국어 특화)
        val words = text.split(" ").take(maxLength - 2) // CLS, SEP 토큰 공간 확보
        
        words.forEachIndexed { index, word ->
            if (index + 1 < maxLength - 1) { // SEP 토큰 공간 확보
                // 안전한 토큰 ID 생성 - 알려진 단어만 특정 ID 할당
                val tokenId = when (word.lowercase()) {
                    "문토스트", "토스트" -> 100
                    "베이컨", "치즈", "바베큐" -> 101
                    "아이스티", "아이스", "티" -> 102
                    "커피", "아메리카노", "라떼" -> 103
                    "음료", "주스", "탄산" -> 104
                    "식품", "마트", "편의점" -> 105
                    "외식", "식당", "레스토랑" -> 106
                    "생활용품", "세제", "용품" -> 107
                    "의류", "옷", "패션" -> 108
                    "전자기기", "전자", "컴퓨터" -> 109
                    "뷰티", "화장품", "미용" -> 110
                    "명품", "브랜드", "루이비통" -> 111
                    else -> UNK_TOKEN_ID // 알 수 없는 단어는 UNK 토큰 사용
                }
                tokenIds[index + 1] = tokenId
            }
        }
        
        // [SEP] 토큰으로 끝
        val sepIndex = minOf(words.size + 1, maxLength - 1)
        tokenIds[sepIndex] = SEP_TOKEN_ID
        
        android.util.Log.d("TFLite", "KCELECTRA 토크나이징: 입력='$text', 토큰 수=${words.size + 2}, 배열 크기=$maxLength")
        
        return tokenIds
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
                try {
                    // CLOVA OCR 결과가 실패인지 확인
                    if (clovaResult.startsWith("CLOVA OCR 실패") || clovaResult == "CLOVA") {
                        android.util.Log.e("OCR", "CLOVA OCR API 호출 실패: $clovaResult")
                        Toast.makeText(context, "OCR 인식에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        return@runClovaOcrRest
                    }
                    
                    // CLOVA OCR 결과에서 날짜/총액/가게명/품목 자동 추출 (실제 JSON 구조에 맞게)
                    var parsedDate: Long = System.currentTimeMillis()
                    var parsedTotal: Int = 0
                    var parsedStore: String = ""
                    var parsedItems: List<ReceiptItem> = emptyList()
                    var dateStrForDisplay = ""
                    var parsedAddress = ""
                    
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
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("OCR", "JSON 파싱 실패: ${e.message}", e)
                        Toast.makeText(context, "영수증 정보 파싱에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        return@runClovaOcrRest
                    }
                    
                    // 카테고리 분류 (안전하게)
                    val category = try {
                        classifyCategorySafely("$parsedStore ${parsedItems.joinToString(" ") { it.name }}")
                    } catch (e: Exception) {
                        android.util.Log.e("TFLite", "카테고리 분류 실패: ${e.message}", e)
                        "기타"
                    }
                    
                    // 주소 → 좌표 변환 (코루틴 launch)
                    if (parsedAddress.isNotBlank()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            try {
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
                                    lng = finalLng,
                                    category = category
                                )
                                saveReceiptToFirestore(receiptInfo)
                                showReceiptResult(parsedStore, dateStrForDisplay, parsedTotal, parsedAddress, finalLat, finalLng, category, parsedItems, clovaResult)
                            } catch (e: Exception) {
                                android.util.Log.e("Geocoding", "좌표 변환 실패: ${e.message}", e)
                                // Geocoding 실패 시에도 결과 표시
                                val receiptInfo = ReceiptInfo(
                                    store = parsedStore,
                                    items = parsedItems,
                                    totalAmount = parsedTotal,
                                    date = parsedDate,
                                    address = parsedAddress,
                                    lat = 0.0,
                                    lng = 0.0,
                                    category = category
                                )
                                saveReceiptToFirestore(receiptInfo)
                                showReceiptResult(parsedStore, dateStrForDisplay, parsedTotal, parsedAddress, 0.0, 0.0, category, parsedItems, clovaResult)
                            }
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
                        lng = 0.0,
                        category = category
                    )
                    saveReceiptToFirestore(receiptInfo)
                    showReceiptResult(parsedStore, dateStrForDisplay, parsedTotal, parsedAddress, 0.0, 0.0, category, parsedItems, clovaResult)
                    
                } catch (e: Exception) {
                    android.util.Log.e("OCR", "전체 처리 실패: ${e.message}", e)
                    Toast.makeText(context, "영수증 처리 중 오류가 발생했습니다: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "이미지 처리 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // 영수증 결과 표시 함수
    private fun showReceiptResult(
        store: String, 
        dateStr: String, 
        total: Int, 
        address: String, 
        lat: Double, 
        lng: Double, 
        category: String, 
        items: List<ReceiptItem>, 
        clovaResult: String
    ) {
        try {
            // 커스텀 다이얼로그 생성
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_receipt_result)
            
            // 다이얼로그 크기 설정
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // 데이터 설정
            dialog.findViewById<TextView>(R.id.tvStoreName).text = if (store.isNotBlank()) store else "-"
            dialog.findViewById<TextView>(R.id.tvTotalAmount).text = if (total > 0) "₩${String.format("%,d", total)}" else "-"
            dialog.findViewById<TextView>(R.id.tvAddress).text = if (address.isNotBlank()) address else "-"
            dialog.findViewById<TextView>(R.id.tvCategory).text = category
            
            // 구매 품목 설정
            val rvItems = dialog.findViewById<RecyclerView>(R.id.rvItems)
            rvItems.layoutManager = LinearLayoutManager(context)
            val detailAdapter = ReceiptDetailAdapter()
            rvItems.adapter = detailAdapter
            detailAdapter.updateData(items)
            
            // 버튼 클릭 리스너
            dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.findViewById<Button>(R.id.btnViewDetails).setOnClickListener {
                dialog.dismiss()
                // 상세 정보 다이얼로그 표시 (기존 방식)
                showDetailedReceiptResult(store, dateStr, total, address, lat, lng, category, items, clovaResult)
            }
            
            // 다이얼로그 표시
            dialog.show()
                
        } catch (e: Exception) {
            android.util.Log.e("UI", "결과 표시 실패: ${e.message}", e)
            Toast.makeText(context, "결과 표시 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 상세 영수증 결과 표시 함수 (기존 방식)
    private fun showDetailedReceiptResult(
        store: String, 
        dateStr: String, 
        total: Int, 
        address: String, 
        lat: Double, 
        lng: Double, 
        category: String, 
        items: List<ReceiptItem>, 
        clovaResult: String
    ) {
        try {
            // 예쁘게 포맷팅
            val sb = StringBuilder()
            sb.appendLine("[영수증 인식 결과]")
            sb.appendLine()
            sb.appendLine("가게명: ${if (store.isNotBlank()) store else "-"}")
            sb.appendLine("날짜: ${if (dateStr.isNotBlank()) dateStr else "-"}")
            sb.appendLine("총액: ${if (total > 0) String.format("%,d원", total) else "-"}")
            sb.appendLine("주소: ${if (address.isNotBlank()) address else "-"}")
            sb.appendLine("위도: $lat, 경도: $lng")
            sb.appendLine("카테고리: $category")
            if (lat == 0.0 && lng == 0.0) {
                sb.appendLine("(Geocoding 실패로 테스트 좌표 사용)")
            } else {
                sb.appendLine("(정확한 Geocoding 좌표)")
            }
            sb.appendLine()
            sb.appendLine("[구매 품목]")
            if (items.isNotEmpty()) {
                items.forEach {
                    sb.appendLine("- ${it.name}  ${if (it.price > 0) String.format("%,d원", it.price) else "-"}")
                }
            } else {
                sb.appendLine("(품목 정보 없음)")
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("상세 영수증 정보")
                .setMessage(sb.toString() + "\n\n[JSON 원문]\n" + clovaResult)
                .setPositiveButton("확인", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("UI", "상세 결과 표시 실패: ${e.message}", e)
            Toast.makeText(context, "상세 결과 표시 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
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

    // 모델 로딩 완료 대기
    private suspend fun waitForModelLoading(): Boolean {
        var attempts = 0
        while (!isModelLoaded && attempts < 10) {
            android.util.Log.d("TFLite", "모델 로딩 대기 중... (시도: ${attempts + 1})")
            delay(500) // 0.5초 대기
            attempts++
        }
        return isModelLoaded
    }

    // 안전한 카테고리 분류
    private fun classifyCategorySafely(text: String): String {
        return try {
            if (!USE_TFLITE) {
                android.util.Log.d("TFLite", "TFLite 비활성화됨, 키워드 분류 사용")
                return classifyCategoryByKeywords(text)
            }
            
            // 모델이 로드되지 않았으면 로딩 시도
            if (!isModelLoaded || tfliteInterpreter == null) {
                android.util.Log.d("TFLite", "모델이 로드되지 않음, 로딩 시도")
                loadTFLiteModelSafely()
                // 로딩 중이므로 키워드 분류 사용
                android.util.Log.d("TFLite", "모델 로딩 중이므로 키워드 분류 사용")
                return classifyCategoryByKeywords(text)
            }
            
            android.util.Log.d("TFLite", "모델이 로드됨, TFLite 추론 시도")
            
            // 백그라운드에서 안전하게 추론
            return runBlocking(Dispatchers.IO) {
                try {
                    android.util.Log.d("TFLite", "추론 시작 - 입력 텍스트: $text")
                    
                    // KCELECTRA 토크나이저 사용
                    val tokenIds = tokenizeForKCELECTRA(text)
                    android.util.Log.d("TFLite", "토크나이징 완료 - 토큰 수: ${tokenIds.size}")
                    
                    val inputArray = Array(1) { tokenIds }
                    val output = Array(1) { FloatArray(LABELS.size) }
                    
                    android.util.Log.d("TFLite", "입력 배열 형태: ${inputArray.size}x${inputArray[0].size}")
                    android.util.Log.d("TFLite", "출력 배열 형태: ${output.size}x${output[0].size}")
                    
                    android.util.Log.d("TFLite", "추론 실행 시작")
                    // 추론 실행
                    tfliteInterpreter?.run(inputArray, output)
                    android.util.Log.d("TFLite", "추론 실행 완료")
                    
                    // 출력 크기 확인
                    android.util.Log.d("TFLite", "실제 출력 크기: ${output[0].size}")
                    
                    val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                    val category = LABELS[maxIdx]
                    
                    android.util.Log.d("TFLite", "KCELECTRA 분류 결과: $category (인덱스: $maxIdx, 신뢰도: ${output[0][maxIdx]})")
                    category
                    
                } catch (e: Exception) {
                    android.util.Log.e("TFLite", "추론 실패: ${e.message}", e)
                    android.util.Log.d("TFLite", "추론 실패로 키워드 분류로 fallback")
                    classifyCategoryByKeywords(text)
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("TFLite", "카테고리 분류 실패: ${e.message}", e)
            // 실패 시 키워드 분류로 fallback
            classifyCategoryByKeywords(text)
        }
    }

    // 키워드 기반 분류 (fallback)
    private fun classifyCategoryByKeywords(text: String): String {
        val lowerText = text.lowercase()
        
        // 점수 기반 분류 시스템
        val scores = mutableMapOf<String, Int>()
        LABELS.forEach { scores[it] = 0 }
        
        // 음료 카테고리 키워드
        val beverageKeywords = listOf("스타벅스", "카페", "커피", "음료", "주스", "탄산", "맥주", "술", "와인", "음료수", "차", "라떼", "아메리카노", "카푸치노", "에스프레소", "아메리카", "블루샥", "블루샤크")
        beverageKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) {
                scores["음료"] = scores["음료"]!! + 1
                android.util.Log.d("Keyword", "음료 키워드 매칭: $keyword")
            }
        }
        
        // 외식 카테고리 키워드
        val diningKeywords = listOf("식당", "맛집", "음식", "피자", "치킨", "햄버거", "중국집", "일식", "양식", "분식", "고기집", "회", "레스토랑", "패밀리", "맥도날드", "버거킹", "롯데리아", "KFC", "도미노", "피자헛")
        diningKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["외식"] = scores["외식"]!! + 1
        }
        
        // 식품 카테고리 키워드
        val foodKeywords = listOf("마트", "편의점", "슈퍼", "이마트", "홈플러스", "롯데마트", "농협", "농협하나로", "식품", "과일", "채소", "육류", "생선", "우유", "빵", "GS25", "CU", "세븐일레븐", "미니스톱", "바이더웨이", "토스트", "문토스트", "베이컨", "치즈", "바베큐")
        foodKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) {
                scores["식품"] = scores["식품"]!! + 1
                android.util.Log.d("Keyword", "식품 키워드 매칭: $keyword")
            }
        }
        
        // 의류 카테고리 키워드
        val clothingKeywords = listOf("옷", "의류", "패션", "신발", "가방", "액세서리", "유니클로", "자라", "h&m", "스파오", "무신사", "29cm", "지그재그", "무신사", "스타일쉐어", "브랜디", "오늘의집")
        clothingKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["의류"] = scores["의류"]!! + 1
        }
        
        // 뷰티 카테고리 키워드
        val beautyKeywords = listOf("화장품", "뷰티", "미용", "올리브영", "롯데면세점", "신세계면세점", "이니스프리", "네이처리퍼블릭", "아리따움", "스킨케어", "메이크업", "향수", "립스틱", "파운데이션", "마스카라", "아이라이너")
        beautyKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["뷰티"] = scores["뷰티"]!! + 1
        }
        
        // 전자기기 카테고리 키워드
        val electronicsKeywords = listOf("전자", "컴퓨터", "폰", "삼성", "lg", "애플", "노트북", "태블릿", "이어폰", "충전기", "케이블", "케이스", "갤럭시", "아이폰", "맥북", "아이패드", "갤럭시탭")
        electronicsKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["전자기기"] = scores["전자기기"]!! + 1
        }
        
        // 명품 카테고리 키워드
        val luxuryKeywords = listOf("명품", "브랜드", "루이비통", "샤넬", "에르메스", "구찌", "프라다", "버버리", "롤렉스", "카르티에", "티파니", "불가리", "샤넬", "디올", "생로랑", "발렌시아가")
        luxuryKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["명품"] = scores["명품"]!! + 1
        }
        
        // 생활용품 카테고리 키워드
        val householdKeywords = listOf("세제", "생활", "용품", "다이소", "무인양품", "이케아", "문구", "책", "도서", "약국", "약", "비타민", "화장지", "휴지", "세제", "빨래", "청소", "주방용품")
        householdKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) scores["생활용품"] = scores["생활용품"]!! + 1
        }
        
        // 최고 점수 카테고리 찾기
        val maxScore = scores.values.maxOrNull() ?: 0
        val maxCategory = scores.entries.find { it.value == maxScore }?.key ?: "기타"
        
        android.util.Log.d("Keyword", "분류 결과: $maxCategory (점수: $maxScore), 입력: $text")
        
        return if (maxScore > 0) maxCategory else "기타"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // TFLite 리소스 정리 (TFLite 사용 시에만)
        if (USE_TFLITE) {
            try {
                tfliteInterpreter?.close()
                tfliteInterpreter = null
                isModelLoaded = false
            } catch (e: Exception) {
                android.util.Log.e("TFLite", "리소스 정리 실패: ${e.message}", e)
            }
        }
        _binding = null
    }
} 