package com.crakac.ofutodon.ui.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.crakac.ofutodon.ui.StatusFragment

/**
 * Created by Kosuke on 2017/04/26.
 */
class MyFragmentPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments = ArrayList<StatusFragment>()

    override fun getItem(position: Int): StatusFragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    fun add(f : StatusFragment){
        fragments.add(f)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return getItem(position).getTitle()
    }
}