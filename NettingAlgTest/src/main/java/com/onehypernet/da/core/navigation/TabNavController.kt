package com.onehypernet.da.core.navigation

import com.onehypernet.da.core.LifecycleOwner
import javafx.scene.layout.AnchorPane

class TabNavController(owner: LifecycleOwner, container: AnchorPane) : NavController(owner, container) {

    private var mCurrentTab: Destination? = null
    private val mCache = hashMapOf<String, Destination>()

    override fun destroy() {
        for (value in mCache.values) {
            value.controller.destroy()
        }
        mCache.clear()
    }

    override fun navigateTo(layout: String, args: Any?): Boolean {
        val lastDest = mCurrentTab

        if (lastDest != null) {
            if (lastDest.layout == layout) return false
            lastDest.controller.pause()
            removeChild(lastDest.node)
        }

        val destination = if (mCache.containsKey(layout)) mCache[layout]!!
        else onCreateDestination(layout)

        addChild(destination.node)
        destination.resume(args)
        if (!mCache.containsKey(layout)) mCache[layout] = destination
        mCurrentTab = destination
        notifyNavigateChange(layout)
        return true
    }

    override fun popBackStack(popupTo: String, inclusive: Boolean): Boolean = false

    override fun popBackStack(): Boolean = false
}