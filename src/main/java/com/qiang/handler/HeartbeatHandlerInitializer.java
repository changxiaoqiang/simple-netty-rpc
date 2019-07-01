package com.qiang.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class HeartbeatHandlerInitializer extends ChannelInitializer<Channel> {
    private int readIdelTimeOut; // 读超时
    private int writeIdelTimeOut;// 写超时
    private int allIdelTimeOut; // 所有超时
    private TimeUnit timeUnit;

    public HeartbeatHandlerInitializer(int readerIdelTimeOut, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdelTimeOut, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public HeartbeatHandlerInitializer(int readerIdelTimeOut, int writerIdleTimeSeconds, int allIdleTimeSeconds, TimeUnit timeUnit) {
        this.readIdelTimeOut = readerIdelTimeOut;
        this.writeIdelTimeOut = writerIdleTimeSeconds;
        this.allIdelTimeOut = allIdleTimeSeconds;
        this.timeUnit = timeUnit;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(readIdelTimeOut,
                writeIdelTimeOut, allIdelTimeOut, timeUnit));
        pipeline.addLast(new HeartbeatClientHandler());
    }
}
