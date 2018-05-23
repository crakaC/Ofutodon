package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import com.crakac.ofutodon.R

class SearchActivity: AppCompatActivity() {
    val TAG: String = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
        }
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            // inset the toolbar down by the status bar height
            val lpToolbar = toolbar
                    .layoutParams as ViewGroup.MarginLayoutParams
            lpToolbar.topMargin += insets.systemWindowInsetTop
            lpToolbar.leftMargin += insets.systemWindowInsetLeft
            lpToolbar.rightMargin += insets.systemWindowInsetRight
            toolbar.layoutParams = lpToolbar
            insets.consumeSystemWindowInsets()
        }

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
        return true
    }
}