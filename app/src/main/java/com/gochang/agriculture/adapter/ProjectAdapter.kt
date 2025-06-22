package com.gochang.agriculture.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gochang.agriculture.fragment.ProjectDetailFragment
import com.gochang.agriculture.model.Project

class ProjectAdapter(
    activity: FragmentActivity,
    private val projects: List<Project>
) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = projects.size
    
    override fun createFragment(position: Int): Fragment {
        return ProjectDetailFragment.newInstance(projects[position], position, projects.size)
    }
}