package com.onehypernet.da.core.navigation

import com.onehypernet.da.core.LifecycleOwner
import javafx.scene.layout.AnchorPane
import java.util.*

class StackNavController(owner: LifecycleOwner, container: AnchorPane) : NavController(owner, container) {
    private val mStack = Stack<Destination>()

    override fun destroy() {
        while (mStack.isNotEmpty()) {
            val dest = mStack.pop()
            dest.controller.destroy()
        }
    }

    override fun navigateTo(layout: String, args: Any?): Boolean {
        if (mStack.isNotEmpty()) {
            val lastDest = mStack.lastElement()
            if (lastDest.layout == layout) return false
            lastDest.controller.pause()
            removeChild(lastDest.node)
        }

        val destination = onCreateDestination(layout)

        addChild(destination.node)
        destination.resume(args)
        mStack.add(destination)
        notifyNavigateChange(layout)
        return true
    }

    override fun popBackStack(popupTo: String, inclusive: Boolean): Boolean {
        if (mStack.isEmpty()) return false
        var popped = false
        val removes = arrayListOf<Destination>()

        while (mStack.isNotEmpty()) {
            val lastDest = mStack.lastElement()
            val shouldBreak = lastDest.layout == popupTo
            if (shouldBreak && !inclusive) break

            popped = true
            mStack.pop()
            removes.add(lastDest)
            if (shouldBreak) break
        }

        if (mStack.isNotEmpty()) {
            val current = mStack.lastElement()
            current.resume()
            addChild(current.node)
        }

        for (remove in removes) {
            remove.controller.destroy()
            removeChild(remove.node)
        }

        if (!mStack.isEmpty()) {
            notifyNavigateChange(mStack.lastElement().layout)
        }
        return popped
    }

    override fun popBackStack(): Boolean {
        if (mStack.isEmpty()) return false
        val lastDest = mStack.pop()

        if (mStack.isNotEmpty()) {
            val current = mStack.lastElement()
            current.resume()
            addChild(current.node)
        }

        lastDest.controller.destroy()
        removeChild(lastDest.node)

        if (!mStack.isEmpty()) notifyNavigateChange(mStack.lastElement().layout)
        return true
    }
}