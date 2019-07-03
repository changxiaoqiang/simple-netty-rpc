package com.qiang.rpc.client;

import com.qiang.rpc.beans.RpcRequest;
import com.qiang.rpc.beans.RpcResponse;
import com.qiang.rpc.handler.RpcClientHandler;
import com.qiang.rpc.util.RpcDecoder;
import com.qiang.rpc.util.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.ReentrantLock;

public class RpcClient {
    public static Logger logger = LogManager.getLogger(RpcClient.class);
    private static EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel = null;
    private String host;
    private int port;
    private Bootstrap bootstrap = null;
    private ReentrantLock lock = new ReentrantLock();

    private RpcClientHandler clientHandler = new RpcClientHandler();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (!this.isConnected()) {
            this.lock.lock();
            try {
                if (!this.isConnected()) {
                    bootstrap = new Bootstrap();
                    bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new RpcEncoder(RpcRequest.class));
                                    ch.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                                    ch.pipeline().addLast(new ReadTimeoutHandler(15));
                                    ch.pipeline().addLast(clientHandler);
                                }
                            })
                            .option(ChannelOption.SO_KEEPALIVE, true);
                    try {
                        ChannelFuture fulture = this.bootstrap.connect(this.host, this.port).sync();
                        this.channel = fulture.channel();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        logger.error(e);
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public boolean isConnected() {
        if (this.channel == null || !this.channel.isOpen() || !this.channel.isActive()) {
            return false;
        }
        return true;
    }

    public void close() {
        if (this.channel != null) {
            this.lock.lock();
            try {
                if (this.channel != null) {
                    this.channel.close();
                    this.channel = null;
                    this.bootstrap = null;
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public SynchronousQueue send(RpcRequest req) {
        this.connect();
        try {
            if (this.isConnected()) {
                SynchronousQueue<Object> queue = new SynchronousQueue<>();

                clientHandler.setQueueMap(req.getRequestId(), queue);
                channel.writeAndFlush(req).sync();
                System.out.println(req.getRequestId());
                return queue;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null;

    }

}
