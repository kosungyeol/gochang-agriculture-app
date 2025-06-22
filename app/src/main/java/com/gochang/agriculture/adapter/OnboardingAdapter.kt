package com.gochang.agriculture.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gochang.agriculture.fragment.UserInfoFragment
import com.gochang.agriculture.fragment.InterestSelectionFragment

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserInfoFragment()
            1 -> InterestSelectionFragment()
            else -> UserInfoFragment()
        }
    }
}