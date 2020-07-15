package cn.morphling;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        EventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.childHandler(new ChannelInitializer<LocalChannel>() {
            @Override
            protected void initChannel(LocalChannel ch) throws Exception {
            }
        });

//        ######### client ##############
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.group(new NioEventLoopGroup());

        bootstrap.handler(new ChannelInitializer<LocalChannel>() {
            @Override
            protected void initChannel(LocalChannel ch) throws Exception {
                ByteBuf buffer = ch.alloc().buffer();
                ch.writeAndFlush(buffer);
            }
        });

        ChannelFuture clientChannelFuture = bootstrap.connect("114.67.234.214", 6379);
        clientChannelFuture.sync();
//        ######### client ##############

        ChannelFuture serverChannelFuture = serverBootstrap.bind(8090).sync();
        System.out.println("complete");

        Channel read = serverChannelFuture.channel().read();

        clientChannelFuture.channel().closeFuture().get();
        serverChannelFuture.channel().closeFuture().get();

    }
}
