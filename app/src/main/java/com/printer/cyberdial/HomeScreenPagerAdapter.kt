package com.printer.cyberdial

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeScreenPagerAdapter(fragmentActivity: FragmentActivity, private val totalPages: Int) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = totalPages

    override fun createFragment(position: Int): Fragment {
        return HomeScreenFragment.newInstance(position)
    }
}