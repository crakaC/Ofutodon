package com.crakac.ofutodon.methods

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert
import org.junit.Test
import java.io.File

class Media: MastodonMethodTestBase(){
    /* /media */
    @Test
    fun uploadMedia(){
        val f = imageFile()
        Assert.assertNotNull(f)

        val reqFile = RequestBody.create(MediaType.parse("image/*"), f)
        val body = MultipartBody.Part.createFormData("file", f.name, reqFile)
        val r = api.uploadMediaAttachment(body).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    fun imageFile(): File{
        return File(javaClass.getResource("/icon.png").path)
    }

}