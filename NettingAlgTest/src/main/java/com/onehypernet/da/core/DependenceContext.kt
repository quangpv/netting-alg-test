package com.onehypernet.da.core

import com.onehypernet.da.core.viewmodel.ViewModel
import javafx.application.Application
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(
    val singleton: Boolean = false
)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectBy(
    val clazz: KClass<out Injectable>,
    val singleton: Boolean = false
)

interface Injectable
interface SingleInjectable : Injectable

interface Bean<T> {
    val value: T
}

interface Scope {
    fun contains(clazz: Class<*>): Boolean
    fun dispose()

    fun <T> factory(clazz: Class<T>, function: () -> T)
    fun <T> lookup(clazz: Class<T>): Bean<T>
}

private open class SimpleBean<T>(
    val isSingleton: Boolean,
    val function: () -> T
) : Bean<T> {
    private var mValue: T? = null

    override val value: T
        get() {
            return if (isSingleton) {
                if (mValue == null) synchronized(this) {
                    if (mValue == null) mValue = function()
                }
                mValue!!
            } else function()
        }
}

private class ScopeBean<T>(private val function: () -> T) : Bean<T> {
    private var mValue: T? = null

    override val value: T
        get() {
            if (mValue == null) mValue = function()
            return mValue!!
        }

    fun dispose() {
        mValue = null
    }
}

private class SimpleScope(private val context: DependenceContext) : Scope {
    private val mBean = hashMapOf<Class<*>, ScopeBean<*>>()

    override fun contains(clazz: Class<*>): Boolean {
        return mBean.containsKey(clazz)
    }

    override fun <T> factory(clazz: Class<T>, function: () -> T) {
        mBean[clazz] = ScopeBean(function)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) factory(clazz) {
            context.create(clazz)
        }
        return mBean[clazz] as Bean<T>
    }

    override fun dispose() {
        mBean.values.forEach { it.dispose() }
        mBean.clear()
    }
}

private class ApplicationBean(application: Application) : Bean<Application> {
    override val value: Application = application
}

abstract class ProvideContext {

    abstract fun getBean(clazz: Class<*>): Bean<*>?

    abstract fun modules(vararg module: Module)

    abstract fun <T> single(override: Boolean = false, clazz: Class<T>, function: () -> T)

    abstract fun <T> factory(override: Boolean = false, clazz: Class<T>, function: () -> T)

    abstract fun <T> scope(scopeId: String, clazz: Class<T>, function: () -> T)

    inline fun <reified T> single(override: Boolean = false, noinline function: () -> T) {
        return single(override, T::class.java, function)
    }

    inline fun <reified T> factory(override: Boolean = false, noinline function: () -> T) {
        return factory(override, T::class.java, function)
    }

    inline fun <reified T> scope(scopeId: String, noinline function: () -> T) {
        return scope(scopeId, T::class.java, function)
    }

    abstract fun <T> getOrNull(clazz: Class<T>): T?

    abstract fun <T> getOrNull(scopeId: String, clazz: Class<T>): T?

