package com.example.spendy_2.ui.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendy_2.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchFunction()
        setupClickListeners()
        loadFriendsList()
    }

    private fun setupRecyclerView() {
        binding.rvFriends.layoutManager = LinearLayoutManager(context)
        // TODO: 친구 목록 어댑터 설정
    }

    private fun setupSearchFunction() {
        binding.etSearchFriends.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO: 실시간 검색 구현
                searchFriends(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        binding.fabAddFriend.setOnClickListener {
            showAddFriendDialog()
        }
    }

    private fun searchFriends(query: String) {
        // TODO: 친구 목록에서 검색어에 맞는 친구들 필터링
        if (query.isEmpty()) {
            loadFriendsList()
        } else {
            // 검색 결과 표시
        }
    }

    private fun showAddFriendDialog() {
        // TODO: 친구 추가 다이얼로그 구현
        Toast.makeText(context, "친구 추가 기능은 추후 구현됩니다", Toast.LENGTH_SHORT).show()
    }

    private fun loadFriendsList() {
        // TODO: Firebase에서 친구 목록 로드
        // 임시로 빈 목록 표시
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 