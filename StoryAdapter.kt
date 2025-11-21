package com.example.assignment1

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(
    private val context: Context,
    private val userStories: List<userStory>,
    private val onStoryClick: (userStory) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(userStories[position])
    }

    override fun getItemCount(): Int = userStories.size

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: CircleImageView = itemView.findViewById(R.id.storyImage)
        private val usernameText: TextView = itemView.findViewById(R.id.storyName)

        fun bind(userStory: userStory) {
            usernameText.text = userStory.username

            if (userStory.stories.size > 1) {
                usernameText.text = "${userStory.username} (${userStory.stories.size})"
            }

            try {
                Log.d("StoryAdapter", "Decoding image for: ${userStory.username} (${userStory.stories.size} stories)")

                val imageBytes = Base64.decode(userStory.latestImageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    storyImage.setImageBitmap(bitmap)
                    Log.d("StoryAdapter", "Image loaded: ${bitmap.width}x${bitmap.height}")
                } else {
                    Log.e("StoryAdapter", " Bitmap decode returned null")
                    storyImage.setImageResource(R.drawable.ic_profile)
                }

            } catch (e: IllegalArgumentException) {
                Log.e("StoryAdapter", " Invalid Base64 string: ${e.message}")
                storyImage.setImageResource(R.drawable.ic_profile)
            } catch (e: Exception) {
                Log.e("StoryAdapter", "Error decoding image: ${e.message}", e)
                storyImage.setImageResource(R.drawable.ic_profile)
            }

            itemView.setOnClickListener {
                Log.d("StoryAdapter", "Clicked ${userStory.username}'s story (${userStory.stories.size} total)")
                onStoryClick(userStory) // call  HomeActivity
            }

        }
    }
}