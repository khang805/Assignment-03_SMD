package com.example.assignment1

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class FollowRequestAdapter(
    private val requests: MutableList<FollowRequest>,
    private val currentUsername: String
) : RecyclerView.Adapter<FollowRequestAdapter.RequestViewHolder>() {

    private val db = FirebaseDatabase.getInstance().reference
    private val imageCache = mutableMapOf<String, ByteArray>()

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.requestName)
        val acceptBtn: Button = itemView.findViewById(R.id.acceptBtn)
        val rejectBtn: Button = itemView.findViewById(R.id.rejectBtn)
        val profileImage: ImageView = itemView.findViewById(R.id.requestImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.follow_request_item, parent, false)
        return RequestViewHolder(view)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun onBindViewHolder(holder: RequestViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val req = requests[position]
        val senderUsername = req.senderUsername

        holder.name.text = req.displayName.ifEmpty { senderUsername }

        if (imageCache.containsKey(senderUsername)) {
            val bytes = imageCache[senderUsername]!!
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.profileImage.setImageBitmap(bmp)
        } else {
            db.child("users").orderByChild("username").equalTo(senderUsername)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var found = false
                        for (userSnap in snapshot.children) {
                            val profileImage = userSnap.child("profileImage").getValue(String::class.java)
                            if (!profileImage.isNullOrEmpty()) {
                                try {
                                    val bytes = Base64.decode(profileImage)
                                    imageCache[senderUsername] = bytes
                                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    holder.profileImage.setImageBitmap(bmp)
                                    found = true
                                    break
                                } catch (e: Exception) {
                                    holder.profileImage.setImageResource(R.drawable.ic_profile)
                                }
                            }
                        }
                        if (!found) holder.profileImage.setImageResource(R.drawable.ic_profile)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        holder.profileImage.setImageResource(R.drawable.ic_profile)
                    }
                })
        }

        holder.acceptBtn.setOnClickListener { acceptRequest(req, position) }
        holder.rejectBtn.setOnClickListener { rejectRequest(req, position) }
    }

    override fun getItemCount() = requests.size

    private fun acceptRequest(req: FollowRequest, position: Int) {
        val sender = req.senderUsername
        val receiver = currentUsername

        db.child("followRequests").child(receiver).child(sender).child("status")
            .setValue("accepted")

        db.child("followers").child(receiver).child(sender).setValue(true)
        db.child("following").child(sender).child(receiver).setValue(true)

        requests.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun rejectRequest(req: FollowRequest, position: Int) {
        val sender = req.senderUsername
        val receiver = currentUsername

        db.child("followRequests").child(receiver).child(sender).child("status")
            .setValue("rejected")

        requests.removeAt(position)
        notifyItemRemoved(position)
    }
}
