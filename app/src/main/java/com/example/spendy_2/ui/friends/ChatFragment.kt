package com.example.spendy_2.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendy_2.databinding.FragmentChatBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val name = it.getString("friend_name", "")
            val phone = it.getString("friend_phone", "")
            viewModel.setFriendInfo(name, phone)
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
        setupUI()
        setupRecyclerView()
        setupObservers()
        viewModel.loadMockMessages()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.tvFriendName.text = viewModel.friendName.value
        
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        binding.rvChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        chatAdapter = ChatAdapter(viewModel.getMessages())
        binding.rvChat.adapter = chatAdapter
    }
    
    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.scrollToPosition(messages.size - 1)
            }
        }
        
        viewModel.friendName.observe(viewLifecycleOwner) { name ->
            binding.tvFriendName.text = name
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return

        viewModel.sendMessage(messageText)
        binding.etMessage.text.clear()
        
        // 자동 응답
        binding.rvChat.postDelayed({
            val autoResponses = listOf("그렇구나!", "응응 알겠어~", "오 좋네!", "그래서 어떻게 됐어?")
            val randomResponse = autoResponses.random()
            viewModel.addFriendResponse(randomResponse)
        }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ChatAdapter(private var messages: List<ChatViewModel.ChatMessage>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_FRIEND_MESSAGE = 2
    }
    
    fun updateMessages(newMessages: List<ChatViewModel.ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromMe) VIEW_TYPE_MY_MESSAGE else VIEW_TYPE_FRIEND_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> com.example.spendy_2.R.layout.item_chat_my_message
            VIEW_TYPE_FRIEND_MESSAGE -> com.example.spendy_2.R.layout.item_chat_friend_message
            else -> com.example.spendy_2.R.layout.item_chat_friend_message
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val tvMessage: android.widget.TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_message)
        private val tvTime: android.widget.TextView = itemView.findViewById(com.example.spendy_2.R.id.tv_time)

        fun bind(message: ChatViewModel.ChatMessage) {
            tvMessage.text = message.message
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = dateFormat.format(Date(message.timestamp))
        }
    }
} 