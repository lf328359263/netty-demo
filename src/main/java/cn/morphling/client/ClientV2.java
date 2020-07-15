package cn.morphling.client;

import cn.morphling.client.codec.*;
import cn.morphling.client.handler.dispacher.OperationResultFuture;
import cn.morphling.client.handler.dispacher.RequestPendingCenter;
import cn.morphling.client.handler.dispacher.ResponseDispatcherHandler;
import cn.morphling.common.OperationResult;
import cn.morphling.common.RequestMessage;
import cn.morphling.common.order.OrderOperation;
import cn.morphling.util.IdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

public class ClientV2 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);

        RequestPendingCenter pendingCenter = new RequestPendingCenter();
        bootstrap.group(new NioEventLoopGroup());

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolEncoder());
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new ResponseDispatcherHandler(pendingCenter));

                pipeline.addLast(new OperationToRequestMessageEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = bootstrap.connect("localhost", 8090);
        channelFuture.sync();
        long streamId = IdUtil.nextId();
        RequestMessage requestMessage = new RequestMessage(streamId, new OrderOperation(1001, "tudou"));
        OperationResultFuture operationResultFuture = new OperationResultFuture();
        pendingCenter.add(streamId, operationResultFuture);

        channelFuture.channel().writeAndFlush(requestMessage);

        OperationResult operationResult = operationResultFuture.get();
        System.out.println(operationResult);
//        OrderOperation orderOperation = new OrderOperation(1001, "tudou");
//        channelFuture.channel().writeAndFlush(orderOperation);

        channelFuture.channel().closeFuture().get();
    }
}
