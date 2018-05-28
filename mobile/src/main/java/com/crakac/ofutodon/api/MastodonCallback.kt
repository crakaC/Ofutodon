package com.crakac.ofutodon.api

import android.util.Log
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Kosuke on 2017/12/04.
 */
interface MastodonCallback<T>: retrofit2.Callback<T> {
    companion object {
        val TAG ="MastodonCallback"
    }
    fun onNetworkAccessError(call: Call<T>?, t: Throwable?){
        Log.d(TAG, t?.toString())
    }

    fun onSuccess(result: T)

    override fun onFailure(call: Call<T>?, t: Throwable?){
        onNetworkAccessError(call, t)
    }

    fun onErrorResponse(call: Call<T>, response: Response<T>?){
        Log.d(TAG, "$call is failed")
    }

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        if (response != null && response.isSuccessful) {
            onSuccess(response.body()!!)
        } else {
            onErrorResponse(call, response)
        }
    }
}