package com.crakac.ofutodon.ui

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.crakac.ofutodon.R


/**
 * https://github.com/saulmm/CoordinatorBehaviorExample/blob/master/app/src/main/java/saulmm/myapplication/AvatarImageBehavior.java
 */
@SuppressWarnings("unused")
class AvatarImageBehaviour(val context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ImageView>(context, attrs) {
    val TAG: String = "AvatarImageBehaviour"
    private var mCustomFinalYPosition: Float = 0f
    private var mCustomStartXPosition: Float = 0f
    private var mCustomStartToolbarPosition: Float = 0f
    private var mCustomStartHeight: Float = 0f
    private var mCustomFinalHeight: Float = 0f

    private var mAvatarMaxSize: Float = 0f
    private var mFinalLeftAvatarPadding: Float = 0f
    private var mStartPosition: Float = 0f
    private var mStartXPosition: Int = 0
    private var mStartToolbarPosition: Float = 0f
    private var mStartYPosition: Int = 0
    private var mFinalYPosition: Int = 0
    private var mStartHeight: Int = 0
    private var mFinalXPosition: Int = 0
    private var mChangeBehaviorPoint: Float = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageBehavior)
        mCustomFinalYPosition = a.getDimension(R.styleable.AvatarImageBehavior_finalYPosition, 0f)
        mCustomStartXPosition = a.getDimension(R.styleable.AvatarImageBehavior_startXPosition, 0f)
        mCustomStartToolbarPosition = a.getDimension(R.styleable.AvatarImageBehavior_startToolbarPosition, 0f)
        mCustomStartHeight = a.getDimension(R.styleable.AvatarImageBehavior_startHeight, 0f)
        mCustomFinalHeight = a.getDimension(R.styleable.AvatarImageBehavior_finalHeight, 0f)
        a.recycle()

        bindDimensions()

        mFinalLeftAvatarPadding = context.resources.getDimension(R.dimen.spacing_normal)
    }

    fun bindDimensions() {
        mAvatarMaxSize = context.resources.getDimension(R.dimen.image_button)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ImageView, dependency: View): Boolean {
        return dependency is Toolbar
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ImageView, dependency: View): Boolean {
        maybeInitProperties(child, dependency)
        val maxScrollDistance = mStartToolbarPosition.toInt()
        val expandedPercentageFactor = dependency.y / maxScrollDistance

        if(expandedPercentageFactor < mChangeBehaviorPoint){
            val heightFactor = (mChangeBehaviorPoint - expandedPercentageFactor) / mChangeBehaviorPoint
            val distanceXToSubtract = ((mStartXPosition - mFinalXPosition) * heightFactor) + (child.height / 2)
            val distanceYToSubtract = ((mStartYPosition - mFinalYPosition) * (1f * expandedPercentageFactor)) + (child.height / 2)

            child.x = mStartXPosition - distanceXToSubtract
            child.y = mStartYPosition - distanceYToSubtract

            val heightToSubtract = ((mStartHeight - mCustomFinalHeight) * heightFactor)
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            lp.width = (mStartHeight - heightToSubtract).toInt()
            lp.height = (mStartHeight - heightToSubtract).toInt()
            child.layoutParams = lp
        } else {
            val distanceYToSubtract = ((mStartYPosition - mFinalYPosition) * (1f - expandedPercentageFactor)) + (mStartHeight / 2)
            child.x = (mStartXPosition - child.width / 2).toFloat()
            child.y = mStartYPosition - distanceYToSubtract

            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            lp.width = mStartHeight
            lp.height = mStartHeight
            child.layoutParams = lp
        }
        return true
    }

    private fun maybeInitProperties(child: ImageView, dependency: View) {
        if (mStartYPosition == 0)
            mStartYPosition = dependency.y.toInt()

        if (mFinalYPosition == 0)
            mFinalYPosition = dependency.height / 2

        if (mStartHeight == 0)
            mStartHeight = child.height

        if (mStartXPosition == 0)
            mStartXPosition = (child.x + child.width / 2).toInt()

        if (mFinalXPosition == 0)
            mFinalXPosition = (context.resources.getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material) + mCustomFinalHeight).toInt() / 2

        if (mStartToolbarPosition == 0f)
            mStartToolbarPosition = dependency.y

        if (mChangeBehaviorPoint == 0f) {
            mChangeBehaviorPoint = (child.height - mCustomFinalHeight) / (2f * (mStartYPosition - mFinalYPosition))
        }

    }

    fun getStatusBarHeight(): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}