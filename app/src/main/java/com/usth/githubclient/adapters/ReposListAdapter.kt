package com.usth.githubclient.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usth.githubclient.R
import com.usth.githubclient.activities.RepositoryDetailActivity
import com.usth.githubclient.data.Repository

class ReposListAdapter(
    private var repos: List<Repository>
) : RecyclerView.Adapter<ReposListAdapter.RepoViewHolder>() {

    class RepoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.repoName)
        val description: TextView = itemView.findViewById(R.id.repoDescription)
        val stars: TextView = itemView.findViewById(R.id.repoStars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.repositories_list_item, parent, false)
        return RepoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repos[position]
        holder.name.text = repo.name
        holder.description.text = repo.description ?: "No description"
        holder.stars.text = "⭐ ${repo.stars}"

        // Khi click mở màn hình chi tiết
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RepositoryDetailActivity::class.java)
            intent.putExtra("repo_name", repo.name)
            intent.putExtra("repo_description", repo.description)
            intent.putExtra("repo_language", repo.language)
            intent.putExtra("repo_stars", repo.stars)
            intent.putExtra("repo_forks", repo.forks)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = repos.size

    fun updateData(newRepos: List<Repository>) {
        repos = newRepos
        notifyDataSetChanged()
    }
}


