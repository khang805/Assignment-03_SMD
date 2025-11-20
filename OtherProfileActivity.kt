package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.JsonParser

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFollowers: TextView
    private lateinit var tvFollowing: TextView
    private lateinit var btnFollow: TextView
    private lateinit var nameTop: TextView

    private var currentUserId: Int = 0
    private var targetUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId()

        ivProfilePic = findViewById(R.id.iv_profile_picture)
        tvDisplayName = findViewById(R.id.tv_display_name)
        tvUsername = findViewById(R.id.tv_username_small)
        tvBio = findViewById(R.id.tv_bio_text)
        tvFollowers = findViewById(R.id.tv_followers_count)
        tvFollowing = findViewById(R.id.tv_following_count)
        btnFollow = findViewById(R.id.following)
        nameTop = findViewById(R.id.tv_username_header)

        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navSearch = findViewById<ImageView>(R.id.nav_search)
        val navAdd = findViewById<ImageView>(R.id.nav_add)
        val navFavorite = findViewById<ImageView>(R.id.nav_favorite)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val backArrow = findViewById<ImageView>(R.id.iv_back_arrow)

        targetUserId = intent.getIntExtra("user_id", 0)
        if (targetUserId == 0) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Prevent viewing own profile
        if (currentUserId == targetUserId) {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
            return
        }

        backArrow.setOnClickListener { finish() }

        navHome.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
        navSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
        navAdd.setOnClickListener { startActivity(Intent(this, AddPostActivity::class.java)) }
        navFavorite.setOnClickListener { startActivity(Intent(this, MainActivity11::class.java)) }
        navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }

        loadProfileData()
        loadFollowCounts()
        checkFollowStatus()
        setupFollowButton()
        setupFollowListClicks()
    }

    private fun loadProfileData() {
        ApiService.getUserProfile(targetUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val user = jsonObject.getAsJsonObject("user")
                            val username = user.get("username")?.asString ?: ""
                            val firstName = user.get("first_name")?.asString ?: ""
                            val lastName = user.get("last_name")?.asString ?: ""
                            val bio = user.get("bio")?.asString ?: ""
                            val profilePhotoUrl = user.get("profile_photo_url")?.asString

                            nameTop.text = username
                            tvDisplayName.text = "$firstName $lastName".trim().ifEmpty { username }
                            tvUsername.text = "@$username"
                            tvBio.text = bio

                            if (!profilePhotoUrl.isNullOrEmpty()) {
                                Glide.with(this@OtherProfileActivity)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.ic_profile_vector)
                                    .error(R.drawable.ic_profile_vector)
                                    .circleCrop()
                                    .into(ivProfilePic)
                            } else {
                                ivProfilePic.setImageResource(R.drawable.ic_profile_vector)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@OtherProfileActivity, "Failed to load profile: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadFollowCounts() {
        ApiService.getFollowCounts(targetUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            tvFollowers.text = jsonObject.get("followers_count")?.asInt?.toString() ?: "0"
                            tvFollowing.text = jsonObject.get("following_count")?.asInt?.toString() ?: "0"
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

    private fun checkFollowStatus() {
        ApiService.getFollowStatus(currentUserId, targetUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val followStatus = jsonObject.get("follow_status")?.asString ?: "not_following"
                            btnFollow.text = when (followStatus) {
                                "following" -> "Following"
                                "requested" -> "Requested"
                                else -> "Follow"
                            }
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

    private fun setupFollowButton() {
        btnFollow.setOnClickListener {
            when (btnFollow.text.toString()) {
                "Follow" -> sendFollowRequest()
                "Following" -> unfollowUser()
                "Requested" -> {
                    // Already requested, do nothing or show message
                    Toast.makeText(this, "Follow request already sent", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendFollowRequest() {
        ApiService.sendFollowRequest(currentUserId, targetUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            Toast.makeText(this@OtherProfileActivity, "Follow request sent", Toast.LENGTH_SHORT).show()
                            btnFollow.text = "Requested"
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Failed to send request"
                            Toast.makeText(this@OtherProfileActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@OtherProfileActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun unfollowUser() {
        ApiService.unfollowUser(currentUserId, targetUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            Toast.makeText(this@OtherProfileActivity, "Unfollowed", Toast.LENGTH_SHORT).show()
                            btnFollow.text = "Follow"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@OtherProfileActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupFollowListClicks() {
        tvFollowers.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("user_id", targetUserId)
            intent.putExtra("type", "followers")
            startActivity(intent)
        }

        tvFollowing.setOnClickListener {
            val intent = Intent(this, FollowingListActivity::class.java)
            intent.putExtra("user_id", targetUserId)
            intent.putExtra("type", "following")
            startActivity(intent)
        }
    }
}
