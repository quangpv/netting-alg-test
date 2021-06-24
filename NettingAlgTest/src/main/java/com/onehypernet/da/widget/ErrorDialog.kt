package com.onehypernet.da.widget

import javafx.scene.control.Alert

/**
 * https://code.makery.ch/blog/javafx-dialogs-official/
 */
class ErrorDialog : Alert(AlertType.ERROR) {
    init {
        this.title = "Error"
        this.headerText = null
    }

    fun show(message: String) {
        this.contentText = message
        super.showAndWait()
    }

    fun show(message: Throwable?) {
        this.contentText = message?.message ?: "Unknown"
        super.showAndWait()
    }
}