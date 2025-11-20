package com.example.assignment1

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiService {
    private const val BASE_URL = "http://192.168.0.113/assignment-03/"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    interface ApiCallback {
        fun onSuccess(response: String)
        fun onError(error: String)
    }

    // Login API
    fun login(email: String, password: String, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}login.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Signup API
    fun signup(
        username: String,
        first_name: String,
        last_name: String,
        dob: String,
        email: String,
        password: String,
        callback: ApiCallback
    ) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("first_name", first_name)
            .add("last_name", last_name)
            .add("dob", dob)
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}signup.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Posts API
    fun getPosts(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}get_posts.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Upload Post API
    fun uploadPost(
        userId: Int,
        imageFile: File,
        caption: String,
        callback: ApiCallback
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("caption", caption)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}upload_post.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Like/Unlike Post API
    fun likePost(userId: Int, postId: Int, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("post_id", postId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}like_post.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Comments API
    fun getComments(postId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}get_comments.php?post_id=$postId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Add Comment API
    fun addComment(userId: Int, postId: Int, comment: String, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("post_id", postId.toString())
            .add("comment", comment)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}add_comment.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get User Profile API
    fun getUserProfile(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_user_profile.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Upload Profile Photo API
    fun uploadProfilePhoto(userId: Int, imageFile: File, callback: ApiCallback) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart(
                "profile_photo",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}apis/upload_profile_photo.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Update Profile API
    fun updateProfile(
        userId: Int,
        firstName: String? = null,
        lastName: String? = null,
        username: String? = null,
        bio: String? = null,
        website: String? = null,
        phone: String? = null,
        gender: String? = null,
        callback: ApiCallback
    ) {
        val formBuilder = FormBody.Builder()
            .add("user_id", userId.toString())
        
        firstName?.let { formBuilder.add("first_name", it) }
        lastName?.let { formBuilder.add("last_name", it) }
        username?.let { formBuilder.add("username", it) }
        bio?.let { formBuilder.add("bio", it) }
        website?.let { formBuilder.add("website", it) }
        phone?.let { formBuilder.add("phone", it) }
        gender?.let { formBuilder.add("gender", it) }

        val request = Request.Builder()
            .url("${BASE_URL}apis/update_profile.php")
            .post(formBuilder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Search Users API
    fun searchUsers(query: String, currentUserId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/search_users.php?query=${java.net.URLEncoder.encode(query, "UTF-8")}&current_user_id=$currentUserId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Send Follow Request API
    fun sendFollowRequest(senderId: Int, receiverId: Int, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("sender_id", senderId.toString())
            .add("receiver_id", receiverId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}apis/send_follow_request.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Accept Follow Request API
    fun acceptFollowRequest(receiverId: Int, senderId: Int, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("receiver_id", receiverId.toString())
            .add("sender_id", senderId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}apis/accept_follow_request.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Reject Follow Request API
    fun rejectFollowRequest(receiverId: Int, senderId: Int, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("receiver_id", receiverId.toString())
            .add("sender_id", senderId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}apis/reject_follow_request.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Unfollow User API
    fun unfollowUser(followerId: Int, followingId: Int, callback: ApiCallback) {
        val formBody = FormBody.Builder()
            .add("follower_id", followerId.toString())
            .add("following_id", followingId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}apis/unfollow_user.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Follow Status API
    fun getFollowStatus(followerId: Int, followingId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_follow_status.php?follower_id=$followerId&following_id=$followingId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Followers API
    fun getFollowers(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_followers.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Following API
    fun getFollowing(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_following.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Follow Requests API
    fun getFollowRequests(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_follow_requests.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }

    // Get Follow Counts API
    fun getFollowCounts(userId: Int, callback: ApiCallback) {
        val url = "${BASE_URL}apis/get_follow_counts.php?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onError("Server error: ${response.code}")
                }
            }
        })
    }
}

