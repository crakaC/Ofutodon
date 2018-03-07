package com.crakac.ofutodon.util

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import com.crakac.ofutodon.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileUtil private constructor() {
    val TAG: String = "FileUtil"

    companion object {
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        fun createTemporaryImageUri(context: Context): Uri? {
            val timeStamp = sdf.format(Date())
            return try {
                val tempFile = File.createTempFile(timeStamp, ".jpg")
                FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), tempFile)
            } catch (e: IOException) {
                null
            }
        }
    }
}