package com.crakac.ofutodon.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.media.ExifInterface
import android.webkit.MimeTypeMap
import java.io.*

class BitmapUtil private constructor() {
    companion object {
        fun createResizedTempImageFile(cr: ContentResolver, uri: Uri, longEdge: Int): File {
            var bm: Bitmap = getSampledBitmap(cr, uri, longEdge)
                    ?: throw RuntimeException("something wrong")
            val scaleW = longEdge.toFloat() / bm.width
            val scaleH = longEdge.toFloat() / bm.height
            val scale = Math.min(scaleH, scaleW)

            if (scale < 1.0) {
                val w = (bm.width * scale + 0.5).toInt()
                val h = (bm.height * scale + 0.5).toInt()
                val old = bm
                try {
                    bm = Bitmap.createScaledBitmap(old, w, h, true)
                    old.recycle()
                } catch (oome: OutOfMemoryError) {
                    oome.printStackTrace()
                }
            }
            val format = getCompressFormatFromUri(cr, uri)
            val bytes = ByteArrayOutputStream()
            bm.compress(format, 90, bytes)
            val tmpFile = File.createTempFile("TMP", "img")
            val os: OutputStream
            try {
                os = BufferedOutputStream(FileOutputStream(tmpFile))
                os.write(bytes.toByteArray())
                os.flush()
                os.close()
                if (format == Bitmap.CompressFormat.JPEG) {
                    val src = ExifInterface(cr.openInputStream(uri))
                    val dst = ExifInterface(tmpFile.toString())
                    val orientation = src.getAttribute(ExifInterface.TAG_ORIENTATION)
                    dst.setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
                    dst.saveAttributes()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return tmpFile
        }

        fun getSampledBitmap(cr: ContentResolver, uri: Uri, longEdge: Int): Bitmap? {
            var stream: InputStream? = null
            try {
                stream = BufferedInputStream(cr.openInputStream(uri))

                val opt = BitmapFactory.Options()
                opt.inJustDecodeBounds = true
                BitmapFactory.decodeStream(stream, null, opt)
                val scaleH = opt.outHeight / longEdge
                val scaleW = opt.outWidth / longEdge
                opt.inSampleSize = Math.min(scaleH, scaleW)
                opt.inJustDecodeBounds = false
                stream.close()
                stream = BufferedInputStream(cr.openInputStream(uri))
                return BitmapFactory.decodeStream(stream, null, opt)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                stream?.close()
            }
            return null
        }

        fun getCompressFormatFromUri(cr: ContentResolver, uri: Uri): Bitmap.CompressFormat {
            var type = cr.getType(uri)
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
}