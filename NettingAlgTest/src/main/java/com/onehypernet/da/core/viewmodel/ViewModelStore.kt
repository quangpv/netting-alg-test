package com.onehypernet.da.core.viewmodel

import kotlin.reflect.KClass

class ViewModelStore {
    private val mViewModelCache = hashMapOf<String, ViewModel>()

    @Suppress("unchecked_cast")
    fun <T : ViewModel> get(clazz: KClass<T>, factory: (Class<T>) -> T): T {
        val key = clazz.java.name
        if (mViewModelCache.containsKey(key)) return mViewModelCache[key] as T
        val vm = factory(clazz.java)
        mViewModelCache[key] = vm
        return vm
    }
}