package com.github.johnnyjayjay.guisl

import com.google.inject.*
import com.google.inject.matcher.Matcher
import com.google.inject.matcher.Matchers
import com.google.inject.spi.ProvisionListener
import com.google.inject.spi.TypeListener
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import java.lang.reflect.Method
import kotlin.reflect.KClass

inline fun module(crossinline body: Binder.() -> Unit) = Module { it.body() }

open class BindingScope(
    internal val binder: Binder,
    private val scope: Scope? = null,
    private val scopeAnnotation: KClass<out Annotation>?
) {

    val constant: ConstantBinding
        get() = ConstantBinding(binder)

    fun <T : Any> type(type: KClass<T>) =
        ClassBinding(type, binder, scope, scopeAnnotation).apply { buildAnnotatable() }

    fun <T : Any> type(type: TypeLiteral<T>) =
        LiteralBinding(type, binder, scope, scopeAnnotation).apply { buildAnnotatable() }

    fun <T : Any> type(key: Key<T>) =
        KeyBinding(key, binder, scope, scopeAnnotation).apply { build() }

}

class DefaultBindingScope(binder: Binder) : BindingScope(binder, null, null) {

    fun scopeAnnotation(annotationType: KClass<out Annotation>) =
        ScopeBinding(binder, annotationType)

    infix fun KClass<out Annotation>.to(scope: Scope) {
        binder.bindScope(this.java, scope)
    }

    fun scope(scope: Scope, body: BindingScope.() -> Unit) {
        BindingScope(binder, scope, null).body()
    }

    fun scope(scope: KClass<out Annotation>, body: BindingScope.() -> Unit) {
        BindingScope(binder, null, scope).body()
    }
}

fun Binder.typeListener(types: Matcher<in TypeLiteral<*>> = Matchers.any(), listener: TypeListener) {
    bindListener(types, listener)
}

fun Binder.provisionListeners(bindings: Matcher<in Binding<*>> = Matchers.any(), vararg listeners: ProvisionListener) {
    bindListener(bindings, *listeners)
}

fun Binder.intercept(
    classes: Matcher<in Class<*>> = Matchers.any(),
    methods: Matcher<in Method> = Matchers.any(),
    interceptor: (MethodInvocation) -> Any
) {
    bindInterceptor(classes, methods, MethodInterceptor(interceptor))
}

fun Binder.interceptors(
    classes: Matcher<in Class<*>> = Matchers.any(),
    methods: Matcher<in Method> = Matchers.any(),
    vararg interceptors: MethodInterceptor
) {
    bindInterceptor(classes, methods, *interceptors)
}

inline fun Binder.bind(body: DefaultBindingScope.() -> Unit) {
    DefaultBindingScope(this).body()
}


