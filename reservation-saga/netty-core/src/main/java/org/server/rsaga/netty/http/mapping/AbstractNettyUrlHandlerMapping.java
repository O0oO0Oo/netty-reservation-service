package org.server.rsaga.netty.http.mapping;

import io.netty.handler.codec.http.FullHttpRequest;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractNettyUrlHandlerMapping extends AbstractNettyHandlerMapping{

    private final Map<String, MethodHandler> handlerMap = new LinkedHashMap<>();

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

    public void registerHandlers(ApplicationContext applicationContext, Class<? extends Annotation>[] annotations) {
        for(Class<? extends Annotation> annotation : annotations) {
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotation);
            for (Object bean : beansWithAnnotation.values()) {
                doRegisterHandlers(bean);
            }
        }
    }

    protected abstract void doRegisterHandlers(Object bean);
}
