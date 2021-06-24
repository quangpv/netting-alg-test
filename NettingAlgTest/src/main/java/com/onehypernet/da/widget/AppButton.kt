package com.onehypernet.da.views.widget

import com.onehypernet.da.app.R
import com.onehypernet.da.app.Style
import javafx.scene.control.Button

class AppButton(private val view: Button) {
    private var mActivated: Boolean = false
    var isActivated: Boolean
        set(value) {
            mActivated = value
            Style.setTextColor(view, if (value) R.color.primary else R.color.black)
            if (value) Style.addClass(view, R.theme.boxBorderOpacity)
            else Style.removeClass(view, R.theme.boxBorderOpacity)
        }
        get() = mActivated
}