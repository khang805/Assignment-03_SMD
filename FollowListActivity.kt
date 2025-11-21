package com.example.assignment1

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowListAdapter
    private lateinit var titleText: TextView
    private val followingList = mutableListOf<User>()
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        recyclerView = findViewById(R.id.followRecyclerView)
        titleText = findViewById(R.id.titleText)
        adapter = FollowListAdapter(followingList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        titleText.text = "Following"

        loadFollowing()
    }

    private fun loadFollowing() {
        val currentUid = auth.currentUser?.uid ?: return

        db.child("users").child(currentUid).child("username").get()
            .addOnSuccessListener { usernameSnap ->
                val username = usernameSnap.getValue(String::class.java) ?: return@addOnSuccessListener

                db.child("following").child(username)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            followingList.clear()

                            val followedUsernames = snapshot.children.mapNotNull { it.key }

                            if (followedUsernames.isEmpty()) {
                                adapter.notifyDataSetChanged()
                                return
                            }

                            var loadedCount = 0

                            for (followedUsername in followedUsernames) {
                                db.child("users").orderByChild("username").equalTo(followedUsername)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnap: DataSnapshot) {
                                            for (u in userSnap.children) {
                                                val fullName = u.child("full name").getValue(String::class.java)
                                                    ?: followedUsername
                                                val image = u.child("profileImage").getValue(String::class.java)
                                                val uid = u.key ?: ""
                                                followingList.add(User(followedUsername, fullName, image, uid))
                                            }
                                            loadedCount++
                                            if (loadedCount == followedUsernames.size) {
                                                adapter.notifyDataSetChanged()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
    }
}
