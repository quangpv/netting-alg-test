package com.onehypernet.da.core.navigation

import com.onehypernet.da.core.Controller
import com.onehypernet.da.core.LifecycleObserver
import com.onehypernet.da.core.LifecycleOwner
import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.AnchorPane

abstract class NavController(private val owner: LifecycleOwner, private val container: AnchorPane) {
    private val mNavigateListeners = ArrayList<(String) -> Unit>()

    private val children = container.javaClass.getMethod("getChildren").run {
        isAccessible = true
        @Suppress("unchecked_cast")
        val value = invoke(container) as ObservableList<Node>
        isAccessible = false
        value
    }

    init {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            override fun onDestroy() {
                destroy()
                mNavigateListeners.clear()
            }
        })
    }

    protected fun addChild(node: Node) {
        AnchorPane.setTopAnchor(node, 0.0)
        AnchorPane.setLeftAnchor(node, 0.0)
        AnchorPane.setRightAnchor(node, 0.0)
        AnchorPane.setBottomAnchor(node, 0.0)
        children.add(node)
    }

    protected fun removeChild(node: Node) {
        children.remove(node)
    }

    protected fun notifyNavigateChange(layout: String) {
        for (mNavigateListener in mNavigateListeners) {
            mNavigateListener(layout)
        }
    }

    protected open fun destroy() {
    }

    abstract fun navigateTo(layout: String, args: Any? = null): Boolean

    abstract fun popBackStack(popupTo: String, inclusive: Boolean = false): Boolean

    abstract fun popBackStack(): Boolean

    protected fun onCreateDestination(layout: String): Destination {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layout/$layout.fxml"))
        val node = fxmlLoader.load<Node>()
        val controller = fxmlLoader.getController<Any>()
        if (controller !is Controller) error("${controller.javaClass.name} should extends from Controller")
        controller.create(node, owner as? Controller)
        return Destination(
            node = node,
            layout = layout,
            controller = controller
        )
    }

    fun addOnNavigateChangeListener(function: (String) -> Unit) {
        if (!mNavigateListeners.contains(function))
            mNavigateListeners.add(function)
    }

    class Destination(
        val layout: String,
        val node: Node,
        val controller: Controller
    ) {
        fun resume(args: Any? = null) {
            if (args != null && controller is ArgumentChangeable) {
                controller.onNewArguments(args)
            }
            controller.resume()
        }
    }
}