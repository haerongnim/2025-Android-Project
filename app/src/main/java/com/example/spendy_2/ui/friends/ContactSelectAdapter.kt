package com.example.spendy_2.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendy_2.R

class ContactSelectAdapter(
    private val items: List<FriendsFragment.Contact>,
    private val addedFriends: List<FriendsFragment.Contact>
) : RecyclerView.Adapter<ContactSelectAdapter.ContactViewHolder>() {
    private val selectedSet = mutableSetOf<Int>()

    fun getSelectedContacts(): List<FriendsFragment.Contact> = selectedSet.map { items[it] }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = items[position]
        val isAdded = addedFriends.any { it.phone == contact.phone }
        holder.bind(contact, position, selectedSet.contains(position), isAdded)
    }

    override fun getItemCount(): Int = items.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cb_select)
        private val tvName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_contact_phone)
        fun bind(item: FriendsFragment.Contact, pos: Int, checked: Boolean, isAdded: Boolean) {
            tvName.text = item.name
            tvPhone.text = item.phone
            if (isAdded) {
                cbSelect.isChecked = true
                cbSelect.isEnabled = false
                cbSelect.text = "추가됨"
            } else {
                cbSelect.isChecked = checked
                cbSelect.isEnabled = true
                cbSelect.text = ""
                cbSelect.setOnCheckedChangeListener(null)
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedSet.add(pos) else selectedSet.remove(pos)
                }
            }
        }
    }
} 