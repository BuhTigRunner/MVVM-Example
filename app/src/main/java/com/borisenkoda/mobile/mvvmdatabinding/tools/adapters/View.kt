package com.borisenkoda.mobile.mvvmdatabinding.tools.adapters

import android.view.View
import androidx.databinding.BindingAdapter
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.setInvisible
import com.borisenkoda.mobile.mvvmdatabinding.tools.extentions.setVisible

@BindingAdapter("isGone")
fun setIsGone(view: View, isGone: Boolean?) {
    view.setVisible(!(isGone ?: false))
}

@BindingAdapter("android:visibility")
fun setVisibility(view: View, visible: Boolean?) {
    view.setVisible(visible ?: false)
}

@BindingAdapter("invisible")
fun setInvisible(view: View, invisible: Boolean?) {
    view.setInvisible(invisible ?: false)
}

@BindingAdapter("selected")
fun setSelected(view: View, selected: Boolean?) {
    view.isSelected = selected ?: false
}

@BindingAdapter("requestFocus")
fun requestFocus(view: View, @Suppress("UNUSED_PARAMETER") requestFocus: Any?) {
    requestFocus?.let {
        view.requestFocus()
    }
}

@BindingAdapter("android:enabled")
fun setEnabled(view: View, enabled: Boolean?) {
    enabled?.let {
        view.isEnabled = it
        view.isClickable = it
        return
    }
    view.isEnabled = false

}