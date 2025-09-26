package com.usth.githubclient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.usth.githubclient.R
import com.usth.githubclient.fragments.RepositoriesListFragment

class RepositoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, RepositoriesListFragment())
            .commit()
    }
}


