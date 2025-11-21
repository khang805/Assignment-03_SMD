package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity11 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main11)

        val youTab = findViewById<TextView>(R.id.following).rootView.findViewById<TextView>(R.id.following)
        val youText = findViewById<TextView>(R.id.following).rootView.findViewById<TextView>(R.id.following)
        val you = findViewById<TextView>(R.id.following).rootView.findViewById<TextView>(R.id.following)

        val youBtn = findViewById<TextView>(R.id.following).rootView.findViewById<TextView>(R.id.following)

        val youTabText = findViewById<TextView>(R.id.you)

        youTabText.setOnClickListener {
            val intent = Intent(this, MainActivity12::class.java)
            startActivity(intent)
        }

        val home = findViewById<ImageView>(R.id.home)
        val search = findViewById<ImageView>(R.id.search)
        val add = findViewById<ImageView>(R.id.add)
        val heart = findViewById<ImageView>(R.id.heart)
        val profile = findViewById<ImageView>(R.id.profile)

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
