package com.kanggo.bubbleshowcase

import androidx.core.text.HtmlCompat

object Extension {

    fun String.convertHtml() = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)

}