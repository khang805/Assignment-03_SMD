package com.example.assignment1

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MessageListAdapter(private val userList: MutableList<User>) :
    RecyclerView.Adapter<MessageListAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.followingProfileImage)
        val nameText: TextView = view.findViewById(R.id.followingName)
        val usernameText: TextView = view.findViewById(R.id.followingUsername)
        val messageButton: Button = view.findViewById(R.id.messageButton)
        val statusText: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.fullName
        holder.usernameText.text = "@${user.username}"

        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile)
        }

        if (user.status == "online") {
            holder.statusText.text = "Online"
            holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.statusText.text = "Offline"
            holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        val context = holder.itemView.context
        val openChat = {
            val intent = Intent(context, MainActivity8::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("username", user.username)
            intent.putExtra("profileImage", user.profileImage)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener { openChat() }
        holder.messageButton.setOnClickListener { openChat() }
    }

    override fun getItemCount() = userList.size
}
