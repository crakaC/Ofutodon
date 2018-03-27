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
    private val rand = Random()
    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // create NotificationChannel for Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(false)
            manager.createNotificationChannel(channel)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val id = rand.nextInt()
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_menu_send).setProgress(0, 0, true).setContentText(getString(R.string.sending_toot)).setOngoing(true)
        startForeground(id, builder.build())


        val text = intent.getStringExtra(C.TEXT) ?: ""
        val spoilerText = intent.getStringExtra(C.SPOILER_TEXT)
        val replyToId = intent.getLongExtra(C.REPLY_TO_ID, -1)
        val visibility = intent.getStringExtra(C.VISIBILITY)
        val isSensitive = intent.getBooleanExtra(C.IS_SENSITIVE, false)
        val sb = StringBuilder(text)
        val attachmentFiles = intent.getSerializableExtra(C.ATTACHMENTS) as ArrayList<File>
        val attachments = ArrayList<Attachment>(attachmentFiles.count())

        for (file in attachmentFiles) {
            val attachment = uploadMedia(file)
            if (attachment != null) {
                attachments.add(attachment)
                sb.append(" ")
                sb.append(attachment.url)
            }
        }

        MastodonUtil.api?.postStatus(
                StatusBuilder(
                        replyTo = if (replyToId > 0) replyToId else null,
                        visibility = visibility,
                        spoilerText = spoilerText,
                        mediaIds = attachments.map { attachment -> attachment.id },
                        isSensitive = isSensitive,
                        text = sb.toString()
                ))?.execute()
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