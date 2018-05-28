package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.SearchView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Results
import com.crakac.ofutodon.ui.adapter.SimplePagerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    val TAG: String = "SearchActivity"
    lateinit var pager: ViewPager
    lateinit var accountAdapter: ReplacableListFragment.AccountAdapter
    lateinit var hashtagAdapter: ReplacableListFragment.HashtagAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
        }
        pager = findViewById(R.id.pager)
        val pagerAdapter = SimplePagerAdapter(supportFragmentManager)
        accountAdapter = ReplacableListFragment.AccountAdapter(this)
        hashtagAdapter = ReplacableListFragment.HashtagAdapter(this)

        val accountFragment = ReplacableListFragment().apply {
            setAdapter(accountAdapter)
        }
        val hashtagFragment = ReplacableListFragment().apply {
            setAdapter(hashtagAdapter)
        }

        pagerAdapter.add(accountFragment)
        pagerAdapter.add(hashtagFragment)
        pager.adapter = pagerAdapter

        val tab = findViewById<TabLayout>(R.id.tab)
        tab.setupWithViewPager(pager)

        tab.getTabAt(0)?.text = getString(R.string.account)
        tab.getTabAt(1)?.text = getString(R.string.tag)
    }

    override fun onStart() {
        super.onStart()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        val searchView = menu!!.findItem(R.id.search).actionView as SearchView
        searchView.isIconified = false
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean{
                if (newText == null || newText.isEmpty()) {
                    accountAdapter.set(emptyList())
                    hashtagAdapter.set(emptyList())
                    return false
                }
                MastodonUtil.api?.search(newText, newText.contains('@'))?.enqueue(
                        object : Callback<Results> {
                            override fun onFailure(call: Call<Results>?, t: Throwable?) {
                            }

                            override fun onResponse(call: Call<Results>?, response: Response<Results>?) {
                                if (response == null || !response.isSuccessful) return
                                val results = response.body() ?: return
                                accountAdapter.set(results.accounts)
                                hashtagAdapter.set(results.hashtags)
                            }
                        }
                )
                return true
            }
        })
        return true
    }
}