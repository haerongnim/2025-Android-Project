package com.example.spendy_2.ui.friends

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendy_2.databinding.FragmentFriendsBinding
import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import com.example.spendy_2.R

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    // 연락처 데이터 클래스
    data class Contact(val name: String, val phone: String)
    private val contactList = mutableListOf<Contact>()
    private val addedFriendsList = mutableListOf<Contact>()

    private lateinit var friendAdapter: FriendAdapter

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
        checkContactsPermissionAndLoad()
    }

    private fun setupRecyclerView() {
        binding.rvFriends.layoutManager = LinearLayoutManager(context)
        friendAdapter = FriendAdapter(addedFriendsList) { contact ->
            // 친구 목록에서의 추가 버튼은 동작하지 않도록 빈 람다 전달
        }
        binding.rvFriends.adapter = friendAdapter
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_contacts, null)
        val rvSelectContacts = dialogView.findViewById<RecyclerView>(R.id.rv_select_contacts)
        val btnAddSelected = dialogView.findViewById<Button>(R.id.btn_add_selected_friends)
        val adapter = ContactSelectAdapter(contactList, addedFriendsList)
        rvSelectContacts.layoutManager = LinearLayoutManager(context)
        rvSelectContacts.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnAddSelected.setOnClickListener {
            val selected = adapter.getSelectedContacts()
            if (selected.isEmpty()) {
                Toast.makeText(context, "추가할 친구를 선택하세요", Toast.LENGTH_SHORT).show()
            } else {
                selected.forEach { addFriendToFirebase(it) }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun loadFriendsList() {
        // TODO: Firebase에서 친구 목록 로드
        // 임시로 빈 목록 표시
    }

    private fun addFriendToFirebase(contact: Contact) {
        if (addedFriendsList.any { it.phone == contact.phone }) {
            Toast.makeText(context, "${contact.name}님은 이미 추가된 친구입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        addedFriendsList.add(contact)
        friendAdapter.updateData(addedFriendsList)
        Toast.makeText(context, "${contact.name}님을 친구로 추가했습니다!", Toast.LENGTH_SHORT).show()
    }

    // 연락처 권한 요청 및 불러오기
    private fun checkContactsPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1002)
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        contactList.clear()
        val cursor = requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx)
                val number = it.getString(numberIdx)
                contactList.add(Contact(name, number))
            }
        }
        // 친구 목록만 표시하므로 어댑터 갱신은 필요 없음
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else if (requestCode == 1002) {
            Toast.makeText(context, "연락처 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 