package com.example.testingfirebaserealtime.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.Article
import com.example.testingfirebaserealtime.databinding.ActivityDetailArticleBinding
import com.google.firebase.database.*

class DetailArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailArticleBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseDatabase.getInstance()
        val articleId = intent.getStringExtra("articleId")
        if (articleId != null) {
            database = db.reference.child("articles").child(articleId)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val article = snapshot.getValue(Article::class.java)
                    if (article != null) {
                        binding.articleTitle.text = article.title
                        binding.articleContent.text = article.content
                        binding.articleDate.text = article.date
                        Glide.with(this@DetailArticleActivity).load(article.imageUrl).into(binding.articleImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailArticleActivity, "Failed to load article", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
