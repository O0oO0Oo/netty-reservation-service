package com.server.reservation.netty.http.mapping;

import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

public class NettySimpleUrlHandlerMapping<T extends MethodHandler> extends AbstractNettyUrlHandlerMapping {
    private final MethodHandlerFactory<T> handlerFactor;

    public NettySimpleUrlHandlerMapping(MethodHandlerFactory<T> handlerFactor) {
        this.handlerFactor = handlerFactor;
    }

    @Override
    protected void doRegisterHandlers(Object bean) {
        Class<?> controllerClazz = bean.getClass();
        RequestMapping requestMapping = controllerClazz.getAnnotation(RequestMapping.class);
        String[] requestMappingPaths = getRequestMappingValue(requestMapping);

        for (Method method : controllerClazz.getMethods()) {
            T handler = handlerFactor.createHandler();
            handler.setMethod(bean, method);

            StringBuilder httpMethod = new StringBuilder();
            String[] methodMappingPaths = getMethodPaths(method, httpMethod);

            if (requestMappingPaths.length > 0) {
                for (String requestMappingPath : requestMappingPaths) {
                    if(!httpMethod.isEmpty() && methodMappingPaths.length == 0){
                        getHandlerMap().put("/" + httpMethod + normalizePath(requestMappingPath), handler);
                    }
                    else {
                        for (String methodMappingPath : methodMappingPaths) {
                            getHandlerMap().put("/" + httpMethod + normalizePath(requestMappingPath) + normalizePath(methodMappingPath), handler);
                        }
                    }
                }
            } else {
                for (String methodMappingPath : methodMappingPaths) {
                    getHandlerMap().put("/" + httpMethod + normalizePath(methodMappingPath), handler);
                }
            }
        }
    }

    private String[] getRequestMappingValue(RequestMapping requestMapping) {
        if(Objects.isNull(requestMapping)) {
            return new String[]{};
        }
        else {
            return requestMapping.value();
        }
    }

    private String[] getMethodPaths(Method method, StringBuilder httpMethod) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof GetMapping getMapping) {
                httpMethod.append("GET");
                return getMapping.value();
            } else if (annotation instanceof PostMapping postMapping) {
                httpMethod.append("POST");
                return postMapping.value();
            } else if (annotation instanceof PutMapping putMapping) {
                httpMethod.append("PUT");
                return putMapping.value();
            } else if (annotation instanceof DeleteMapping deleteMapping) {
                httpMethod.append("DELETE");
                return deleteMapping.value();
            } else if (annotation instanceof PatchMapping patchMapping) {
                httpMethod.append("PATCH");
                return patchMapping.value();
            }
        }

        return new String[]{};
    }
}
