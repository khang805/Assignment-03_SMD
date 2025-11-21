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

class FollowListAdapter(private val followingList: MutableList<User>) :
    RecyclerView.Adapter<FollowListAdapter.FollowingViewHolder>() {

    class FollowingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.followingProfileImage)
        val nameText: TextView = view.findViewById(R.id.followingName)
        val usernameText: TextView = view.findViewById(R.id.followingUsername)
        val unfollowButton: Button = view.findViewById(R.id.unfollowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_following_user, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val user = followingList[position]
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

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MainActivity8::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("username", user.username)
            intent.putExtra("profileImage", user.profileImage)
            context.startActivity(intent)
        }

        holder.unfollowButton.setOnClickListener {
            // TODO: handle unfollow if needed
        }
    }

    override fun getItemCount() = followingList.size
}
