package com.onehypernet.da.core

import javafx.application.Platform

abstract class LiveData<T : Any> {
    companion object {
        private const val INITIAL = -1
    }

    private val mObservers = LinkList<ObserverWrapper>()
    private var mData: Any? = null
    private var mVersion = INITIAL

    @Suppress("unchecked_cast")
    val value
        get() = mData as? T

    protected open fun post(data: T?) {
        synchronized(this) {
            mData = data
            mVersion++
        }
        if (Platform.isFxApplicationThread()) notifyChange()
        else Platform.runLater { notifyChange() }
    }

    private fun notifyChange() {
        for (mObserver in mObservers) {
            mObserver.notifyChange()
        }
    }

    fun observe(owner: LifecycleOwner, function: Observer<T>) {
        mObservers.add(ObserverWrapper(owner, function))
    }

    inner class ObserverWrapper(
        private val owner: LifecycleOwner,
        private val callback: Observer<*>
    ) : LifecycleObserver {

        private var mNotifyVersion = INITIAL

        private val shouldNotify get() = mNotifyVersion != mVersion && mVersion != INITIAL

        init {
            owner.lifecycle.addObserver(this)
        }

        override fun onResume() {
            super.onResume()
            if (shouldNotify) performNotify()
        }

        override fun onDestroy() {
            super.onDestroy()
            owner.lifecycle.removeObserver(this)
            mObservers.remove(this)
        }

        fun notifyChange() {
            if (owner.lifecycle.currentEvent == LifecycleRegistry.RESUME) {
                performNotify()
                return
            }
        }

        @Suppress("unchecked_cast")
        private fun performNotify() {
            (callback as Observer<Any>)(mData)
            mNotifyVersion = mVersion
        }
    }
}

typealias Observer<T> = (T?) -> Unit

class MutableLiveData<T : Any> : LiveData<T>() {
    public override fun post(data: T?) {
        super.post(data)
    }

    fun call() {
        post(null)
    }
}