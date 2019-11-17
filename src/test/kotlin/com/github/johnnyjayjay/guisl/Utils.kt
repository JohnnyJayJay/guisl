package com.github.johnnyjayjay.guisl

import com.google.inject.TypeLiteral

inline fun <reified T> literally() = object : TypeLiteral<T>() {}

