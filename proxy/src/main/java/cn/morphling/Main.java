package cn.morphling;

import cn.morphling.handler.LimitHandler;
import cn.morphling.handler.ProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        String host = "114.67.234.214";
        int port = 6379;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        LimitHandler limitHandler = new LimitHandler(Integer.MAX_VALUE);
        EventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ProxyHandler(host, port));
                pipeline.addLast(limitHandler);
            }
        });


        ChannelFuture serverChannelFuture = serverBootstrap.bind(8088).sync();
        System.out.println("complete");

        serverChannelFuture.channel().closeFuture().get();

    }
}
