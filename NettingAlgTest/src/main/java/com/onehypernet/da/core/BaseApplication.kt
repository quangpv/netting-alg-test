package com.onehypernet.da.core

import javafx.application.Application
import javafx.stage.Stage

abstract class BaseApplication(private val layout: String) : Application(), StageLoader.OnStartListener {
    private val loader = StageLoader(layout)

    override fun start(primaryStage: Stage) {
        loader.start(primaryStage, this)
    }

    override fun onStart(stage: Stage) {}

    override fun stop() {
//        loader.stop()
    }
}