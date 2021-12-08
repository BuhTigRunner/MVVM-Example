package com.borisenkoda.mobile.mvvmdatabinding.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.Failure
import com.borisenkoda.mobile.mvvmdatabinding.tools.Logg
import com.borisenkoda.mobile.mvvmdatabinding.base.functional.Either
import com.borisenkoda.mobile.mvvmdatabinding.models.AuthState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.random.Random

interface User {
    fun authState(): SharedFlow<AuthState>
    suspend fun login(login: String, password: String): Either<Failure, Nothing?>
}

class UserTestImpl : User {

    private val _authState = MutableStateFlow(NOT_AUTHORIZED)


    override fun authState(): SharedFlow<AuthState> {
        return _authState
    }

    override suspend fun login(login: String, password: String): Either<Failure, Nothing?> {
        _authState.emit(IN_PROCESS)
        @Suppress("BlockingMethodInNonBlockingContext")
        Thread.sleep(3000)

        Logg.d { "current thread: ${Thread.currentThread()}" }

        return Random.nextInt(until = 3).let { random ->
            when (random) {
                0 -> {
                    _authState.emit(AUTHORIZED)
                    Either.Right(null)
                }
                1 -> {
                    _authState.emit(NOT_AUTHORIZED)
                    Either.Left(Failure.ServerError)
                }
                2 -> {
                    _authState.emit(NOT_AUTHORIZED)
                    Either.Left(Failure.NetworkConnection)
                }
                else -> {
                    _authState.emit(NOT_AUTHORIZED)
                    Either.Left(Failure.AuthError)
                }
            }

        }
    }

    companion object {
        val instance by lazy {
            UserTestImpl()
        }
    }

}


enum class AuthState {
    AUTHORIZED, NOT_AUTHORIZED, IN_PROCESS
}