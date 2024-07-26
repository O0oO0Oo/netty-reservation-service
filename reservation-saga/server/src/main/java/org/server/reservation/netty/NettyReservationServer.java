package org.server.reservation.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyReservationServer {
    private String host;
    private int port;
    private ChannelInitializer<Channel> channelInitializer;

    public NettyReservationServer(String host, int port, ChannelInitializer<Channel> channelInitializer) {
        this.host = host;
        this.port = port;
        this.channelInitializer = channelInitializer;
    }

    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(host, port)).sync();
            channelFuture.addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()){
                            log.info("Netty started on port 8080 (http) with context path '/'");
                        }else {
                            log.error("Connection failed.");
                            future.cause().printStackTrace();
                        }
                    }
            );

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
