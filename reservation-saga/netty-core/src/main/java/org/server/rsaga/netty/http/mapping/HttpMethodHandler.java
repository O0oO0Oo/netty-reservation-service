package org.server.rsaga.netty.http.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpMethodHandler implements MethodHandler {
    private Object instance;
    private Method method;

    @Override
    public Object execute(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, args);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }
}