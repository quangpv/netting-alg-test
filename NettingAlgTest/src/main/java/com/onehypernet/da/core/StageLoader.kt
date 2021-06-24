package com.onehypernet.da.core

import com.onehypernet.da.core.navigation.ArgumentChangeable
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class StageLoader(private val layout: String) {
    private var mListener: OnStartListener? = null
    private lateinit var mController: Controller

    fun start(stage: Stage = Stage(), args: Any? = null, listener: OnStartListener? = null) {
        mListener = listener
        val loader = FXMLLoader(javaClass.getResource("/layout/$layout.fxml"))
        val parent = loader.load<Parent>()

        mController = loader.getController()
        mController.create(parent)

        if (args != null) (mController as? ArgumentChangeable)?.onNewArguments(args)

        listener?.onStart(stage)
        stage.scene = Scene(parent)
        stage.show()
        stage.setOnCloseRequest {
            stop()
        }
        mController.resume()
    }

    fun stop() {
        mController.pause()
        mController.destroy()
        mListener?.onStop()
    }

    interface OnStartListener {
        fun onStart(stage: Stage) {}
        fun onStop() {}
    }
}