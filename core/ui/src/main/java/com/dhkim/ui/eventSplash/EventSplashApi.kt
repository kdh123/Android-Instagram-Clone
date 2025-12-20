package com.dhkim.ui.eventSplash

import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity

/**
 * Referenced from: https://github.com/sankalpchauhan-me/fast-splash-experiment
 * */

object EventSplashApi {
    @JvmStatic
    fun attachTo(activity: ComponentActivity): Builder = Builder(activity)

    class Builder(private val activity: ComponentActivity) {
        private var config: EventSplashConfig? = null

        fun with(config: EventSplashConfig) = apply { this.config = config }

        fun show() {
            val finalConfig = config ?: DefaultConfig(
                appIcon = getAppIcon()
            )
            EventSplash(activity = activity, config = finalConfig)
        }

        private fun getAppIcon(): Drawable {
            return activity.packageManager.getApplicationIcon(activity.applicationInfo)
        }
    }
}