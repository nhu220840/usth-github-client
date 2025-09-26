package com.usth.githubclient.data

import com.google.gson.annotations.SerializedName

data class Repository(
    val id: Int,
    val name: String,
    val description: String?,
    val language: String?,
    @SerializedName("stargazers_count")
    val stars: Int,
    @SerializedName("forks_count")
    val forks: Int
)

