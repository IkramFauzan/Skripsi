package com.example.testingfirebaserealtime.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.Article
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.adapter.ArticleAdapter
import com.example.testingfirebaserealtime.databinding.FragmentListArticleBinding
import com.example.testingfirebaserealtime.detail.DetailArticleActivity
import com.google.firebase.database.*

class ListArticleFragment : Fragment() {

    private lateinit var binding: FragmentListArticleBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var database: DatabaseReference
    private lateinit var articleList: MutableList<Article>
    private lateinit var adapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListArticleBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance()
        database = db.reference.child("articles")

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        articleList = mutableListOf()
        adapter = ArticleAdapter(articleList) { article ->
            val intent = Intent(activity, DetailArticleActivity::class.java)
            intent.putExtra("articleId", article.id)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter

        loadArticles()

        return binding.root
    }

    private fun loadArticles() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                for (articleSnapshot in snapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    if (article != null) {
                        articleList.add(article)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
