package com.qiang.rpc.handler;

import com.qiang.rpc.beans.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        String requestId = response.getResponseId();
        SynchronousQueue<Object> queue = queueMap.get(requestId);
        if (null != queue) {
            queue.put(response);
            removeQueueMap(requestId);
        } else {
            System.err.println("requestId: " + requestId + " has been removed.");
        }
    }

    public SynchronousQueue<Object> getQueuemap(String key) {
        return queueMap.get(key);
    }

    public void putQueueMap(String key, SynchronousQueue<Object> queue) {
        this.queueMap.put(key, queue);
    }

    public void removeQueueMap(String key) {
        this.queueMap.remove(key);
    }
}
