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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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

    @BindView(R.id.container)
    lateinit var container: View

    @BindView(R.id.spoiler_text)
    lateinit var spoilerText: EditText

    @BindView(R.id.text_separator)
    lateinit var textSeparator: View

    @BindView(R.id.toot_text)
    lateinit var tootText: EditText

    @BindView(R.id.image_attachments_root)
    lateinit var imageAttachmentParent: LinearLayout

    @BindView(R.id.toot)
    lateinit var tootButton: TextView

    @BindView(R.id.add_photo)
    lateinit var attachmentButton: ImageView

    @BindView(R.id.toot_visibility)
    lateinit var visibilityButton: ImageView

    @BindView(R.id.content_warning)
    lateinit var cwButton: TextView

    @BindView(R.id.nsfw)
    lateinit var nsfwButton: TextView

    @BindView(R.id.text_count)
    lateinit var textCount: TextView

    var isPosting = false

    // using for picking up attachmentUris by other app
    var cameraUri: Uri? = null
    var cameraFilePath: String? = null

    // keep attachmentUris
    var uriAttachmentsMap = HashMap<Uri, Attachment?>()

    var tootVisibility = Status.Visibility.valueOf(
            PrefsUtil.getString(TOOT_VISIBILITY, Status.Visibility.Public.toString())!!
    )

    var isContentWarningEnabled = false

    fun getSpoilerText(): String? {
        if (!isContentWarningEnabled) {
            return null
        }
        return spoilerText.text.toString()
    }

    var isNotSafeForWork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)
        ButterKnife.bind(this)
        FabTransform.setup(this, container)

        cameraUri = savedInstanceState?.getParcelable("CameraUri")
        cameraFilePath = savedInstanceState?.getString("CameraFilePath")
        (savedInstanceState?.getSerializable("Attachments") as HashMap<Uri, Attachment?>?)?.let {
            uriAttachmentsMap = it
            it.forEach { e -> addThumbnail(e.key) }
        }

        tootText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                updateTootButtonState()
                p0?.let {
                    checkTextCount(it)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        updateTootButtonState()
        updateVisibilityButtonState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("CameraUri", cameraUri)
        outState.putString("CameraFilePath", cameraFilePath)
        outState.putSerializable("Attachments", uriAttachmentsMap)
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ATTACHMENT) {
            handleAttachment(resultCode, data)
        }
    }

    @OnClick(R.id.toot_background)
    fun dismiss() {
        finishAfterTransition()
    }

    @OnClick(R.id.toot)
    fun toot() {
        if (isPosting) return
        isPosting = true
        tootButton.isEnabled = false

        MastodonUtil.api?.postStatus(
                StatusBuilder(
                        visibility = tootVisibility.value,
                        spoilerText = getSpoilerText(),
                        mediaIds = uriAttachmentsMap.filter { e -> e.value != null }.map { e -> e.value!!.id },
                        isSensitive = isNotSafeForWork,
                        text = tootText.text.toString()
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
        }
    }

    @OnClick(R.id.add_photo)
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
            addThumbnail(data.data)
            uploadMedia(data.data)
        } else if (cameraUri != null) {
            addToGallery(cameraFilePath!!)
            addThumbnail(cameraUri!!)
            uploadMedia(cameraUri!!)
            cameraUri = null
            cameraFilePath = null
        }

        nsfwButton.visibility = View.VISIBLE
    }

    fun addThumbnail(uri: Uri) {
        updateAttachmentButtonState()
        updateTootButtonState()

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
                            popup.setOnMenuItemClickListener { _ ->
                                imageAttachmentParent.removeView(v)
                                uriAttachmentsMap.remove(uri)
                                if(uriAttachmentsMap.isEmpty())
                                    nsfwButton.visibility = View.GONE
                                return@setOnMenuItemClickListener true
                            }
                            MenuPopupHelper(this@TootActivity, popup.menu as MenuBuilder, v).apply {
                                setForceShowIcon(true)
                                show()
                            }
                            updateAttachmentButtonState()
                        }
                        return false
                    }

                    override fun onException(e: Exception?, model: Uri?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }
                }
        ).centerCrop().into(v)

        imageAttachmentParent.addView(v)
    }

    private fun uploadMedia(uri: Uri){
        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog.setMessage(getString(R.string.uploading_media))
        dialog.show()
        Glide.with(applicationContext)
                .load(uri)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, 85)
                .atMost()
                .override(2048, 2048)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(object : SimpleTarget<ByteArray>() {
                    override fun onResourceReady(resource: ByteArray?, glideAnimation: GlideAnimation<in ByteArray>?) {
                        val attachment = RequestBody.create(MediaType.parse("image/*"), resource)
                        val body = MultipartBody.Part.createFormData("file", "media_attachment", attachment)
                        MastodonUtil.api?.uploadMediaAttachment(body)?.enqueue(
                                object : Callback<Attachment> {
                                    override fun onResponse(call: Call<Attachment>?, response: Response<Attachment>?) {
                                        response?.let {
                                            uriAttachmentsMap.put(uri, it.body())
                                        }
                                        dialog.dismiss()
                                    }

                                    override fun onFailure(call: Call<Attachment>?, t: Throwable?) {
                                        Toast.makeText(this@TootActivity, "falied", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                }
                        )
                    }
                })
    }

    @OnClick(R.id.content_warning)
    fun toggleContentWarning() {
        isContentWarningEnabled = !isContentWarningEnabled
        if (isContentWarningEnabled) {
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

    @OnClick(R.id.nsfw)
    fun toggleNotSafeForWark() {
        isNotSafeForWork = !isNotSafeForWork
        if (isNotSafeForWork) {
            nsfwButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        } else {
            nsfwButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary_dark))
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
        val hasText = tootText.text.isNotEmpty() && (tootText.text.length <= MAX_TOOT_LENGTH)
        val hasMedia = uriAttachmentsMap.isNotEmpty()
        tootButton.isEnabled = (hasText || hasMedia) && !isPosting
    }

    private fun updateAttachmentButtonState() {
        attachmentButton.isEnabled = (uriAttachmentsMap.size < MAX_ATTACHMENTS_NUM)
        val d = attachmentButton.drawable.mutate()
        if (attachmentButton.isEnabled) {
            d.clearColorFilter()
        } else {
            d.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun clearAttachments() {
        imageAttachmentParent.removeAllViews()
        uriAttachmentsMap.clear()
        if (isNotSafeForWork) {
            toggleNotSafeForWark()
        }
        nsfwButton.visibility = View.GONE
    }


    @OnClick(R.id.toot_visibility)
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

    private fun checkTextCount(text: CharSequence){
        textCount.text = (MAX_TOOT_LENGTH - text.length).toString()
        if(text.length > MAX_TOOT_LENGTH){
            textCount.setTextColor(ContextCompat.getColor(this, R.color.text_error))
        } else {
            textCount.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark))
        }
    }
}
