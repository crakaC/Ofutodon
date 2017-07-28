package com.crakac.ofutodon.ui

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.util.TextUtil
import com.crakac.ofutodon.util.ViewUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception


class TootActivity : AppCompatActivity() {
    val PERMISSION_REQUEST = 1234
    val REQUEST_ATTACHMENT = 1235
    val TAG = "TootActivity"

    val IMAGE_ATTACHMENT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator

    @BindView(R.id.container)
    lateinit var container: View

    @BindView(R.id.toot_text)
    lateinit var tootText: EditText

    @BindView(R.id.image_attachments_root)
    lateinit var imageAttachmentParent: LinearLayout

    var isPosting = false

    var cameraUri: Uri? = null
    var cameraFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)
        ButterKnife.bind(this)
        FabTransform.setup(this, container)
        cameraUri = savedInstanceState?.getParcelable("CameraUri")
        cameraFilePath = savedInstanceState?.getString("CameraFilePath")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("CameraUri", cameraUri)
        outState.putString("CameraFilePath", cameraFilePath)
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
    fun toot(v: View) {
        if (isPosting) return
        isPosting = true
        MastodonUtil.api?.postStatus(StatusBuilder(text = tootText.text.toString()))?.enqueue(
                object : Callback<Status> {
                    override fun onFailure(call: Call<Status>?, t: Throwable?) {
                        isPosting = false
                        Log.d(TAG, "Failed")
                    }

                    override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                        isPosting = false
                        if (response == null || !response.isSuccessful) {
                            Log.d(TAG, "Failed")
                            return
                        }
                        Log.d(TAG, "Success:" + response.body()?.content)
                        tootText.setText("")
                    }
                }
        )
    }

    @OnClick(R.id.add_photo)
    fun addPhoto() {
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

        if (data == null) {
            Log.d(TAG, "data is null")
            return
        }

        if (data.data != null) {
            addThumbnail(data.data)
        } else if (cameraUri != null) {
            addThumbnail(cameraUri!!)
            addToGallery(cameraFilePath!!)
            cameraUri = null
            cameraFilePath = null
        }
    }

    fun addThumbnail(uri: Uri) {
        val v = ImageView(this)
        val edge = resources.getDimension(R.dimen.image_attachment).toInt()
        val p = LinearLayout.LayoutParams(edge, edge)
        v.layoutParams = p

        Glide.with(this).loadFromMediaStore(uri).override(edge, edge).listener(
            object : RequestListener<Uri, GlideDrawable> {
                override fun onResourceReady(resource: GlideDrawable?, model: Uri?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val drawable = resource!!.current as GlideBitmapDrawable
                        Palette.from(drawable.bitmap).generate { palette ->
                            v.foreground = ViewUtil.createRipple(palette, 0.25f, 0.5f, getColor(R.color.mid_grey), true)
                            v.setOnClickListener { _ ->
                                Log.d("Attachment", "Attachment Clicked")
                            }
                        }
                    }
                    return false
                }

                override fun onException(e: Exception?, model: Uri?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }
            }
        ).centerCrop().crossFade().into(v)
        imageAttachmentParent.addView(v)
    }

    @OnClick(R.id.toot_visibility)
    fun toggleTootVisibility() {

    }

    @OnClick(R.id.content_warning)
    fun toggleContentWarning() {

    }

    @OnClick(R.id.nsfw)
    fun toggleNotSafeForWark() {

    }

    // コンテントプロバイダを使用し,ギャラリーに画像を保存.
    fun addToGallery(file: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATA, file)
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}
