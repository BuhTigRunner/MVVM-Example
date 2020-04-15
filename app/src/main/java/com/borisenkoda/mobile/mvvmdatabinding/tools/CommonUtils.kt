package com.borisenkoda.mobile.mvvmdatabinding.tools

import com.borisenkoda.mobile.mvvmdatabinding.BuildConfig


inline fun runIfDebug(function: () -> Unit) {
    if (BuildConfig.DEBUG) {
        function()
    }
}

inline fun runIfRelease(function: () -> Unit) {
    if (!BuildConfig.DEBUG) {
        function()
    }
}

