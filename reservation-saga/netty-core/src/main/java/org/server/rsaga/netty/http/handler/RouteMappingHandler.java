package org.server.rsaga.netty.http.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.netty.config.SpringApplicationContext;
import org.server.rsaga.netty.http.mapping.HandlerExecution;
import org.server.rsaga.netty.http.mapping.NettyHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusOutputFormat;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;

import java.lang.reflect.InvocationTargetException;

/**
 * todo refactoring
 */
@Slf4j
@ChannelHandler.Sharable
public class RouteMappingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final NettyHandlerMapping nettyHandlerMapping;
    private final PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    public RouteMappingHandler() {
        this.nettyHandlerMapping = SpringApplicationContext.getBean(NettyHandlerMapping.class);
        this.prometheusScrapeEndpoint = SpringApplicationContext.getBean(PrometheusScrapeEndpoint.class);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.uri();

        if (uri.startsWith("/actuator/prometheus")) {
            FullHttpResponse response = handlePrometheusActuatorRequest();
            ctx.writeAndFlush(response);
        } else {
            HandlerExecution methodHandler = nettyHandlerMapping.getHandler(msg);
            FullHttpResponse response = createResponse(methodHandler);
            ctx.writeAndFlush(response);
        }
    }

    /**
     * Prometheus 메트릭 응답
     */
    private FullHttpResponse handlePrometheusActuatorRequest() {
        WebEndpointResponse<byte[]> response = prometheusScrapeEndpoint.scrape(
                PrometheusOutputFormat.CONTENT_TYPE_PROTOBUF,
                null
        );

        byte[] metricsData = response.getBody();

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(metricsData)
        );

        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(metricsData.length));
        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
        fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        return fullHttpResponse;
    }

    private FullHttpResponse createResponse(HandlerExecution methodHandler) throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        if (methodHandler == null) {
            return FullHttpResponseBuilder.builder()
                    .body("The requested resource was not found.")
                    .statusCode(HttpResponseStatus.NOT_FOUND)
                    .build();
        }
        else {
            Object retValue = methodHandler.execute();

            if (retValue instanceof FullHttpResponse) {
                return (FullHttpResponse) retValue;
            } else {
                return FullHttpResponseBuilder.builder()
                        .body(retValue)
                        .statusCode(HttpResponseStatus.OK)
                        .build();
            }
        }
    }
}