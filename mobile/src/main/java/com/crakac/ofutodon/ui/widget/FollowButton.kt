package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.crakac.ofutodon.R

class FollowButton(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    companion object {
        val STATE_IS_FOLLOWING = intArrayOf(R.attr.isFollowing)
        val STATE_IS_LOADING = intArrayOf(R.attr.isLoading)
    }

    val textView: TextView
    val icon: ImageView
    private var mIsFollowing: Boolean = false
    var isFollowing: Boolean
        get() = mIsFollowing
        set(value) {
            mIsFollowing = value
            post {
                icon.visibility = View.VISIBLE
                if (isFollowing) {
                    textView.text = context.getString(R.string.unfollow)
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    icon.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
                    icon.setImageResource(R.drawable.ic_person)
                } else {
                    textView.text = context.getString(R.string.follow)
                    textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                    icon.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent))
                    icon.setImageResource(R.drawable.ic_person_add)
                }
                refreshDrawableState()
            }
        }

    private var mIsLoading = false
    var isLoading
        get() = mIsLoading
        set(value) {
            mIsLoading = value
            post {
                refreshDrawableState()
            }
        }

    init {
        val v = View.inflate(context, R.layout.widget_follow_button, this)
        textView = v.findViewById(R.id.text)
        icon = v.findViewById(R.id.icon)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (mIsFollowing) {
            View.mergeDrawableStates(drawableState, STATE_IS_FOLLOWING)
        }
        if (mIsLoading) {
            View.mergeDrawableStates(drawableState, STATE_IS_LOADING)
        }
        return drawableState
    }
}