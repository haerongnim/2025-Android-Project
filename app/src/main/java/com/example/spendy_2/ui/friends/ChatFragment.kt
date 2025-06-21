package com.example.spendy_2.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.databinding.FragmentChatBinding
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendContact: FriendsFragment.Contact
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val ARG_FRIEND_CONTACT = "friend_contact"

        fun newInstance(contact: FriendsFragment.Contact): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_CONTACT + "_name", contact.name)
            args.putString(ARG_FRIEND_CONTACT + "_phone", contact.phone)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 전달받은 친구 정보 설정
        arguments?.let { args ->
            val name = args.getString(ARG_FRIEND_CONTACT + "_name", "친구")
            val phone = args.getString(ARG_FRIEND_CONTACT + "_phone", "")
            friendContact = FriendsFragment.Contact(name, phone)
        } ?: run {
            friendContact = FriendsFragment.Contact("친구", "")
        }

        setupUI()
        setupChat()
        loadMockMessages()
    }

    private fun setupUI() {
        // 친구 정보 설정
        binding.tvFriendName.text = friendContact.name
        binding.tvFriendStatus.text = "온라인"

        // 툴바 설정
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // 채팅 리사이클러뷰 설정
        binding.rvChatMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        chatAdapter = ChatAdapter(messages)
        binding.rvChatMessages.adapter = chatAdapter

        // 메시지 전송 버튼 설정
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // 엔터키로 메시지 전송
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun setupChat() {
        // TODO: Firebase Realtime Database나 Firestore를 사용한 실시간 채팅 구현
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val message = ChatMessage(
                text = messageText,
                isFromMe = true,
                timestamp = System.currentTimeMillis()
            )
            messages.add(message)
            chatAdapter.notifyItemInserted(messages.size - 1)
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
            binding.etMessage.text?.clear()

            // TODO: Firebase에 메시지 저장
            // TODO: 친구에게 푸시 알림 전송

            // 임시로 자동 응답 (실제로는 친구의 실제 응답)
            simulateFriendResponse()
        }
    }

    private fun simulateFriendResponse() {
        // 1-3초 후 친구 응답 시뮬레이션
        binding.btnSend.postDelayed({
            val responses = listOf(
                "오! 그렇구나 😊",
                "나도 비슷해!",
                "좋은 정보 감사해~",
                "오늘 날씨 진짜 좋다",
                "저녁 뭐 먹을까?",
                "영화 보러 갈까?",
                "카페 가자!",
                "오늘 진짜 힘들었어 😅"
            )
            val randomResponse = responses.random()
            
            val message = ChatMessage(
                text = randomResponse,
                isFromMe = false,
                timestamp = System.currentTimeMillis()
            )
            messages.add(message)
            chatAdapter.notifyItemInserted(messages.size - 1)
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
        }, (1000..3000).random().toLong())
    }

    private fun loadMockMessages() {
        // 임시 대화 데이터
        val mockMessages = listOf(
            ChatMessage("안녕! 오늘 뭐해?", false, System.currentTimeMillis() - 3600000),
            ChatMessage("안녕! 그냥 집에 있어", true, System.currentTimeMillis() - 3500000),
            ChatMessage("오늘 날씨 진짜 좋다", false, System.currentTimeMillis() - 3400000),
            ChatMessage("맞아! 나가서 놀까?", true, System.currentTimeMillis() - 3300000),
            ChatMessage("좋아! 어디 갈까?", false, System.currentTimeMillis() - 3200000),
            ChatMessage("카페 어때?", true, System.currentTimeMillis() - 3100000),
            ChatMessage("좋은 아이디어야! 😊", false, System.currentTimeMillis() - 3000000)
        )
        
        messages.addAll(mockMessages)
        chatAdapter.notifyDataSetChanged()
        binding.rvChatMessages.scrollToPosition(messages.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 채팅 메시지 데이터 클래스
    data class ChatMessage(
        val text: String,
        val isFromMe: Boolean,
        val timestamp: Long
    )

    // 채팅 어댑터
    inner class ChatAdapter(
        private val messages: List<ChatMessage>
    ) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                com.example.spendy_2.R.layout.item_chat_message, parent, false
            )
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int = messages.size

        inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val layoutMyMessage = itemView.findViewById<View>(com.example.spendy_2.R.id.layout_my_message)
            private val layoutFriendMessage = itemView.findViewById<View>(com.example.spendy_2.R.id.layout_friend_message)
            private val tvMyMessage = itemView.findViewById<TextView>(com.example.spendy_2.R.id.tv_my_message)
            private val tvMyTime = itemView.findViewById<TextView>(com.example.spendy_2.R.id.tv_my_time)
            private val tvFriendMessage = itemView.findViewById<TextView>(com.example.spendy_2.R.id.tv_friend_message)
            private val tvFriendTime = itemView.findViewById<TextView>(com.example.spendy_2.R.id.tv_friend_time)

            fun bind(message: ChatMessage) {
                val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)
                val timeString = timeFormat.format(Date(message.timestamp))

                if (message.isFromMe) {
                    layoutMyMessage.visibility = View.VISIBLE
                    layoutFriendMessage.visibility = View.GONE
                    tvMyMessage.text = message.text
                    tvMyTime.text = timeString
                } else {
                    layoutMyMessage.visibility = View.GONE
                    layoutFriendMessage.visibility = View.VISIBLE
                    tvFriendMessage.text = message.text
                    tvFriendTime.text = timeString
                }
            }
        }
    }
} 