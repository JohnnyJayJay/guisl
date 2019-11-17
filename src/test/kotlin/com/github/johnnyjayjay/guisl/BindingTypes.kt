package com.github.johnnyjayjay.guisl

import com.google.inject.*
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import java.lang.reflect.Constructor
import kotlin.reflect.KClass

class ScopeBinding(private val binder: Binder, private val annotationType: KClass<out Annotation>) {
    infix fun to(scope: Scope) {
        binder.bindScope(annotationType.java, scope)
    }
}

class ConstantBinding(private val binder: Binder) {

    private lateinit var annotation: Annotation
    private lateinit var annotationType: KClass<out Annotation>

    infix fun annotatedWith(annotation: Annotation): ConstantBinding {
        this.annotation = annotation
        return this
    }

    infix fun annotatedWith(annotationType: KClass<out Annotation>): ConstantBinding {
        this.annotationType = annotationType
        return this
    }

    infix fun <E : Enum<E>> to(enumValue: E) {
        build().to(enumValue)
    }

    infix fun to(int: Int) {
        build().to(int)
    }

    infix fun to(float: Float) {
        build().to(float)
    }

    infix fun to(double: Double) {
        build().to(double)
    }

    infix fun to(long: Long) {
        build().to(long)
    }

    infix fun to(short: Short) {
        build().to(short)
    }

    infix fun to(byte: Byte) {
        build().to(byte)
    }

    infix fun to(char: Char) {
        build().to(char)
    }

    infix fun to(boolean: Boolean) {
        build().to(boolean)
    }

    infix fun to(string: String) {
        build().to(string)
    }

    infix fun to(type: KClass<*>) {
        build().to(type.java)
    }

    private fun build() = binder.bindConstant().let {
        if (::annotation.isInitialized) {
            it.annotatedWith(annotation)
        } else if (::annotationType.isInitialized) {
            it.annotatedWith(annotationType.java)
        } else {
            throw RuntimeException("Constant binding must have an annotation")
        }
    }
}

sealed class ImplementationBinding<T : Any>(
    val binder: Binder,
    private val scope: Scope?,
    private val scopeAnnotation: KClass<out Annotation>?
) {

    private fun ScopedBindingBuilder.setScope() {
        if (scope != null) {
            `in`(scope)
        } else if (scopeAnnotation != null) {
            `in`(scopeAnnotation.java)
        }
    }

    infix fun to(implementation: KClass<out T>) {
        build().to(implementation.java).setScope()
    }

    infix fun to(implementation: TypeLiteral<out T>) {
        build().to(implementation).setScope()
    }

    infix fun to(targetKey: Key<out T>) {
        build().to(targetKey).setScope()
    }

    infix fun toProvider(provider: Provider<out T>) {
        build().toProvider(provider).setScope()
    }

    infix fun toProvider(provider: () -> T) {
        toProvider(Provider(provider))
    }

    infix fun toProvider(providerType: KClass<out Provider<out T>>) {
        build().toProvider(providerType.java).setScope()
    }

    infix fun toProvider(providerKey: Key<out Provider<out T>>) {
        build().toProvider(providerKey).setScope()
    }

    infix fun toInstance(instance: T) {
        build().toInstance(instance)
    }

    infix fun toConstructor(constructor: Constructor<T>) {
        build().toConstructor(constructor).setScope()
    }

    fun toConstructor(constructor: Constructor<T>, type: TypeLiteral<out T>) {
        build().toConstructor(constructor, type).setScope()
    }

    internal abstract fun build(): LinkedBindingBuilder<T>
}

class KeyBinding<T : Any>(
    val key: Key<T>,
    binder: Binder,
    scope: Scope?,
    scopeAnnotation: KClass<out Annotation>?
) : ImplementationBinding<T>(binder, scope, scopeAnnotation) {

    override fun build() = binder.bind(key)
}

sealed class TypeBinding<T : Any>(binder: Binder, scope: Scope?, scopeAnnotation: KClass<out Annotation>?) :
    ImplementationBinding<T>(binder, scope, scopeAnnotation) {

    private lateinit var annotation: Annotation
    private lateinit var annotationType: KClass<out Annotation>

    infix fun annotatedWith(annotation: Annotation): TypeBinding<T> {
        this.annotation = annotation
        return this
    }

    infix fun annotatedWith(annotationType: KClass<out Annotation>): TypeBinding<T> {
        this.annotationType = annotationType
        return this
    }

    internal abstract fun buildAnnotatable(): AnnotatedBindingBuilder<T>

    override fun build() =
        buildAnnotatable().let {
            if (::annotation.isInitialized) {
                it.annotatedWith(annotation)
            } else if (::annotationType.isInitialized) {
                it.annotatedWith(annotationType.java)
            } else {
                it
            }
        }
}

class ClassBinding<T : Any>(
    private val kclass: KClass<T>,
    binder: Binder,
    scope: Scope?,
    scopeAnnotation: KClass<out Annotation>?
) : TypeBinding<T>(binder, scope, scopeAnnotation) {

    override fun buildAnnotatable() = binder.bind(kclass.java)

}

class LiteralBinding<T : Any>(
    private val literal: TypeLiteral<T>, binder: Binder, scope: Scope?,
    scopeAnnotation: KClass<out Annotation>?
) : TypeBinding<T>(binder, scope, scopeAnnotation) {

    override fun buildAnnotatable() = binder.bind(literal)

}