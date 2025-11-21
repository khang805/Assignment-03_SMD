package com.example.assignment1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowingListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowListAdapter
    private val followingList = mutableListOf<User>()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following_list)

        recyclerView = findViewById(R.id.followingRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FollowListAdapter(followingList)
        recyclerView.adapter = adapter

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

                            db.child("users").get().addOnSuccessListener { usersSnap ->
                                for (userSnap in usersSnap.children) {
                                    val uname = userSnap.child("username").getValue(String::class.java)
                                    if (uname != null && uname in followedUsernames) {
                                        val fullName = userSnap.child("full name").getValue(String::class.java) ?: uname
                                        val image = userSnap.child("profileImage").getValue(String::class.java)
                                        val uid = userSnap.key ?: ""
                                        followingList.add(User(uname, fullName, image, uid))
                                    }
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
    }
}
