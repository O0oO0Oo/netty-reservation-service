package org.server.rsaga.netty.http.mapping;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Objects;

@Slf4j
public abstract class AbstractNettyHandlerMapping implements NettyHandlerMapping {

    private MethodHandler defaultMethodHandler;
    private PathMatcher pathMatcher = new AntPathMatcher();
    private NettyUrlPathHelper urlPathHelper = new NettyUrlPathHelper();
    public NettyUrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    public MethodHandler getDefaultHandler() {
        return defaultMethodHandler;
    }

    public void setDefaultHandler(MethodHandler defaultMethodHandler) {
        this.defaultMethodHandler = defaultMethodHandler;
    }

    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    /**
     * @param request Http 요청 정보
     * @return reqeust 에 대한 controller Method 실행 클래스를 리턴
     * @throws Exception
     */
    @Override
    public HandlerExecution getHandler(FullHttpRequest request) {
        // AbstractNettyUrlHandlerMapping
        HandlerExecution handlerExecution = getHandlerInternal(request);

        if (handlerExecution == null) {
            // TODO : Default 를 해야하나? -> api 정보 같은?
            if (Objects.nonNull(defaultMethodHandler)) {
                handlerExecution = HandlerExecution.builder()
                        .handler(defaultMethodHandler)
                        .build();
            }
        }
        if (handlerExecution == null || handlerExecution.getHandler() == null) {
            return null;
        }

        // Cors, ExecutionChain 구현 X
        return handlerExecution;
    }

    protected String initLookupPath(FullHttpRequest request) {
        return "/" + request.method().name() + normalizePath(request.uri());
    }

    protected abstract HandlerExecution getHandlerInternal(FullHttpRequest request);

    // convert to {/asd/ , asd/ , asd} -> /asd
    protected String normalizePath(String path) {
        if(path.startsWith("/")){
            if(path.endsWith("/")){
                return path.substring(0, path.length() - 1);
            }
            return path;
        } else if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        else {
            return "/" + path;
        }
    }
}
