package com.example.assignment1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowRequestsActivity : AppCompatActivity() {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        setContentView(listView)

        val currentUid = auth.currentUser?.uid ?: return
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList())
        val requestUids = ArrayList<String>()
        val requestUsernames = ArrayList<String>()
        listView.adapter = adapter

        db.child("users").child(currentUid).child("username").get()
            .addOnSuccessListener { currentUsernameSnapshot ->
                val currentUsername = currentUsernameSnapshot.getValue(String::class.java) ?: return@addOnSuccessListener

                db.child("followRequests").child(currentUid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                adapter.add("No requests")
                                return
                            }
                            for (child in snapshot.children) {
                                val fromUid = child.key ?: continue
                                val senderUsername = child.child("senderUsername").getValue(String::class.java) ?: continue

                                requestUids.add(fromUid)
                                requestUsernames.add(senderUsername)
                                adapter.add("Request from: $senderUsername")
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })

                listView.setOnItemClickListener { _, _, position, _ ->
                    if (position >= requestUids.size) return@setOnItemClickListener
                    val fromUid = requestUids[position]
                    val fromUsername = requestUsernames[position]

                    AlertDialog.Builder(this)
                        .setTitle("Follow request")
                        .setMessage("Accept follow request from $fromUsername?")
                        .setPositiveButton("Accept") { _, _ ->
                            FollowManager.acceptFollowRequest(
                                currentUid = currentUid,
                                currentUsername = currentUsername,
                                fromUid = fromUid,
                                fromUsername = fromUsername
                            ) { success ->
                                if (success) {
                                    adapter.remove(adapter.getItem(position))
                                    requestUids.removeAt(position)
                                    requestUsernames.removeAt(position)
                                }
                            }
                        }
                        .setNegativeButton("Reject") { _, _ ->
                            FollowManager.rejectFollowRequest(currentUid, fromUid) { success ->
                                if (success) {
                                    adapter.remove(adapter.getItem(position))
                                    requestUids.removeAt(position)
                                    requestUsernames.removeAt(position)
                                }
                            }
                        }
                        .setNeutralButton("Cancel", null)
                        .show()
                }
            }
    }
}
