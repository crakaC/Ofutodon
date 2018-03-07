package com.crakac.ofutodon.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.media.ExifInterface
import android.support.v4.app.NotificationCompat
import android.webkit.MimeTypeMap
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.util.C
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*
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
        val attachmentUris = intent.getParcelableArrayListExtra<Uri>(C.MEDIA_URIS)
        val attachments = ArrayList<Attachment>(attachmentUris.count())

        for (uri in attachmentUris) {
            val attachment = uploadMedia(uri)
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

    private fun uploadMedia(uri: Uri): Attachment? {
        val byteArray = createResizedBitmapByteArray(uri, 2048)
        val attachment = RequestBody.create(MediaType.parse("image/*"), byteArray)
        val body = MultipartBody.Part.createFormData("file", "media_attachment", attachment)
        val response = MastodonUtil.api?.uploadMediaAttachment(body)?.execute()
        return if (response != null && response.isSuccessful) {
            response.body()!!
        } else null
    }

    fun createResizedBitmapByteArray(uri: Uri, longEdge: Int): ByteArray {
        var bm: Bitmap = getSampledBitmap(uri, longEdge)
                ?: throw RuntimeException("something wrong")
        val scaleW = longEdge.toFloat() / bm.getWidth()
        val scaleH = longEdge.toFloat() / bm.getHeight()
        val scale = Math.min(scaleH, scaleW)

        if (scale < 1.0) {
            val w = (bm.getWidth() * scale + 0.5).toInt()
            val h = (bm.getHeight() * scale + 0.5).toInt()
            val old = bm
            try {
                bm = Bitmap.createScaledBitmap(old, w, h, true)
                old.recycle()
            } catch (oome: OutOfMemoryError) {
                oome.printStackTrace()
            }
        }

        val format = getCompressFormatFromUri(uri)
        val bytes = ByteArrayOutputStream()
        bm.compress(format, 90, bytes)

        if (format != Bitmap.CompressFormat.JPEG) {
            return bytes.toByteArray()
        }

        val tmpFile = File.createTempFile("TMP", ".jpg")
        val os: OutputStream
        try {
            os = BufferedOutputStream(FileOutputStream(tmpFile))
            os.write(bytes.toByteArray())
            os.flush()
            os.close()
            val src = ExifInterface(contentResolver.openInputStream(uri))
            val dst = ExifInterface(tmpFile.toString())
            val orientation = src.getAttribute(ExifInterface.TAG_ORIENTATION)
            dst.setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
            dst.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tmpFile.readBytes()
    }

    fun getSampledBitmap(uri: Uri, longEdge: Int): Bitmap? {
        var stream: InputStream? = null
        try {
            stream = BufferedInputStream(contentResolver.openInputStream(uri))

            val opt = BitmapFactory.Options()
            opt.inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, null, opt)
            val scaleH = opt.outHeight / longEdge
            val scaleW = opt.outWidth / longEdge
            opt.inSampleSize = Math.min(scaleH, scaleW)
            opt.inJustDecodeBounds = false
            stream.close()
            stream = BufferedInputStream(contentResolver.openInputStream(uri))
            return BitmapFactory.decodeStream(stream, null, opt)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            stream?.close()
        }
        return null
    }

    fun getCompressFormatFromUri(uri: Uri): Bitmap.CompressFormat {
        var type = contentResolver.getType(uri)
        if (type == null) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        return if (type.contains(Regex("png", RegexOption.IGNORE_CASE))) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
    }
}