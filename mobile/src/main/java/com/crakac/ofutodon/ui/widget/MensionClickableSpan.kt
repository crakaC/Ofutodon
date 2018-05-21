package com.crakac.ofutodon.ui.widget

import android.content.Intent
import android.view.View
import com.crakac.ofutodon.ui.UserActivity

class MensionClickableSpan(text: String, url: String, val accountId: Long) : LinkClickableSpan(text, url) {
    val TAG: String = "MensionClickableSpan"

    override fun onClick(widget: View) {
        val intent = Intent(widget.context, UserActivity::class.java)
        UserActivity.setAccountId(intent, accountId)
        widget.context.startActivity(intent)
    }
}