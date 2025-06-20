package com.example.spendy_2.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spendy_2.databinding.FragmentMapBinding
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null

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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        
        // 지도 기본 설정
        naverMap.uiSettings.isZoomControlEnabled = true
        naverMap.uiSettings.isCompassEnabled = true
        
        // TODO: 지출 데이터에서 위치 정보를 가져와서 마커 표시
        loadExpenseMarkers()
    }

    private fun loadExpenseMarkers() {
        // TODO: 데이터베이스에서 지출 위치 정보를 가져와서 마커 추가
        // 임시로 데이터가 없다는 메시지 표시
        binding.tvNoData.visibility = View.VISIBLE
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