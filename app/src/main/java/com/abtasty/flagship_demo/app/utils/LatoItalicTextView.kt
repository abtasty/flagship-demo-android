package com.abtasty.flagship_demo.app.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView


class LatoItalicTextView : TextView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val bold = Typeface.createFromAsset(context.assets, "fonts/Lato-Italic.ttf")
        typeface = bold
    }
}