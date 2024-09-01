package org.server.rsaga.netty.http.mapping;

import io.netty.handler.codec.http.FullHttpRequest;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.annotation.AsyncResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public abstract class AbstractNettyUrlHandlerMapping<T extends MethodHandler> extends AbstractNettyHandlerMapping{
    private final Map<String, MethodHandler> handlerMap = new LinkedHashMap<>();
    private final MethodHandlerFactory<T> handlerFactor;

    protected AbstractNettyUrlHandlerMapping(MethodHandlerFactory<T> handlerFactor) {
        this.handlerFactor = handlerFactor;
    }

    protected Map<String, MethodHandler> getHandlerMap() {
        return handlerMap;
    }

    @Override
    protected HandlerExecution getHandlerInternal(FullHttpRequest request) {
        String lookupPath = initLookupPath(request);
        return lookupHandler(lookupPath, request);
    }

    @Nullable
    protected HandlerExecution lookupHandler(String path, FullHttpRequest request) {
        MethodHandler methodHandler = getDirectMatch(path, request);
        if (methodHandler != null) {
            // 바로 매칭된다면 path parameter 가 없는것.
            return buildHandlerExecution(methodHandler, request);
        }

        // 유사한 url 을 가져옴
        ArrayList<String> matchingPatterns = new ArrayList<>();
        for (String registeredPattern : this.handlerMap.keySet()) {
            if (getPathMatcher().match(registeredPattern, path)) {
                matchingPatterns.add(registeredPattern);
            }
        }

        // 우선 순위
        String bestMatch = null;
        Comparator<String> patternComparator = getPathMatcher().getPatternComparator(path);
        if (!matchingPatterns.isEmpty()) {
            matchingPatterns.sort(patternComparator);
            if (log.isTraceEnabled() && matchingPatterns.size() > 1) {
                log.trace("Matching patterns " + matchingPatterns);
            }
            bestMatch = matchingPatterns.get(0);
        }

        if (bestMatch != null) {
            methodHandler = this.handlerMap.get(bestMatch);
            if (methodHandler == null) {
                throw new IllegalStateException(
                        "Could not find methodHandler for best pattern match [" + bestMatch + "]"
                );
            }

            // bestMatch 가 여러개 일 경우
            Map<String, String> uriTemplateVariables = new LinkedHashMap<>();
            for (String matchingPattern: matchingPatterns){
                if (patternComparator.compare(bestMatch, matchingPattern) == 0) {
                    Map<String, String> vars = getPathMatcher().extractUriTemplateVariables(matchingPattern, path);
                    Map<String, String> decodedVars = getUrlPathHelper().decodePathVariables(vars);
                    uriTemplateVariables.putAll(decodedVars);
                }
            }

            if (log.isTraceEnabled() && uriTemplateVariables.size() > 0) {
                log.trace("URI variables " + uriTemplateVariables);
            }

            return buildHandlerExecution(methodHandler, uriTemplateVariables, request);
        }

        // no methodHandler found
        return null;
    }

    @Nullable
    private MethodHandler getDirectMatch(String urlPath, FullHttpRequest request) {
        return this.handlerMap.get(urlPath);
    }

    private HandlerExecution buildHandlerExecution(MethodHandler methodHandler, Map<String, String> uriTemplateVariables, FullHttpRequest request) {
        // 핸들러 템플릿 변수 주입
        return HandlerExecution.builder()
                .handler(methodHandler)
                .uriTemplateVariables(uriTemplateVariables)
                .bodyString(request.content().toString(StandardCharsets.UTF_8))
                .build();
    }

    private HandlerExecution buildHandlerExecution(MethodHandler methodHandler, FullHttpRequest request) {
        return HandlerExecution.builder()
                .handler(methodHandler)
                .bodyString(request.content().toString(StandardCharsets.UTF_8))
                .build();
    }

    @Override
    public void registerHandlers(ApplicationContext applicationContext, Class<? extends Annotation>[] annotations) {
        for(Class<? extends Annotation> annotation : annotations) {
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotation);
            for (Object bean : beansWithAnnotation.values()) {
                doRegisterHandlers(bean);
            }
        }
    }

    private void doRegisterHandlers(Object bean) {
        Class<?> controllerClazz = bean.getClass();
        RequestMapping requestMapping = controllerClazz.getAnnotation(RequestMapping.class);
        String[] requestMappingPaths = getRequestMappingValue(requestMapping);

        for (Method method : controllerClazz.getMethods()) {
            if (!shouldRegisterMethod(method)) {
                continue;
            }

            T handler = handlerFactor.createHandler();
            handler.setMethod(bean, method);

            StringBuilder httpMethod = new StringBuilder();
            String[] methodMappingPaths = getMethodPaths(method, httpMethod);

            registerHandlerPaths(handler, requestMappingPaths, methodMappingPaths, httpMethod.toString());
        }
    }

    /**
     * 등록해야할 메서드 조건
     */
    protected abstract boolean shouldRegisterMethod(Method method);

    private void registerHandlerPaths(T handler, String[] requestMappingPaths, String[] methodMappingPaths, String httpMethod) {
        if (requestMappingPaths.length > 0) {
            for (String requestMappingPath : requestMappingPaths) {
                if (!httpMethod.isEmpty() && methodMappingPaths.length == 0) {
                    getHandlerMap().put("/" + httpMethod + normalizePath(requestMappingPath), handler);
                } else {
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


    private String[] getRequestMappingValue(RequestMapping requestMapping) {
        return Objects.isNull(requestMapping) ? new String[]{} : requestMapping.value();
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
