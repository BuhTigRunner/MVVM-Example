package com.borisenkoda.mobile.mvvmdatabinding.base.failure

import android.content.Context
import com.borisenkoda.mobile.mvvmdatabinding.R

class FailureInterpreterImpl (
    private val context: Context
) : FailureInterpreter {
    override fun getFailureDescription(failure: Failure): FailureDescription {
        return when (failure) {
            Failure.ServerError -> FailureDescription(message = context.getString(R.string.error_server))
            Failure.AuthError -> FailureDescription(message = context.getString(R.string.auth_error))
            Failure.NetworkConnection -> FailureDescription(message = context.getString(R.string.error_network_connection))
            Failure.FileReadingError -> FailureDescription(message = context.getString(R.string.file_reading_error))
            is Failure.DbError -> FailureDescription(message = context.getString(R.string.db_error))
            else -> FailureDescription(
                isUnknown = true,
                message = context.getString(R.string.unknown_error)
            )

        }

    }
}


interface FailureInterpreter {
    fun getFailureDescription(failure: Failure): FailureDescription
}

data class FailureDescription(
    val isUnknown: Boolean = false,
    val message: String
)