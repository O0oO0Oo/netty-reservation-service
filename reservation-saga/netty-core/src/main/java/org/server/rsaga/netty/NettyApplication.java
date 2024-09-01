package org.server.rsaga.netty;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultExecutorServiceFactory;
import org.server.rsaga.netty.config.SpringApplicationContext;
import org.server.rsaga.netty.http.HttpPipelineInitializer;
import org.server.rsaga.netty.http.handler.AsyncRouteMappingHandler;
import org.server.rsaga.netty.http.handler.ExceptionHandler;
import org.server.rsaga.netty.http.handler.RouteMappingHandler;
import org.server.rsaga.netty.http.mapping.NettySimpleUrlAsyncHandlerMapping;
import org.server.rsaga.netty.http.mapping.NettySimpleUrlHandlerMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "org.server.rsaga",
})
@EnableJpaRepositories(basePackages = {
        "org.server.rsaga.business.infra.repository",
        "org.server.rsaga.reservableitem.infra.repository",
        "org.server.rsaga.reservation.infra.repository",
        "org.server.rsaga.user.infra.repository",
        "org.server.rsaga.payment.infra.repository"
})
public class NettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);


        HttpPipelineInitializer httpPipelineInitializer = createHttpPipelineInitializer();
        new NettyReservationServer("0.0.0.0",8080, httpPipelineInitializer).start();
    }

    /**
     * TODO 이벤트 실행 크기 조정.
     */
    private static HttpPipelineInitializer createHttpPipelineInitializer() {
        AsyncRouteMappingHandler asyncRouteMappingHandler = new AsyncRouteMappingHandler(
                SpringApplicationContext.getBean(NettySimpleUrlAsyncHandlerMapping.class),
                new DefaultEventExecutorGroup(
                        16,
                        new DefaultExecutorServiceFactory("async-executor-thread-group")
                )
        );

        RouteMappingHandler routeMappingHandler = new RouteMappingHandler(
                SpringApplicationContext.getBean(NettySimpleUrlHandlerMapping.class),
                new DefaultEventExecutorGroup(
                        16,
                        new DefaultExecutorServiceFactory("sync-executor-thread-group")
                        )
        );

        ExceptionHandler exceptionHandler = new ExceptionHandler();

        return new HttpPipelineInitializer(
                false,
                routeMappingHandler,
                asyncRouteMappingHandler,
                exceptionHandler
        );
    }
}