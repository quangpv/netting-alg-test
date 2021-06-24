package com.onehypernet.da.core

import com.onehypernet.da.core.viewmodel.ViewModelStore
import javafx.scene.Node

abstract class Controller : LifecycleOwner {
    private var mParent: Controller? = null
    private val mLifecycle = LifecycleRegistry()

    val viewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle = mLifecycle

    val parent get() = mParent ?: error("Not found parent controller of ${this.javaClass.name}")
    private val currentState get() = mLifecycle.currentEvent

    protected open fun onCreate(node: Node) {}
    protected open fun onResume() {}
    protected open fun onPause() {}
    protected open fun onDestroy() {}

    private fun performPause() {
        mLifecycle.dispatch(LifecycleRegistry.PAUSE)
        onPause()
    }

    private fun performResume() {
        onResume()
        mLifecycle.dispatch(LifecycleRegistry.RESUME)
    }

    private fun performDestroy() {
        mLifecycle.dispatch(LifecycleRegistry.DESTROY)
        onDestroy()
    }

    fun pause() {
        if (currentState == LifecycleRegistry.RESUME) {
            performPause()
        } else if (currentState == LifecycleRegistry.CREATE) {
            performResume()
            performPause()
        }
    }

    fun resume() {
        if (currentState == LifecycleRegistry.CREATE
            || currentState == LifecycleRegistry.PAUSE
        ) {
            performResume()
        } else {
            error("Can not resume controller from state $currentState")
        }
    }

    fun create(node: Node, parent: Controller? = null) {

        if (currentState == LifecycleRegistry.INITIALIZE) {
            mParent = parent
            node.properties["javafx:controller"] = this

            onCreate(node)
            mLifecycle.dispatch(LifecycleRegistry.CREATE)
        }
    }

    fun destroy() {
        if (currentState == LifecycleRegistry.CREATE
            || currentState == LifecycleRegistry.PAUSE
        ) {
            performDestroy()
        } else if (currentState == LifecycleRegistry.RESUME) {
            performPause()
            performDestroy()
        }
    }
}