package com.crakac.ofutodon.utils

import com.crakac.ofutodon.model.api.entity.Status
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test

class HtmlUtilTest {
    val TAG: String = "HtmlUtilTest"

    @Test
    fun content(){
        val jsonString = javaClass.getResourceAsStream("/status.json").bufferedReader().readText()
        val status = Gson().fromJson<Status>(jsonString, Status::class.java)
        Assert.assertNotNull(status)
    }
}