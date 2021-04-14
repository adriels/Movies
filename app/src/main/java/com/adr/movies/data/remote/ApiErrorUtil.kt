package com.adr.movies.data.remote

import android.content.Context
import com.adr.movies.R
import com.adr.movies.data.entity.DefaultResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorUtil {

    fun getErrorMessage(t: Throwable, context: Context?): String {
        if (context == null) return ""
        val apiError = getError(t)
        return when {
            apiError == null -> context.getString(R.string.api_error)
            apiError.status_code == ErrorCodes.NO_INTERNET -> context.getString(R.string.no_internet)
            apiError.status_code == ErrorCodes.TIME_OUT -> context.getString(R.string.timeout)
            apiError.status_message.isNotBlank() -> apiError.status_message
            else -> context.getString(R.string.api_error)
        }
    }

    private fun getError(t: Throwable): DefaultResponse? {
        var apiError: DefaultResponse? = null
        when (t) {
            is SocketTimeoutException -> apiError =
                    DefaultResponse(
                            ErrorCodes.TIME_OUT,
                            ""
                    )
            is UnknownHostException -> apiError =
                    DefaultResponse(
                            ErrorCodes.NO_INTERNET,
                            ""
                    )
            is HttpException ->
                try {
                    apiError = Gson().fromJson(t.response()?.errorBody()?.string() ?: "",
                            DefaultResponse::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
        }
        return apiError
    }

    object ErrorCodes {
        const val TIME_OUT = 90
        const val NO_INTERNET = 91
    }
}