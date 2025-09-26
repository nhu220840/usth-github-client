package com.usth.githubclient.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.usth.githubclient.R
import com.usth.githubclient.adapters.ReposListAdapter
import com.usth.githubclient.viewmodel.RepoViewModel

class RepositoriesListFragment : Fragment() {

    private val viewModel: RepoViewModel by viewModels()
    private lateinit var adapter: ReposListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repositories_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReposListAdapter(emptyList())
        recyclerView.adapter = adapter

        // Quan sát dữ liệu từ ViewModel
        viewModel.repos.observe(viewLifecycleOwner) { repos ->
            adapter.updateData(repos)
        }

        // Test với user mặc định
        viewModel.fetchRepos("octocat")

        return view
    }
}

