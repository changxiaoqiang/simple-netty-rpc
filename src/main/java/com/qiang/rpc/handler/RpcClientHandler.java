package com.qiang.rpc.handler;

import com.qiang.rpc.beans.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        String requestId = response.getResponseId();
        SynchronousQueue<Object> queue = queueMap.get(requestId);
        queue.put(response);
        queueMap.remove(requestId);
    }

    public SynchronousQueue<Object> getQueuemap(String key) {
        return queueMap.get(key);
    }

    public void setQueueMap(String key, SynchronousQueue<Object> queue) {
        this.queueMap.put(key, queue);
    }

    public void removeQueueMap(String key) {
        this.queueMap.remove(key);
    }
}
