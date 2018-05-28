package com.crakac.ofutodon.ui.adapter

import com.crakac.ofutodon.api.entity.Status

interface StatusChangeListener {
    fun onUpdate(status: Status){}
    fun onRemove(status: Status){}
}