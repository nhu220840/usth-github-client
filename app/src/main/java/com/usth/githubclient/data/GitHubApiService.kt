package com.usth.githubclient.data

import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApiService {
    @GET("users/{username}/repos")
    suspend fun getUserRepos(@Path("username") username: String): List<Repository>
}


