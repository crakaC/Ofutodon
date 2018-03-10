package com.crakac.ofutodon.ui

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.service.TootService
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.util.*
import com.google.gson.Gson
import java.io.File
import java.lang.Exception


class TootActivity : AppCompatActivity() {
    val PERMISSION_REQUEST = 1234
    val REQUEST_ATTACHMENT = 1235

    val LONG_EDGE = 2048 // 画像の最大サイズは2048x2048

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

    // using for picking up attachmentUris by other app
    private var cameraUri: Uri? = null

    // keep attachmentUris
    private var attachmentUris = ArrayList<Uri>(MAX_ATTACHMENTS_NUM)
    private var attachmentFiles = ArrayList<File>(MAX_ATTACHMENTS_NUM)

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
        cwButton = findViewById(R.id.spoiler_text)
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
        outState.putParcelableArrayList("AttachmentUris", attachmentUris)
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
        attachmentUris = savedInstanceState.getParcelableArrayList("AttachmentUris")
        attachmentUris.forEach { e -> addThumbnail(e) }

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
        var intent = Intent(this, TootService::class.java)
        intent.putExtra(C.TEXT, tootText.text.toString())
        intent.putExtra(C.SPOILER_TEXT, getSpoilerText())
        intent.putExtra(C.VISIBILITY, tootVisibility.value)
        intent.putExtra(C.IS_SENSITIVE, isNsfw)
        replyToStatus?.let {
            intent.putExtra(C.REPLY_TO_ID, it.id)
        }
        intent.putExtra(C.ATTACHMENTS, attachmentFiles)
        startService(intent)

        clearAttachments()
        if (isContentWarningEnabled) {
            toggleContentWarning()
        }
        tootText.text.clear()
        replyToStatus = null
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
        cameraUri = FileUtil.createTemporaryImageUri(this)

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
            attachmentUris.add(data.data)
            addThumbnail(data.data)
        } else if (cameraUri != null) {
            attachmentUris.add(cameraUri!!)
            addThumbnail(cameraUri!!)
            cameraUri = null
        }
    }

    fun addThumbnail(uri: Uri) {
        val v = ImageView(this)
        val edge = resources.getDimension(R.dimen.image_attachment).toInt()
        val p = LinearLayout.LayoutParams(edge, edge)
        v.layoutParams = p

        CreateTempImageFileTask(contentResolver, uri, LONG_EDGE, { f ->
            addTempFile(f)
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
                                            val pos = attachmentUris.indexOf(uri)
                                            attachmentUris.removeAt(pos)
                                            attachmentFiles.removeAt(pos)
                                            imageAttachmentParent.removeView(v)
                                            if (attachmentUris.isEmpty())
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
            checkTextCount()
            updateAttachmentButtonState()
            updateTootButtonState()
        }).execute()
    }

    fun addTempFile(f: File) {
        attachmentFiles.add(f)
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

    private fun updateTootButtonState() {
        val validText = tootText.text.isNotEmpty() && (wholeTextCount() <= MAX_TOOT_LENGTH)
        val hasMedia = attachmentUris.isNotEmpty()
        tootButton.isEnabled = (validText || hasMedia)
    }

    private fun updateAttachmentButtonState() {
        attachmentButton.isEnabled = (attachmentUris.size < MAX_ATTACHMENTS_NUM)
        val d = attachmentButton.drawable.mutate()
        if (attachmentButton.isEnabled) {
            d.clearColorFilter()
        } else {
            d.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun clearAttachments() {
        imageAttachmentParent.removeAllViews()
        attachmentUris.clear()
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
            spoilerText.length() + tootText.length() + attachmentUris.count() * URL_LENGTH

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
        AttachmentsPreviewActivity.setup(intent, attachmentUris, attachmentUris.indexOf(uri))
        startActivity(intent)
    }

    class CreateTempImageFileTask(val cr: ContentResolver, val uri: Uri, val longEdge: Int, val callback: (File) -> Unit) : AsyncTask<Void, Void, File>() {
        override fun doInBackground(vararg params: Void): File {
            return BitmapUtil.createResizedTempImageFile(cr, uri, longEdge)
        }

        override fun onPostExecute(result: File) {
            callback(result)
        }
    }
}
