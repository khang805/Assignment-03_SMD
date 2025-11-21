package com.example.assignment1

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity12 : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: FollowRequestAdapter
    private lateinit var recycler: RecyclerView
    private val requestList = mutableListOf<FollowRequest>()
    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main12)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        recycler = findViewById(R.id.followRequestsRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        setupNavigation()

        val currentUid = auth.currentUser?.uid ?: return

        database.child("users").child(currentUid).child("username").get()
            .addOnSuccessListener { snapshot ->
                currentUsername = snapshot.getValue(String::class.java)
                if (currentUsername != null) {
                    adapter = FollowRequestAdapter(requestList, currentUsername!!)
                    recycler.adapter = adapter
                    loadFollowRequests()
                }
            }

        findViewById<TextView>(R.id.following).setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
    }

    private fun loadFollowRequests() {
        val username = currentUsername ?: return

        database.child("followRequests").child(username)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestList.clear()
                    for (child in snapshot.children) {
                        val senderUsername = child.key ?: continue
                        val status = child.child("status").getValue(String::class.java) ?: continue
                        if (status != "pending") continue

                        database.child("users").orderByChild("username").equalTo(senderUsername)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    for (u in userSnap.children) {
                                        val displayName = u.child("full name").getValue(String::class.java) ?: senderUsername
                                        val profileImageEncoded = u.child("profileImage").getValue(String::class.java)
                                        var profileImage: String? = null
                                        if (!profileImageEncoded.isNullOrEmpty()) {
                                            try {
                                                val bytes = Base64.decode(profileImageEncoded, Base64.DEFAULT)
                                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                profileImage = profileImageEncoded
                                            } catch (_: Exception) {}
                                        }
                                        val senderUid = u.key ?: ""
                                        requestList.add(
                                            FollowRequest(
                                                senderUsername = senderUsername,
                                                displayName = displayName,
                                                profileImage = profileImage,
                                                status = status,
                                                senderUid = senderUid
                                            )
                                        )
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<ImageView>(R.id.add).setOnClickListener {
            startActivity(Intent(this, MainActivity19::class.java))
        }
        findViewById<ImageView>(R.id.heart).setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
        findViewById<ImageView>(R.id.profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
