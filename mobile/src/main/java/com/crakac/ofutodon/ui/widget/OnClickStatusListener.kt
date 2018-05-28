package com.crakac.ofutodon.ui.widget

import android.widget.ImageView
import com.crakac.ofutodon.api.entity.Status

interface OnClickStatusListener {
    fun onItemClicked(status: Status)
    fun onIconClicked(icon: ImageView, status: Status){}
    fun onReplyClicked(icon: ImageView, status: Status){}
    fun onBoostClicked(icon: ImageView, status: Status){}
    fun onFavoriteClicked(icon: ImageView, status: Status){}
    fun onMenuClicked(status: Status, menuId: Int){}
    fun onClickAttachment(status: Status, attachmentIndex: Int){}
}
