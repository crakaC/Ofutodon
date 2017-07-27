package com.crakac.ofutodon.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.support.annotation.ColorInt
import android.support.annotation.FloatRange
import android.support.v4.graphics.ColorUtils
import android.support.v7.graphics.Palette

class ViewUtil private constructor(){
    companion object {
        fun createRipple(palette: Palette,
                         @FloatRange(from = 0.0, to = 1.0) darkAlpha: Float,
                         @FloatRange(from = 0.0, to = 1.0) lightAlpha: Float,
                         @ColorInt fallbackColor: Int,
                         bounded: Boolean): RippleDrawable {
            var rippleColor = fallbackColor
            if (palette != null) {
                // try the named swatches in preference order
                if (palette.vibrantSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.vibrantSwatch!!.rgb, (darkAlpha * 255).toInt())

                } else if (palette.lightVibrantSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.lightVibrantSwatch!!.rgb,
                            (lightAlpha * 255).toInt())
                } else if (palette.darkVibrantSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.darkVibrantSwatch!!.rgb,
                            (darkAlpha * 255).toInt())
                } else if (palette.mutedSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.mutedSwatch!!.rgb, (darkAlpha * 255).toInt())
                } else if (palette.lightMutedSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.lightMutedSwatch!!.rgb,
                            (lightAlpha * 255).toInt())
                } else if (palette.darkMutedSwatch != null) {
                    rippleColor = ColorUtils.setAlphaComponent(palette.darkMutedSwatch!!.rgb, (darkAlpha * 255).toInt())
                }
            }
            return RippleDrawable(ColorStateList.valueOf(rippleColor), null,
                    if (bounded) ColorDrawable(Color.WHITE) else null)
        }
    }
}