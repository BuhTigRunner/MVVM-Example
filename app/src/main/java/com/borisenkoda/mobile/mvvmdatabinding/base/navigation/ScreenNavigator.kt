package com.borisenkoda.mobile.mvvmdatabinding.base.navigation

import androidx.appcompat.app.AlertDialog
import com.borisenkoda.mobile.mvvmdatabinding.R
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.Failure
import com.borisenkoda.mobile.mvvmdatabinding.base.failure.FailureInterpreter

interface ScreenNavigator {
    fun openAlertDialog(failure: Failure)
    fun openSuccessDialog(okCallBack: () -> Unit)
}

class ScreenNavigatorImpl (
    private val foregroundActivityProvider: ForegroundActivityProvider,
    private val failureInterpreter: FailureInterpreter
) : ScreenNavigator {

    private val functionsCollector: FunctionsCollector by lazy {
        FunctionsCollector(foregroundActivityProvider.onPauseStateLiveData)
    }

    private fun runOrPostpone(function: () -> Unit) {
        functionsCollector.executeFunction(function)
    }


    override fun openAlertDialog(failure: Failure) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(failureInterpreter.getFailureDescription(failure).message)
                    .show()
            }
        }

    }

    override fun openSuccessDialog(okCallBack: () -> Unit) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.apply {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.success))
                    .setMessage(getString(R.string.auth_success_message))
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ -> okCallBack() }
                    .show()
            }
        }

    }
}


