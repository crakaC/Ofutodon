package com.crakac.ofutodon

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by Kosuke on 2017/04/26.
 */
class MyFragmentPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = ArrayList<StatusFragment>()

    override fun getItem(position: Int): Fragment {
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
        return (getItem(position) as StatusFragment).getTitle()
    }
}