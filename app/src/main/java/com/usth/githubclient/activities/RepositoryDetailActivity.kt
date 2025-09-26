
package com.usth.githubclient.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.usth.githubclient.R

class RepositoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository_detail)

        val name = intent.getStringExtra("repo_name")
        val description = intent.getStringExtra("repo_description")
        val language = intent.getStringExtra("repo_language")
        val stars = intent.getIntExtra("repo_stars", 0)
        val forks = intent.getIntExtra("repo_forks", 0)

        findViewById<TextView>(R.id.repoNameDetail).text = name
        findViewById<TextView>(R.id.repoDescriptionDetail).text = description ?: "No description"
        findViewById<TextView>(R.id.repoLanguageDetail).text = "Language: ${language ?: "Unknown"}"
        findViewById<TextView>(R.id.repoStarsDetail).text = "⭐ Stars: $stars"
        findViewById<TextView>(R.id.repoForksDetail).text = "🍴 Forks: $forks"
    }
}

