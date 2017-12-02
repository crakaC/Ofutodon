package com.crakac.ofutodon.ui

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.util.PrefsUtil
import com.crakac.ofutodon.util.TextUtil
import com.crakac.ofutodon.util.ViewUtil
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception


class TootActivity : AppCompatActivity() {
    val PERMISSION_REQUEST = 1234
    val REQUEST_ATTACHMENT = 1235

    val TOOT_VISIBILITY = "toot_visibility"

    val MAX_TOOT_LENGTH = 500
    val MAX_ATTACHMENTS_NUM = 4

    val TAG = "TootActivity"

    val IMAGE_ATTACHMENT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator
    val URL_LENGTH = 24 // space(1char) + placeholder(23chars)

    companion object {
        private val REPLY_STATUS = "reply_to_status"
        private val ACTION_REPLY = "reply"
        fun addReplyInfo(intent: Intent, status: Status) {
            intent.action = ACTION_REPLY
            intent.putExtra(REPLY_STATUS, Gson().toJson(status))
        }
    }

    lateinit var container: View
    lateinit var spoilerText: EditText
    lateinit var textSeparator: View
    lateinit var tootText: EditText
    lateinit var imageAttachmentParent: LinearLayout
    lateinit var tootButton: TextView
    lateinit var attachmentButton: ImageView
    lateinit var visibilityButton: ImageView
    lateinit var cwButton: TextView
    lateinit var nsfwButton: ImageView
    lateinit var textCount: TextView

    private var isPosting = false

    // using for picking up attachmentUris by other app
    private var cameraUri: Uri? = null
    private var cameraFilePath: String? = null

    // keep attachmentUris
    private var uriAttachmentsList = ArrayList<Pair<Uri, Attachment>>(MAX_ATTACHMENTS_NUM)

    private var tootVisibility = defaultVisibility

    private val defaultVisibility: Status.Visibility
        get() {
            return Status.Visibility.valueOf(
                    PrefsUtil.getString(TOOT_VISIBILITY, Status.Visibility.Public.toString())!!)
        }

    private var isContentWarningEnabled = false

    fun getSpoilerText(): String? {
        if (!isContentWarningEnabled) {
            return null
        }
        return spoilerText.text.toString()
    }

    private var isNsfw = false

