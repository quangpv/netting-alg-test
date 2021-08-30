package com.onehypernet.da.core.viewmodel

import com.onehypernet.da.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class ViewModel {
    val scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.JavaFx
    }

    fun clear() {
        onCleared()
        scope.cancel()
    }

    protected open fun onCleared() {}
}

fun ViewModel.launch(
    loading: LiveData<Boolean>? = null,
    error: LiveData<Throwable>? = null,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(context) {
        try {
            (loading as? MutableLiveData)?.post(true)
            block()
        } catch (e: CancellationException) {
        } catch (e: Throwable) {
            Log.e(javaClass.simpleName, e)
            (error as? MutableLiveData)?.post(e)
        } finally {
            (loading as? MutableLiveData)?.post(false)
        }
    }
}

inline fun <reified T : ViewModel> Controller.viewModel(): Lazy<T> = lazy { getViewModel() }

inline fun <reified T : ViewModel> Controller.getViewModel(): T {
    val vm = viewModelStore.get(T::class) { dependenceContext.get(it) }
    lifecycle.addObserver(object : LifecycleObserver {
        override fun onDestroy() {
            vm.clear()
        }
    })
    return vm
}


inline fun <reified T : ViewModel> viewModel(crossinline owner: () -> Controller): Lazy<T> = lazy {
    owner().getViewModel()
}
