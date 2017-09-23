package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

open class Identifiable(@SerializedName("id") val id: Long = 0)