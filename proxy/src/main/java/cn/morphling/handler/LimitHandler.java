package cn.morphling.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
public class LimitHandler extends ChannelDuplexHandler {

    private final AtomicInteger totalConnectionNumber = new AtomicInteger();
    private final int limitCount;

    public LimitHandler(int limitCount) {
        this.limitCount = limitCount;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int currConnection = totalConnectionNumber.getAndIncrement();
        if (currConnection >= limitCount) {
            ctx.close();
            System.out.println("连接已达到上限： " + currConnection + " / " + limitCount);
        } else {
            System.out.println("当前连接： " + totalConnectionNumber.get());
            super.channelActive(ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int currConnection = totalConnectionNumber.decrementAndGet();
        System.out.println("当前连接： " + currConnection);
        super.channelInactive(ctx);
    }
}
