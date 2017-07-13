package com.crakac.ofutodon.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.crakac.ofutodon.R
import com.crakac.ofutodon.util.AnimUtils
import java.util.*

class FabTransform : Transition {
    companion object {
        val TAG = "FabTransform"
        val EXTRA_FAB_COLOR = "EXTRA_FAB_COLOR"
        val EXTRA_FAB_ICON_RES_ID = "EXTRA_FAB_ICON_RES_ID"
        val DEFAULT_DURATION = 240L
        val PROP_BOUNDS = "ofutodon:fabTransform:bounds"
        val TRANSITION_PROPERTIES = arrayOf(PROP_BOUNDS)

        fun addExtras(intent: Intent, @ColorInt fabColor: Int,
                               @DrawableRes fabIconResId: Int) {
            intent.putExtra(EXTRA_FAB_COLOR, fabColor)
            intent.putExtra(EXTRA_FAB_ICON_RES_ID, fabIconResId)
        }

        fun setup(activity: Activity, target: View?): Boolean {
            val intent = activity.intent
            if (!intent.hasExtra(EXTRA_FAB_COLOR) || !intent.hasExtra(EXTRA_FAB_ICON_RES_ID)) {
                return false
            }

            val color = intent.getIntExtra(EXTRA_FAB_COLOR, Color.TRANSPARENT)
            val icon = intent.getIntExtra(EXTRA_FAB_ICON_RES_ID, -1)
            val sharedEnter = FabTransform(color, icon)
            if (target != null) {
                sharedEnter.addTarget(target)
            }
            activity.window.sharedElementEnterTransition = sharedEnter
            return true
        }
    }

    private var color: Int = 0
    private var icon: Int = 0

