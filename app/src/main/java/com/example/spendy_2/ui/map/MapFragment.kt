package com.example.spendy_2.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spendy_2.databinding.FragmentMapBinding
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.example.spendy_2.ui.home.HomeFragment.ReceiptInfo
import com.naver.maps.map.overlay.Overlay

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private val markerList = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 네이버 지도 인증 실패 리스너 추가
        NaverMapSdk.getInstance(requireContext()).onAuthFailedListener = 
            NaverMapSdk.OnAuthFailedListener { exception ->
                Log.e("MapFragment", "네이버 지도 인증 실패: ${exception.message}")
                Log.e("MapFragment", "에러 코드: ${exception.errorCode}")
                Log.e("MapFragment", "패키지명: ${requireContext().packageName}")
                Log.e("MapFragment", "예외 타입: ${exception.javaClass.simpleName}")
                Toast.makeText(requireContext(), 
                    "네이버 지도 인증 실패: ${exception.message}\n패키지명: ${requireContext().packageName}", 
                    Toast.LENGTH_LONG).show()
            }
        
        setupMap(savedInstanceState)
        setupClickListeners()
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun setupClickListeners() {
        binding.fabMyLocation.setOnClickListener {
            // TODO: 현재 위치로 이동
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        
        // 테스트 마커 추가 (서울 중심)
        addTestMarkers()
        
        // Firestore에서 영수증 마커 로드
        loadExpenseMarkers()
    }

    private fun addTestMarkers() {
        // 서울 중심 좌표
        val seoulCenter = LatLng(37.5665, 126.9780)
        
        // 테스트 마커들 추가
        val testLocations = listOf(
            Triple("강남역", LatLng(37.4980, 127.0276), "강남역 지하상가"),
            Triple("홍대입구역", LatLng(37.5572, 126.9254), "홍대거리"),
            Triple("명동", LatLng(37.5636, 126.9834), "명동거리"),
            Triple("동대문", LatLng(37.5663, 127.0097), "동대문 패션타운"),
            Triple("잠실역", LatLng(37.5139, 127.1006), "잠실 롯데월드")
        )
        
        for ((name, latLng, description) in testLocations) {
            val marker = Marker()
            marker.position = latLng
            marker.map = naverMap
            marker.captionText = name
            marker.subCaptionText = description
            
            // 마커 클릭 이벤트 추가
            marker.onClickListener = Overlay.OnClickListener {
                Toast.makeText(requireContext(), "$name: $description", Toast.LENGTH_SHORT).show()
                true // 이벤트 소비
            }
            
            markerList.add(marker)
        }
        
        // 서울 중심으로 카메라 이동
        naverMap.moveCamera(CameraUpdate.scrollTo(seoulCenter))
        naverMap.moveCamera(CameraUpdate.zoomTo(10.0))
        
        Log.d("MapFragment", "테스트 마커 ${testLocations.size}개 추가됨")
    }

    private fun loadExpenseMarkers() {
        // 기존 마커 제거
        markerList.forEach { it.map = null }
        markerList.clear()
        // Firestore에서 ReceiptInfo 불러와 마커 추가
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("receipts").get()
            .addOnSuccessListener { result ->
                var firstMarkerLatLng: LatLng? = null
                for (doc in result.documents) {
                    val info = doc.toObject(ReceiptInfo::class.java)
                    if (info != null && info.lat != 0.0 && info.lng != 0.0) {
                        val marker = Marker()
                        val latLng = LatLng(info.lat, info.lng)
                        marker.position = latLng
                        marker.map = naverMap
                        marker.captionText = info.store
                        marker.subCaptionText = "총액: ${info.totalAmount}원"
                        
                        // 영수증 마커 클릭 이벤트
                        marker.onClickListener = Overlay.OnClickListener {
                            val message = "${info.store}\n총액: ${info.totalAmount}원\n날짜: ${info.date}"
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                            true
                        }
                        
                        markerList.add(marker)
                        if (firstMarkerLatLng == null) firstMarkerLatLng = latLng
                    }
                }
                // 첫 마커 위치로 카메라 이동
                firstMarkerLatLng?.let {
                    naverMap.moveCamera(CameraUpdate.scrollTo(it))
                }
            }
            .addOnFailureListener {
                // 실패 시 메시지 표시
                binding.tvNoData.visibility = View.VISIBLE
            }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
} 