    fun <T> get(clazz: Class<T>): T {
        return getOrNull(clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    fun <T> get(scopeId: String, clazz: Class<T>): T {
        return getOrNull(scopeId, clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    inline fun <reified T> get(scopeId: String): T {
        return get(scopeId, T::class.java)
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }
}

class DependenceContext : ProvideContext() {

    private val mScopeBean = hashMapOf<String, Scope>()
    private val mBean = hashMapOf<Class<*>, Bean<*>>()
    private lateinit var mApplication: ApplicationBean

    internal fun set(application: Application) {
        mApplication = ApplicationBean(application)
    }

    private fun error(clazz: Class<*>) {
        error("Class ${clazz.simpleName} defined, please set override true to override this")
    }

    override fun getBean(clazz: Class<*>): Bean<*>? {
        if (mBean.containsKey(clazz)) return mBean[clazz]
        error("Not found ${clazz.javaClass.name}")
    }

    override fun <T> single(override: Boolean, clazz: Class<T>, function: () -> T) {
        if (mBean.containsKey(clazz) && !override) error(clazz)
        mBean[clazz] = SimpleBean(true, function)
    }

    override fun <T> factory(override: Boolean, clazz: Class<T>, function: () -> T) {
        if (mBean.containsKey(clazz) && !override) error(clazz)
        mBean[clazz] = SimpleBean(false, function)
    }

    override fun <T> scope(scopeId: String, clazz: Class<T>, function: () -> T) {
        val scope = getScope(scopeId)
        if (scope.contains(clazz)) error("Class ${clazz.simpleName} exist in scope $scopeId")
        scope.factory(clazz, function)
    }

    fun getScope(scopeId: String): Scope {
        return if (!mScopeBean.containsKey(scopeId))
            SimpleScope(this).also { mScopeBean[scopeId] = it }
        else mScopeBean[scopeId]!!
    }

    override fun modules(vararg module: Module) {
        module.forEach { it.provide() }
    }

    @Suppress("unchecked_cast")
    fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) {
            if (clazz.isAssignableFrom(Application::class.java)
            ) return mApplication as Bean<T>
            reflectProvideIfNeeded(clazz)
        }
        return getBean(clazz) as Bean<T>
    }

    fun <T> lookup(scopeId: String, clazz: Class<T>): Bean<T> {
        return getScope(scopeId).lookup(clazz)
    }

    private fun <T> reflectProvideIfNeeded(clazz: Class<T>) {
        when {
            ViewModel::class.java.isAssignableFrom(clazz) -> factory(clazz = clazz) {
                create(clazz)
            }
            clazz.isInterface -> provideByInjectBy(clazz)
            else -> provideByInject(clazz)
        }
    }

    @Suppress("unchecked_cast")
    private fun <T> provideByInjectBy(clazz: Class<T>) {
        val annotation = clazz.getAnnotation(InjectBy::class.java)
            ?: error("Not found provider for ${clazz.simpleName}")
        val byClazz = annotation.clazz.java

        if (annotation.singleton) single(clazz = clazz) {
            create(byClazz) as T
        } else factory(clazz = clazz) {
            create(byClazz) as T
        }
    }

    private fun <T> provideByInject(clazz: Class<T>) {
        val annotation = clazz.getAnnotation(Inject::class.java)
        var shouldProvide = false
        var singleton = false

        when {
            annotation != null -> {
                shouldProvide = true
                singleton = annotation.singleton
            }
            Injectable::class.java.isAssignableFrom(clazz) -> {
                shouldProvide = true
                singleton = SingleInjectable::class.java.isAssignableFrom(clazz)
            }
        }
        if (!shouldProvide) error("Not found declaration for ${clazz.simpleName}")

        if (singleton) single(clazz = clazz) {
            create(clazz)
        } else factory(clazz = clazz) {
            create(clazz)
        }
    }

    override fun <T> getOrNull(clazz: Class<T>): T? {
        return (lookup(clazz) as? Bean<T>)?.value
    }

    override fun <T> getOrNull(scopeId: String, clazz: Class<T>): T? {
        return lookup(scopeId, clazz).value
    }

    @Suppress("unchecked_cast")
    fun <T> create(clazz: Class<T>): T {
        val constructor = clazz.constructors.firstOrNull()
            ?: clazz.declaredConstructors.firstOrNull()
            ?: error("Not found constructor for ${clazz.simpleName}")

        val paramTypes = constructor.genericParameterTypes
        return try {
            constructor.newInstance(*paramTypes.map { lookup(it as Class<*>).value }
                .toTypedArray()) as T
        } catch (e: Throwable) {
            Log.e("DependencyContext", "Error lookup for ${clazz.name}")
            throw e
        }
    }

}

class Module(
    private val context: DependenceContext,
    private val provide: (DependenceContext) -> Unit
) : ProvideContext() {
    private var mModules: Array<out Module>? = null

    override fun getBean(clazz: Class<*>): Bean<*>? {
        return context.getBean(clazz)
    }

    override fun modules(vararg module: Module) {
        mModules = module
    }

    override fun <T> single(override: Boolean, clazz: Class<T>, function: () -> T) {
        context.single(override, clazz, function)
    }

    override fun <T> factory(override: Boolean, clazz: Class<T>, function: () -> T) {
        context.factory(override, clazz, function)
    }

    override fun <T> scope(scopeId: String, clazz: Class<T>, function: () -> T) {
        context.scope(scopeId, clazz, function)
    }

    override fun <T> getOrNull(clazz: Class<T>): T? {
        return context.getOrNull(clazz)
    }

    override fun <T> getOrNull(scopeId: String, clazz: Class<T>): T? {
        return context.getOrNull(scopeId, clazz)
    }

    fun provide() {
        mModules?.forEach { it.provide() }
        provide(context)
    }
}

fun module(function: ProvideContext.() -> Unit): Module {
    return Module(dependenceContext, function)
}

inline fun <reified T> inject() = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(T::class.java)
}

inline fun <reified T> inject(scopeId: String) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(scopeId, T::class.java)
}

val dependenceContext = DependenceContext()

fun Application.dependencies(function: DependenceContext.() -> Unit) {
    dependenceContext.set(this)
    function(dependenceContext)
}