    constructor(@ColorInt color: Int, @ColorInt resId: Int){
        this.color = color
        this.icon = resId
        pathMotion = GravityArcMotion()
        duration = DEFAULT_DURATION
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FabTransform)
        a.let {
            if(!a.hasValue(R.styleable.FabTransform_fabColor) || !a.hasValue(R.styleable.FabTransform_fabIcon)){
                throw IllegalArgumentException("Must provide both color & icon.")
            }
            color = a.getColor(R.styleable.FabTransform_fabColor, Color.TRANSPARENT)
            icon = a.getResourceId(R.styleable.FabTransform_fabIcon, 0)
            pathMotion = GravityArcMotion()
            if(duration < 0){
                duration = DEFAULT_DURATION
            }
            a.recycle()
        }
    }

    override fun getTransitionProperties(): Array<String> {
        return TRANSITION_PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues?) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues?) {
        captureValues(transitionValues)
    }

    override fun createAnimator(sceneRoot: ViewGroup?, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) return null

        val startBounds = startValues.values[PROP_BOUNDS] as Rect
        val endBounds = endValues.values[PROP_BOUNDS] as Rect

        val fromFab = endBounds.width() > startBounds.width()
        val view = endValues.view
        val dialogBounds = if (fromFab) endBounds else startBounds
        val fabBounds = if (fromFab) startBounds else endBounds
        val fastOutSlowInInterpolator = FastOutSlowInInterpolator()
        val duration = duration
        val halfDuration = duration / 2
        val twoThirdsDuration = duration * 2 / 3

        if (!fromFab) {
            // Force measure / layout the dialog back to it's original bounds
            view.measure(
                    makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY),
                    makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY))
            view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom)
        }

        val translationX = startBounds.centerX() - endBounds.centerX()
        val translationY = startBounds.centerY() - endBounds.centerY()
        if (fromFab) {
            view.translationX = translationX.toFloat()
            view.translationY = translationY.toFloat()
        }

        // Add a color overlay to fake appearance of the FAB
        val fabColor = ColorDrawable(color)
        fabColor.setBounds(0, 0, dialogBounds.width(), dialogBounds.height())
        if (!fromFab) fabColor.alpha = 0
        view.overlay.add(fabColor)

        // Add an icon overlay again to fake the appearance of the FAB
        val fabIcon = ContextCompat.getDrawable(sceneRoot?.context, icon).mutate()
        val iconLeft = (dialogBounds.width() - fabIcon.intrinsicWidth) / 2
        val iconTop = (dialogBounds.height() - fabIcon.intrinsicHeight) / 2
        fabIcon.setBounds(iconLeft, iconTop,
                iconLeft + fabIcon.intrinsicWidth,
                iconTop + fabIcon.intrinsicHeight)
        if (!fromFab) fabIcon.alpha = 0
        view.overlay.add(fabIcon)

        // Circular clip from/to the FAB size
        val circularReveal: Animator
        if (fromFab) {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    view.width / 2,
                    view.height / 2,
                    (startBounds.width() / 2).toFloat(),
                    Math.hypot((endBounds.width() / 2).toDouble(), (endBounds.height() / 2).toDouble()).toFloat())
            circularReveal.interpolator = FastOutLinearInInterpolator()
        } else {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    view.width / 2,
                    view.height / 2,
                    Math.hypot((startBounds.width() / 2).toDouble(), (startBounds.height() / 2).toDouble()).toFloat(),
                    (endBounds.width() / 2).toFloat())
            circularReveal.interpolator = LinearOutSlowInInterpolator()

            // Persist the end clip i.e. stay at FAB size after the reveal has run
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            val left = (view.width - fabBounds.width()) / 2
                            val top = (view.height - fabBounds.height()) / 2
                            outline.setOval(
                                    left, top, left + fabBounds.width(), top + fabBounds.height())
                            view.clipToOutline = true
                        }
                    }
                }
            })
        }
        circularReveal.duration = duration

        // Translate to end position along an arc
        val translate = ObjectAnimator.ofFloat(
                view,
                View.TRANSLATION_X,
                View.TRANSLATION_Y,
                if (fromFab)
                    pathMotion.getPath(translationX.toFloat(), translationY.toFloat(), 0f, 0f)
                else
                    pathMotion.getPath(0f, 0f, (-translationX).toFloat(), (-translationY).toFloat()))
        translate.duration = duration
        translate.interpolator = fastOutSlowInInterpolator

        // Fade contents of non-FAB view in/out
        var fadeContents: MutableList<Animator>? = null
        if (view is ViewGroup) {
            val vg = view
            fadeContents = ArrayList<Animator>(vg.childCount)
            for (i in vg.childCount - 1 downTo 0) {
                val child = vg.getChildAt(i)
                val fade = ObjectAnimator.ofFloat(child, View.ALPHA, if (fromFab) 1f else 0f)
                if (fromFab) {
                    child.alpha = 0f
                }
                fade.duration = twoThirdsDuration
                fade.interpolator = fastOutSlowInInterpolator
                fadeContents.add(fade)
            }
        }

        // Fade in/out the fab color & icon overlays
        val colorFade = ObjectAnimator.ofInt(fabColor, "alpha", if (fromFab) 0 else 255)
        val iconFade = ObjectAnimator.ofInt(fabIcon, "alpha", if (fromFab) 0 else 255)
        if (!fromFab) {
            colorFade.startDelay = halfDuration
            iconFade.startDelay = halfDuration
        }
        colorFade.duration = halfDuration
        iconFade.duration = halfDuration
        colorFade.interpolator = fastOutSlowInInterpolator
        iconFade.interpolator = fastOutSlowInInterpolator

        // Work around issue with elevation shadows. At the end of the return transition the shared
        // element's shadow is drawn twice (by each activity) which is jarring. This workaround
        // still causes the shadow to snap, but it's better than seeing it double drawn.
        var elevation: Animator? = null
        if (!fromFab) {
            elevation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, -view.elevation)
            elevation.duration = duration
            elevation.interpolator = fastOutSlowInInterpolator
        }

        // Run all animations together
        val transition = AnimatorSet()
        transition.playTogether(circularReveal, translate, colorFade, iconFade)
        transition.playTogether(fadeContents)
        if (elevation != null) transition.play(elevation)
        if (fromFab) {
            transition.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Clean up
                    view.overlay.clear()
                }
            })
        }
        return AnimUtils.NoPauseAnimator(transition)
    }

    private fun captureValues(transitionValues: TransitionValues?){
        if (transitionValues == null) return
        val view  = transitionValues.view
        if(view == null || view.width <= 0 || view.height <= 0) return

        transitionValues.values.put(PROP_BOUNDS, Rect(view.left, view.top, view.right, view.bottom))
    }
}