package com.bhadouriya.raml.template;

public abstract class ServiceTemplate<T> {

    public abstract T callService(Class<T> returnType, Object o);

    public final Object firstMethod() {
        return null;
    }

    public final void lastMethod(final T resp) {

    }

}
