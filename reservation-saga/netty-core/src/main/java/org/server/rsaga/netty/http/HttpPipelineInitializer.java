package org.server.rsaga.netty.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.server.rsaga.netty.http.handler.AsyncRouteMappingHandler;
import org.server.rsaga.netty.http.handler.ExceptionHandler;
import org.server.rsaga.netty.http.handler.RouteMappingHandler;

public class HttpPipelineInitializer extends ChannelInitializer<Channel> {
    private final boolean isClient;
    private final RouteMappingHandler routeMappingHandler;
    private final AsyncRouteMappingHandler asyncRouteMappingHandler;
    private final ExceptionHandler exceptionHandler;

    public HttpPipelineInitializer(boolean isClient,
                                   RouteMappingHandler routeMappingHandler,
                                   AsyncRouteMappingHandler asyncRouteMappingHandler,
                                   ExceptionHandler exceptionHandler) {
        this.isClient = isClient;
        this.routeMappingHandler = routeMappingHandler;
        this.asyncRouteMappingHandler = asyncRouteMappingHandler;
        this.exceptionHandler = exceptionHandler;
    }


    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if(isClient){
            pipeline.addLast("httpClientCodec", new HttpClientCodec());
        }else {
            pipeline.addLast("httpServerCodec", new HttpServerCodec());
        }

        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(512 * 1024));
        pipeline.addLast("routeMappingHandler", routeMappingHandler);
        pipeline.addLast("AsyncRequestHandler", asyncRouteMappingHandler);
        pipeline.addLast("exceptionHandler", exceptionHandler);
    }
}