    private var replyToStatus: Status? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)

        container = findViewById(R.id.container)
        spoilerText = findViewById(R.id.spoiler_text)
        textSeparator = findViewById(R.id.text_separator)
        tootText = findViewById(R.id.toot_text)
        imageAttachmentParent = findViewById(R.id.image_attachments_root)
        tootButton = findViewById(R.id.toot)
        tootButton.setOnClickListener {
            toot()
        }
        attachmentButton = findViewById(R.id.add_photo)
        attachmentButton.setOnClickListener {
            onClickAttachment()
        }
        visibilityButton = findViewById(R.id.toot_visibility)
        visibilityButton.setOnClickListener { v ->
            onClickVisibility(v)
        }
        cwButton = findViewById(R.id.content_warning)
        cwButton.setOnClickListener {
            toggleContentWarning()
        }
        nsfwButton = findViewById(R.id.nsfw)
        nsfwButton.setOnClickListener {
            toggleNotSafeForWork()
        }

        textCount = findViewById(R.id.text_count)

        findViewById<View>(R.id.toot_background).setOnClickListener {
            dismiss()
        }

        FabTransform.setup(this, container)

        if (intent.action == ACTION_REPLY) {
            setUpReply()
        }

        tootText.addTextChangedListener(textWatcher)
        spoilerText.addTextChangedListener(textWatcher)

        updateTootButtonState()
        updateVisibilityButtonState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("CameraUri", cameraUri)
        outState.putString("CameraFilePath", cameraFilePath)
        outState.putParcelableArrayList("AttachmentUris", ArrayList(uriAttachmentsList.map { (uri) -> uri }))
        outState.putSerializable("Attachments", uriAttachmentsList.map { (_, attachment) -> attachment }.toTypedArray())

        outState.putString("tootText", tootText.text.toString())
        outState.putString("spoilerText", spoilerText.text.toString())
        outState.putBoolean("cw", isContentWarningEnabled)
        outState.putBoolean("nsfw", isNsfw)
        outState.putSerializable("visibility", tootVisibility)
        if (replyToStatus != null) {
            outState.putString(REPLY_STATUS, Gson().toJson(replyToStatus))
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        cameraUri = savedInstanceState.getParcelable("CameraUri")
        cameraFilePath = savedInstanceState.getString("CameraFilePath")
        val savedAttachments = savedInstanceState.getSerializable("Attachments") as Array<Attachment>
        val savedUris = savedInstanceState.getParcelableArrayList<Uri>("AttachmentUris")

        for (i in savedAttachments.indices) {
            uriAttachmentsList.add(Pair(savedUris[i], savedAttachments[i]))
        }

        savedUris.forEach { e -> addThumbnail(e) }


        tootText.setText(savedInstanceState.getString("tootText"))
        spoilerText.setText(savedInstanceState.getString("spoilerText"))

        tootVisibility = savedInstanceState.getSerializable("visibility") as Status.Visibility
        updateVisibilityButtonState()

        if (savedInstanceState.getBoolean("cw", false)) {
            toggleContentWarning()
        }
        if (savedInstanceState.getBoolean("nsfw", false)) {
            toggleNotSafeForWork()
        }

        savedInstanceState.getString(REPLY_STATUS)?.let {
            replyToStatus = Gson().fromJson(it, Status::class.java)
        }

        tootText.requestFocus()
        tootText.setSelection(tootText.length())
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ATTACHMENT) {
            handleAttachment(resultCode, data)
        }
    }

    fun dismiss() {
        finishAfterTransition()
    }

    fun toot() {
        if (isPosting) return
        isPosting = true
        tootButton.isEnabled = false

        val sb = StringBuilder(tootText.text)
        uriAttachmentsList.forEach { (_, attachment) ->
            if (sb.isNotEmpty()) {
                sb.append(" ")
            }
            sb.append(attachment.textUrl)
        }

        MastodonUtil.api?.postStatus(
                StatusBuilder(
                        replyTo = replyToStatus?.id,
                        visibility = tootVisibility.value,
                        spoilerText = getSpoilerText(),
                        mediaIds = uriAttachmentsList.map { (_, attachment) -> attachment.id },
                        isSensitive = isNsfw,
                        text = sb.toString()
                ))?.enqueue(onTootFinished)
    }

    var onTootFinished = object : Callback<Status> {
        override fun onFailure(call: Call<Status>?, t: Throwable?) {
            isPosting = false
            Log.d(TAG, "Failed")
        }

        override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
            isPosting = false
            if (response == null || !response.isSuccessful) {
                Log.d(TAG, "Server Error")
                Toast.makeText(this@TootActivity, "無理でした", Toast.LENGTH_LONG).show()
                return
            }

            Log.d(TAG, "Success:" + response.body()?.content)

            clearAttachments()
            if (isContentWarningEnabled) {
                toggleContentWarning()
            }

            tootText.text.clear()
            replyToStatus = null
        }
    }

    fun onClickAttachment() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    PERMISSION_REQUEST)
            return
        }

        if (!File(IMAGE_ATTACHMENT_DIR).exists()) {
            File(IMAGE_ATTACHMENT_DIR).mkdirs()
        }

        //take picture intent
        cameraFilePath = IMAGE_ATTACHMENT_DIR + TextUtil.currentTimeString() + ".jpg"
        cameraUri = FileProvider.getUriForFile(this, packageName + ".provider", File(cameraFilePath))

        val cameraIntents = ArrayList<Intent>()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val cameraList = packageManager.queryIntentActivities(cameraIntent, 0)
        for (info in cameraList) {
            val intent = Intent(cameraIntent)
            intent.component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
            intent.`package` = info.activityInfo.packageName
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            cameraIntents.add(intent)
        }

        //select picture intent
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/* video/*"
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))

        val pickTitle = getString(R.string.choose_image_or_take_picture)
        val chooserIntent = Intent.createChooser(galleryIntent, pickTitle)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray())

        startActivityForResult(chooserIntent, REQUEST_ATTACHMENT)
    }

    fun handleAttachment(resultCode: Int, data: Intent?) {
        Log.d(TAG, resultCode.toString())
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Activity Canceled")
            return
        }

        if (data?.data != null) {
            uploadMedia(data.data)
        } else if (cameraUri != null) {
            addToGallery(cameraFilePath!!)
            uploadMedia(cameraUri!!)
            cameraUri = null
            cameraFilePath = null
        }
    }

    fun addThumbnail(uri: Uri) {
        val v = ImageView(this)
        val edge = resources.getDimension(R.dimen.image_attachment).toInt()
        val p = LinearLayout.LayoutParams(edge, edge)
        v.layoutParams = p

        Glide.with(this).loadFromMediaStore(uri).listener(
                object : RequestListener<Uri, GlideDrawable> {
                    override fun onResourceReady(resource: GlideDrawable?, model: Uri?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val drawable = resource!!.current as GlideBitmapDrawable
                            Palette.from(drawable.bitmap).generate { palette ->
                                v.foreground = ViewUtil.createRipple(palette, 0.25f, 0.5f, getColor(R.color.mid_grey), true)
                            }
                        }
                        v.setOnClickListener { _ ->
                            val popup = PopupMenu(this@TootActivity, v)
                            popup.inflate(R.menu.attachment_thumbnail)
                            popup.setOnMenuItemClickListener { menu ->
                                when (menu.itemId) {
                                    R.id.preview -> {
                                        preview(uri)
                                    }
                                    R.id.delete -> {
                                        imageAttachmentParent.removeView(v)
                                        val removeindex = uriAttachmentsList.indexOfFirst { (u) -> u == uri }
                                        uriAttachmentsList.removeAt(removeindex)
                                        if (uriAttachmentsList.isEmpty())
                                            nsfwButton.visibility = View.GONE
                                    }
                                }
                                return@setOnMenuItemClickListener true
                            }
                            MenuPopupHelper(this@TootActivity, popup.menu as MenuBuilder, v).apply {
                                setForceShowIcon(true)
                                show()
                            }
                        }
                        nsfwButton.visibility = View.VISIBLE
                        return false
                    }
                    override fun onException(e: Exception?, model: Uri?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean = false
                }
        ).centerCrop().into(v)

        imageAttachmentParent.addView(v)
    }

    private fun uploadMedia(uri: Uri) {
        val type = contentResolver.getType(uri)
        val compressFormat = if (type.contains(Regex("png", RegexOption.IGNORE_CASE))) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog.setMessage(getString(R.string.uploading_media))
        dialog.show()
        Glide.with(applicationContext)
                .load(uri)
                .asBitmap()
                .toBytes(compressFormat, 85)
                .atMost()
                .override(2048, 2048)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(object : SimpleTarget<ByteArray>() {
                    override fun onResourceReady(resource: ByteArray, glideAnimation: GlideAnimation<in ByteArray>) {
                        val attachment = RequestBody.create(MediaType.parse("image/*"), resource)
                        val body = MultipartBody.Part.createFormData("file", "media_attachment", attachment)
                        MastodonUtil.api?.uploadMediaAttachment(body)?.enqueue(
                                object : Callback<Attachment> {
                                    override fun onResponse(call: Call<Attachment>?, response: Response<Attachment>?) {
                                        response?.body()?.let {
                                            uriAttachmentsList.add(Pair(uri, it))
                                            addThumbnail(uri)
                                            checkTextCount()
                                        }
                                        dialog.dismiss()
                                        updateAttachmentButtonState()
                                        updateTootButtonState()
                                    }

                                    override fun onFailure(call: Call<Attachment>?, t: Throwable?) {
                                        Toast.makeText(this@TootActivity, "falied", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                        updateAttachmentButtonState()
                                        updateTootButtonState()
                                    }
                                }
                        )
                    }
                })
    }

    fun toggleContentWarning() {
        isContentWarningEnabled = !isContentWarningEnabled
        setContentWarning(isContentWarningEnabled)
    }

    fun setContentWarning(isEnabled: Boolean) {
        if (isEnabled) {
            cwButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            spoilerText.visibility = View.VISIBLE
            textSeparator.visibility = View.VISIBLE
            spoilerText.requestFocus()
        } else {
            cwButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary_dark))
            spoilerText.text.clear()
            spoilerText.visibility = View.GONE
            textSeparator.visibility = View.GONE
        }
    }

    fun toggleNotSafeForWork() {
        isNsfw = !isNsfw
        setNotSafeForWorkEnabled(isNsfw)
    }

    fun setNotSafeForWorkEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            nsfwButton.setImageResource(R.drawable.ic_visibility_off)
            nsfwButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent))

        } else {
            nsfwButton.setImageResource(R.drawable.ic_visibility)
            nsfwButton.clearColorFilter()
        }
    }

    // コンテントプロバイダを使用し,ギャラリーに画像を保存.
    fun addToGallery(file: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATA, file)
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun updateTootButtonState() {
        val validText = tootText.text.isNotEmpty() && (wholeTextCount() <= MAX_TOOT_LENGTH)
        val hasMedia = uriAttachmentsList.isNotEmpty()
        tootButton.isEnabled = (validText || hasMedia) && !isPosting
    }

    private fun updateAttachmentButtonState() {
        attachmentButton.isEnabled = (uriAttachmentsList.size < MAX_ATTACHMENTS_NUM)
        val d = attachmentButton.drawable.mutate()
        if (attachmentButton.isEnabled) {
            d.clearColorFilter()
        } else {
            d.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun clearAttachments() {
        imageAttachmentParent.removeAllViews()
        uriAttachmentsList.clear()
        if (isNsfw) {
            toggleNotSafeForWork()
        }
        nsfwButton.visibility = View.GONE
    }


    fun onClickVisibility(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.toot_visibility)
        popup.setOnMenuItemClickListener { item ->
            val menuItemId = item.itemId
            when (menuItemId) {
                R.id.visibility_public -> {
                    tootVisibility = Status.Visibility.Public
                }
                R.id.visibility_unlisted -> {
                    tootVisibility = Status.Visibility.UnListed
                }
                R.id.visibility_followers -> {
                    tootVisibility = Status.Visibility.Private
                }
                R.id.visibility_direct -> {
                    tootVisibility = Status.Visibility.Direct
                }
            }
            updateVisibilityButtonState()
            return@setOnMenuItemClickListener true
        }
        MenuPopupHelper(this, popup.menu as MenuBuilder, v).apply {
            setForceShowIcon(true)
            show()
        }
        updateAttachmentButtonState()
    }

    private fun updateVisibilityButtonState() {
        when (tootVisibility) {
            Status.Visibility.Public -> {
                visibilityButton.setImageResource(R.drawable.ic_public)
                tootButton.text = getString(R.string.toot_public)
                tootButton.setCompoundDrawables(null, null, null, null)
            }
            Status.Visibility.UnListed -> {
                tootButton.text = getString(R.string.toot_unpublic)
                visibilityButton.setImageResource(R.drawable.ic_lock_open)
                tootButton.setCompoundDrawables(null, null, null, null)
            }
            Status.Visibility.Private -> {
                tootButton.text = getString(R.string.toot_unpublic)
                visibilityButton.setImageResource(R.drawable.ic_lock)

                tootButton.compoundDrawablePadding = 0
                val d = getDrawable(R.drawable.ic_lock_white).mutate()
                tootButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
            }
            Status.Visibility.Direct -> {
                tootButton.text = getString(R.string.toot_unpublic)
                visibilityButton.setImageResource(R.drawable.ic_private_message)

                tootButton.compoundDrawablePadding = 0
                val d = getDrawable(R.drawable.ic_lock_white).mutate()
                tootButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            checkTextCount()
            updateTootButtonState()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }

    private fun wholeTextCount(): Int =
            spoilerText.length() + tootText.length() + uriAttachmentsList.count() * URL_LENGTH

    private fun checkTextCount() {
        val total = wholeTextCount()
        textCount.text = (MAX_TOOT_LENGTH - wholeTextCount()).toString()
        if (total > MAX_TOOT_LENGTH) {
            textCount.setTextColor(ContextCompat.getColor(this, R.color.text_error))
        } else {
            textCount.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark))
        }

        if (total == 0) {
            replyToStatus = null
        }
    }

    private fun setUpReply() {
        val status = Gson().fromJson(intent.extras.getString(REPLY_STATUS), Status::class.java)
        tootVisibility = Status.Visibility.values()[Math.max(defaultVisibility.ordinal, status.getVisibility().ordinal)]
        if (status.spoilerText.isNotEmpty()) {
            if (!isContentWarningEnabled) {
                toggleContentWarning()
            }
            spoilerText.setText(status.spoilerText)
        }
        tootText.setText("@${status.account.acct} ")
        tootText.requestFocus()
        tootText.setSelection(tootText.length())
        checkTextCount()
        replyToStatus = status
    }

    private fun preview(uri: Uri) {
        val intent = Intent(this, AttachmentsPreviewActivity::class.java)
        val uris = ArrayList<Uri>(uriAttachmentsList.map { (u) -> u })
        AttachmentsPreviewActivity.setup(intent, uris, uris.indexOfFirst { e -> e == uri })
        startActivity(intent)
    }
}
