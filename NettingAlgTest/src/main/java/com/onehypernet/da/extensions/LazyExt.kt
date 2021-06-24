package com.onehypernet.da.extensions

class DoubleCheckLazy<T>(private val factory: () -> T) : Lazy<T> {
    private var mValue: T? = null

    override val value: T
        get() {
            if (mValue == null) synchronized(this) {
                if (mValue == null) mValue = factory()
            }
            return mValue!!
        }

    override fun isInitialized(): Boolean = mValue != null

    fun close(callback: T.() -> Unit = {}) {
        mValue?.callback()
        mValue = null
    }
}