package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment1.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.io.ByteArrayOutputStream

class HomeActivity : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy{
        ActivityHomeBinding.inflate(layoutInflater)
    }
    private val CAMERA_REQUEST_CODE = 100
    private lateinit var storyAdapter: StoryAdapter
    private val userStories = mutableListOf<userStory>()

    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private val dbRef = FirebaseDatabase.getInstance().getReference("stories")
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        Log.d("HomeActivity", "onCreate called")

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                // Store FCM token if needed
                // This can be sent to your API later
            }
        }
        binding.cameraBrown.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Log.e("HomeActivity", "No camera app available")
            }
        }

        // Bottom Navigation
        binding.home.setOnClickListener {

            loadStories()
        }
        binding.search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        binding.add.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }
        binding.heart.setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.sharebrown.setOnClickListener {
            startActivity(Intent(this, MainActivity9::class.java))
        }

        binding.storyRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        storyAdapter = StoryAdapter(this, userStories) { userStory ->
            openStoryViewer(userStory)
        }
        binding.storyRecyclerView.adapter = storyAdapter

        // Load stories from Firebase
        loadStories()

        binding.postRecyclerView.setHasFixedSize(true)
        binding.postRecyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(this, postList)
        binding.postRecyclerView.adapter = postAdapter

        readPosts()
    }

    fun readPosts() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        
        if (userId == -1) {
            return
        }

        ApiService.getPosts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = com.google.gson.JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            val postsArray = jsonObject.getAsJsonArray("posts")
                            postList.clear()
                            
                            postsArray?.forEach { element ->
                                val postJson = element.asJsonObject
                                val post = Post(
                                    id = postJson.get("id")?.asInt ?: 0,
                                    user_id = postJson.get("user_id")?.asInt ?: 0,
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
                            
                            postAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error silently or show toast
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeActivity", "onResume - reloading stories and posts")
        loadStories()
        readPosts() // Refresh posts when activity resumes
    }

    private fun openStoryViewer(userStory: userStory) {
        val intent = Intent(this, StoryViewerActivity::class.java)
        intent.putExtra("username", userStory.username)
        intent.putExtra("storyCount", userStory.stories.size)

        // Pass image URLs as array
        val imageUrls = userStory.stories.map { it.imageUrl }.toTypedArray()
        intent.putExtra("imageUrls", imageUrls)

        startActivity(intent)
    }

    private fun loadStories() {
        Log.d("HomeActivity", "Loading stories...")
        dbRef.get().addOnSuccessListener { snapshot ->
            val allStories = mutableListOf<Story>()

            for (child in snapshot.children) {
                // Skip the "test" node
                if (child.key == "test") {
                    continue
                }

                try {
                    val story = child.getValue(Story::class.java)
                    if (story != null && story.imageUrl.isNotEmpty()) {
                        allStories.add(story)
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Failed to parse story: ${child.key}, error: ${e.message}")
                }
            }

            val groupedStories = allStories.groupBy { it.userId }

            val newUserStories = mutableListOf<userStory>()
            for ((userId, stories) in groupedStories) {
                val sortedStories = stories.sortedByDescending { it.timestamp }
                val username = sortedStories.firstOrNull()?.username ?: "User"

                newUserStories.add(
                    userStory(
                        userId = userId,
                        username = username,
                        stories = sortedStories,
                        latestImageUrl = sortedStories.first().imageUrl
                    )
                )
            }

            // Sort users by latest story timestamp
            val sortedUserStories = newUserStories.sortedByDescending {
                it.stories.maxOfOrNull { story -> story.timestamp } ?: 0L
            }

            userStories.clear()
            userStories.addAll(sortedUserStories)

            Log.d("HomeActivity", "Total users with stories: ${userStories.size}")
            storyAdapter.notifyDataSetChanged()

        }.addOnFailureListener { e ->
            Log.e("HomeActivity", "Failed to load stories: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("HomeActivity", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                Log.d("HomeActivity", "Bitmap received: ${bitmap.width}x${bitmap.height}")
                uploadStoryToDatabase(bitmap)
            } else {
                Log.e("HomeActivity", "Bitmap is null")
            }
        }
    }

    private fun uploadStoryToDatabase(bitmap: Bitmap) {
        Log.d("HomeActivity", "Starting story upload...")

        val username = sessionManager.getUsername() ?: "User"
        Log.d("HomeActivity", "Using username from session: $username")
        uploadWithUsername(bitmap, username)
    }

    private fun uploadWithUsername(bitmap: Bitmap, username: String) {
        try {
            Log.d("HomeActivity", "Compressing image for user: $username")

            val baos = ByteArrayOutputStream()
            val maxSize = 1080  // bigger
            val scale = maxSize.toFloat() / Math.max(bitmap.width, bitmap.height)
            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledHeight = (bitmap.height * scale).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos) // higher quality

            val imageBytes = baos.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            Log.d("HomeActivity", "Image compressed: ${imageBytes.size} bytes")

            val userId = sessionManager.getUserId().toString()
            val story = Story(
                userId = userId,
                username = username,
                imageUrl = base64Image,
                timestamp = System.currentTimeMillis()
            )

            Log.d("HomeActivity", "Uploading story to database...")

            dbRef.push().setValue(story)
                .addOnSuccessListener {
                    Log.d("HomeActivity", "Story uploaded successfully!")
                    loadStories()
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Upload failed: ${e.message}", e)
                }

        } catch (e: Exception) {
            Log.e("HomeActivity", "Error processing image: ${e.message}", e)
        }
    }
}
