package org.server.rsaga.netty.config;

import org.server.rsaga.netty.http.mapping.HttpMethodHandlerFactory;
import org.server.rsaga.netty.http.mapping.NettyHandlerMapping;
import org.server.rsaga.netty.http.mapping.NettySimpleUrlAsyncHandlerMapping;
import org.server.rsaga.netty.http.mapping.NettySimpleUrlHandlerMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * 요청이 들어왔을때 URL 로 컨트롤러의 메서드를 찾을 수 있게 해준다.
 */
@Configuration
public class InitialRouteRegistry {
    @Bean("nettySimpleUrlHandlerMapping")
    public NettyHandlerMapping nettySimpleUrlHandlerMapping(ApplicationContext applicationContext) {
        NettyHandlerMapping urlHandlerMapping = new NettySimpleUrlHandlerMapping<>(new HttpMethodHandlerFactory());
        urlHandlerMapping.registerHandlers(applicationContext, new Class[]{RestController.class, Controller.class});
        return urlHandlerMapping;
    }

    /**
     * {@link org.server.rsaga.common.annotation.AsyncResponse} 어노테이션이 붙은 메서드를 찾는다.
     */
    @Bean("nettySimpleUrlAsyncHandlerMapping")
    public NettyHandlerMapping nettySimpleUrlAsyncHandlerMapping(ApplicationContext applicationContext) {
        NettyHandlerMapping urlAsyncHandlerMapping = new NettySimpleUrlAsyncHandlerMapping<>(new HttpMethodHandlerFactory());
        urlAsyncHandlerMapping.registerHandlers(applicationContext, new Class[]{RestController.class, Controller.class});
        return urlAsyncHandlerMapping;
    }
}