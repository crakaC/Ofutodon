package com.crakac.ofutodon.ui.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SimplePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()

    override fun getItem(position: Int) = fragments[position]

    override fun getCount(): Int = fragments.size

    fun add(f: Fragment) {
        fragments.add(f)
        notifyDataSetChanged()
    }
}