package com.crakac.ofutodon.ui.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.crakac.ofutodon.ui.TimelineFragment

/**
 * Created by Kosuke on 2017/04/26.
 */
class MyFragmentPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments = ArrayList<TimelineFragment>()

    override fun getItem(position: Int): TimelineFragment = fragments[position]

    override fun getCount(): Int = fragments.size

    fun add(f : TimelineFragment){
        fragments.add(f)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return getItem(position).getTitle()
    }
}