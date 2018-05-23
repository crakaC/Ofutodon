package com.crakac.ofutodon.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.util.C
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class TootService : IntentService("TootService") {
    val TAG: String = "TootService"
    private val channelId = "TootService"
    private val channelName = "toot"
    private val id = 3838

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // create NotificationChannel for Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            channel.enableVibration(false)
            manager.createNotificationChannel(channel)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_send).setProgress(0, 0, true).setContentText(getString(R.string.sending_toot)).setOngoing(true)
        startForeground(id, builder.build())

        val text = intent.getStringExtra(C.TEXT) ?: ""
        val spoilerText = intent.getStringExtra(C.SPOILER_TEXT)
        val replyToId = intent.getLongExtra(C.REPLY_TO_ID, -1)
        val visibility = intent.getStringExtra(C.VISIBILITY)
        val isSensitive = intent.getBooleanExtra(C.IS_SENSITIVE, false)
        val attachmentFiles = intent.getSerializableExtra(C.ATTACHMENTS) as ArrayList<File>
        val attachments = ArrayList<Attachment>(attachmentFiles.count())

        for (file in attachmentFiles) {
            uploadMedia(file)?.let {
                attachments.add(it)
            }
        }

        val response = MastodonUtil.api?.postStatus(
                StatusBuilder(
                        replyTo = if (replyToId > 0) replyToId else null,
                        visibility = visibility,
                        spoilerText = spoilerText,
                        mediaIds = attachments.map { attachment -> attachment.id },
                        isSensitive = isSensitive,
                        text = text
                ))?.execute()

        if(response == null || !response.isSuccessful){
            val builder = NotificationCompat.Builder(this, channelId)
            builder.setSmallIcon(R.drawable.ic_clear)
                    .setProgress(0, 0, false)
                    .setContentText(getString(R.string.impossible))
            notifyManager.notify(1818, builder.build())
        }
    }

    private fun uploadMedia(file: File): Attachment? {
        val byteArray = file.readBytes()
        val attachment = RequestBody.create(MediaType.parse("image/*"), byteArray)
        val body = MultipartBody.Part.createFormData("file", "media_attachment", attachment)
        val response = MastodonUtil.api?.uploadMediaAttachment(body)?.execute()
        return if (response != null && response.isSuccessful) {
            response.body()!!
        } else null
    }
}