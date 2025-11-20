package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonParser

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: ProfilePostAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val dp = findViewById<ImageView>(R.id.dp)
        val nameTextView = findViewById<TextView>(R.id.name)
        val usernameTextView = findViewById<TextView>(R.id.username)
        val postsCountTextView = findViewById<TextView>(R.id.post_count)
        postsRecyclerView = findViewById<RecyclerView>(R.id.postsGridRecyclerView)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)

        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postAdapter = ProfilePostAdapter(this, postList)
        postsRecyclerView.adapter = postAdapter

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserProfile(userId, dp, nameTextView, usernameTextView, postsCountTextView)
        loadUserPosts(userId)
        loadFollowCounts(userId)

        findViewById<LinearLayout>(R.id.followersLayout).setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("type", "followers")
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.followingLayout).setOnClickListener {
            val intent = Intent(this, FollowingListActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("type", "following")
            startActivity(intent)
        }

        setupNavigation()
    }

    private fun loadUserProfile(userId: Int, dp: ImageView, nameTextView: TextView, usernameTextView: TextView, postsCountTextView: TextView) {
        ApiService.getUserProfile(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val user = jsonObject.getAsJsonObject("user")
                            val firstName = user.get("first_name")?.asString ?: ""
                            val lastName = user.get("last_name")?.asString ?: ""
                            val username = user.get("username")?.asString ?: ""
                            val profilePhotoUrl = user.get("profile_photo_url")?.asString
                            val postsCount = user.get("posts_count")?.asInt ?: 0

                            nameTextView.text = "$firstName $lastName".trim()
                            usernameTextView.text = username
                            postsCountTextView.text = postsCount.toString()

                            if (!profilePhotoUrl.isNullOrEmpty()) {
                                Glide.with(this@ProfileActivity)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.ic_profile_vector)
                                    .error(R.drawable.ic_profile_vector)
                                    .circleCrop()
                                    .into(dp)
                            } else {
                                dp.setImageResource(R.drawable.ic_profile_vector)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadUserPosts(userId: Int) {
        // Get posts for this user
        ApiService.getPosts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val postsArray = jsonObject.getAsJsonArray("posts")
                            postList.clear()

                            postsArray?.forEach { element ->
                                val postJson = element.asJsonObject
                                val postUserId = postJson.get("user_id")?.asInt ?: 0
                                // Only add posts from this user
                                if (postUserId == userId) {
                                    val post = Post(
                                        id = postJson.get("id")?.asInt ?: 0,
                                        user_id = postUserId,
                                        image_url = postJson.get("image_url")?.asString ?: "",
                                        caption = postJson.get("caption")?.asString ?: "",
                                        username = postJson.get("username")?.asString ?: "",
                                        first_name = postJson.get("first_name")?.asString ?: "",
                                        last_name = postJson.get("last_name")?.asString ?: "",
                                        profile_photo_url = postJson.get("profile_photo_url")?.asString,
                                        like_count = postJson.get("like_count")?.asInt ?: 0,
                                        comment_count = postJson.get("comment_count")?.asInt ?: 0,
                                        is_liked = postJson.get("is_liked")?.asBoolean ?: false
                                    )
                                    postList.add(post)
                                }
                            }

                            postAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error silently
            }
        })
    }

    private fun loadFollowCounts(userId: Int) {
        ApiService.getFollowCounts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            followersCount.text = jsonObject.get("followers_count")?.asInt?.toString() ?: "0"
                            followingCount.text = jsonObject.get("following_count")?.asInt?.toString() ?: "0"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error silently
            }
        })
    }

    private fun setupNavigation() {
        val home = findViewById<ImageView>(R.id.home)
        val search = findViewById<ImageView>(R.id.search)
        val add = findViewById<ImageView>(R.id.add)
        val heart = findViewById<ImageView>(R.id.heart)
        val profile = findViewById<ImageView>(R.id.profile)
        val addNew = findViewById<ImageView>(R.id.addnew)
        val editProfile = findViewById<TextView>(R.id.edit)

        addNew.setOnClickListener {
            startActivity(Intent(this, MainActivity14::class.java))
        }
        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        add.setOnClickListener {
            startActivity(Intent(this, MainActivity19::class.java))
        }
        heart.setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
