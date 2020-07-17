package cn.morphling.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

public class ProxyHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture cf;
    private final String host;
    private final int port;

    public ProxyHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void createClient(ChannelHandlerContext ctx, Object msg) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch)  {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx0, Object msg) {
                                ctx.channel().writeAndFlush(msg);
                            }
                        });
                    }
                });
        cf = bootstrap.connect(host, port);
        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(msg);
            } else {
                System.out.println("failed: " + future.cause().getMessage());
                ctx.channel().close();
            }
        });
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cf == null) {
            createClient(ctx, msg);
        } else {
            cf.channel().writeAndFlush(msg);
        }
    }

}
