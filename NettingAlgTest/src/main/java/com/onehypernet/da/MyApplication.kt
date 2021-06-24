package com.onehypernet.da

import com.onehypernet.da.core.BaseApplication
import com.onehypernet.da.core.dependencies
import javafx.scene.image.Image
import javafx.stage.Stage

class MyApplication : BaseApplication("main") {
    override fun start(primaryStage: Stage) {
        dependencies { }
        primaryStage.icons.addAll(Image(javaClass.getResource("/ic_launcher.png").openStream()))
//        primaryStage.icons.addAll(icon16, icon32, icon64, icon128)
        super.start(primaryStage)
    }

    override fun onStart(stage: Stage) {
        super.onStart(stage)
        stage.title = "OneHypernet Multilateral Netting Test"
    }
}