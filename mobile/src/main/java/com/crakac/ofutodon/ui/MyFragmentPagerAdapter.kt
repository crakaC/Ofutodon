package com.crakac.ofutodon.ui

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

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

//    override fun getCustomTabView(parent: ViewGroup?, position: Int): View {
//    }
//
//
//    override fun tabUnselected(tab: View?) {
//    }
//
//    override fun tabSelected(tab: View?) {
//    }
}