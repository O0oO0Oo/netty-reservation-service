package org.server.rsaga.netty.http.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface MethodHandler {
    Object execute(Object... args) throws InvocationTargetException, IllegalAccessException;
    Method getMethod();
    void setMethod(Object object, Method method);
}