package com.usth.githubclient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usth.githubclient.data.GitHubApiService
import com.usth.githubclient.data.Repository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepoViewModel : ViewModel() {
    private val _repos = MutableLiveData<List<Repository>>()
    val repos: LiveData<List<Repository>> get() = _repos

    private val api: GitHubApiService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubApiService::class.java)

    fun fetchRepos(username: String) {
        viewModelScope.launch {
            try {
                _repos.value = api.getUserRepos(username)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


