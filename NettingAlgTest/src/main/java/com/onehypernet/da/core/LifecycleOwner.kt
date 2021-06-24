package com.onehypernet.da.core

interface LifecycleOwner {
    val lifecycle: Lifecycle
}

interface Lifecycle {
    val currentEvent: Int

    fun addObserver(observer: LifecycleObserver)
    fun removeObserver(observer: LifecycleObserver)
}

class LifecycleRegistry : Lifecycle {
    companion object {
        const val INITIALIZE = -1
        const val CREATE = 0
        const val RESUME = 1
        const val PAUSE = 2
        const val DESTROY = 3
    }

    private var mCurrentEvent: Int = INITIALIZE

    private val mObservers = LinkList<LifecycleObserver>()

    override val currentEvent get() = mCurrentEvent

    override fun addObserver(observer: LifecycleObserver) {
        if (mObservers.contains(observer)) return
        mObservers.add(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        mObservers.remove(observer)
    }

    fun dispatch(event: Int) {
        if (mCurrentEvent == event) return

        mCurrentEvent = event
        for (mObserver in mObservers) {
            when (event) {
                CREATE -> mObserver.onCreate()
                RESUME -> mObserver.onResume()
                PAUSE -> mObserver.onPause()
                DESTROY -> mObserver.onDestroy()
            }
        }
    }
}

interface LifecycleObserver {
    fun onCreate() {}
    fun onResume() {}
    fun onPause() {}
    fun onDestroy() {}
}