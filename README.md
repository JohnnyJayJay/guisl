# guisl
Guisl is a small embedded domain specific language for [Guice](https://github.com/google/guice)
modules in Kotlin.

Java equivalents in comments:
```kotlin
module { 
    // ad hoc MethodInterceptor ("classes" and "methods" are Matchers, by default Matchers.any())
    intercept(
        classes = subclassesOf(MySuperClass::class.java),
        methods = annotatedWith(MyAnnotation::class.java)
    ) {
        it.arguments.forEach(::println)
        it.proceed()
    }
    
    // more extensions for type and provision listeners available //
    
    bind {
        // bindConstant().annotatedWith(Pi.class).to(Math.PI)
        constant annotatedWith Pi::class to Math.PI
        // bindScope(CustomScope.class, CustomScope.INSTANCE)
        scopeAnnotation(CustomScope::class) to CustomScope
        // bind(Key.get(ProvidedClass.class))
        type(Key.get(ProvidedClass::class))
        // bind(OtherProvidedClass.class).annotatedWith(MyAnnotation.class).to(SomeImplementation.class)
        type(OtherProvidedClass::class) annotatedWith MyAnnotation::class to SomeImplementation::class
        // bind(new TypeLiteral<GenericClass<String>>() {}).to(GenericClassImpl.class)
        type(literally<GenericClass<String>>()) to GenericClassImpl::class
        scope(CustomScope) {
            // bind(String.class).toInstance("foo").in(CustomScope.INSTANCE)
            type(String::class) toInstance "foo"
            // bind(int.class).annotatedWith(SomeOtherAnnotation.class).toProvider(() -> 42 - 67).in(CustomScope.INSTANCE)
            type(Int::class) annotatedWith SomeOtherAnnotation::class toProvider { 42 - 67 }
        }
    }
}
```