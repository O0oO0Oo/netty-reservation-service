package org.server.rsaga.netty.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.NoArgsConstructor;
import org.server.rsaga.netty.http.handler.ExceptionHandler;
import org.server.rsaga.netty.http.handler.RouteMappingHandler;

@NoArgsConstructor
public class HttpPipelineInitializer extends ChannelInitializer<Channel> {
    private boolean isClient = false;

    /**
     * @param isClient default true
     */
    public HttpPipelineInitializer(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if(isClient){
            pipeline.addLast("httpClientCodec", new HttpClientCodec());
        }else {
            pipeline.addLast("httpServerCodec", new HttpServerCodec());
        }

        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(512 * 1024));
        pipeline.addLast("routeMappingHandler", new RouteMappingHandler());
        pipeline.addLast("exceptionHandler", new ExceptionHandler());
    }
}