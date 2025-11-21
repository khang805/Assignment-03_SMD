package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserSearchAdapter(private val context: Context, private var userList: List<SearchUser>) :
    RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUsername.text = user.username
        holder.tvDisplayName.text = user.name

        if (user.profileImageUrl.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(user.profileImageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(context).load(bitmap).placeholder(R.drawable.ic_profile_blue).into(holder.ivProfile)
            } catch (e: Exception) {
                holder.ivProfile.setImageResource(R.drawable.ic_profile_blue)
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile_blue)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OtherProfileActivity::class.java)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
    
    fun updateUsers(users: List<SearchUser>) {
        this.userList = users
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
    }
}
