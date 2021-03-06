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
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class RpcClient {
    public static Logger logger = LogManager.getLogger(RpcClient.class);
    private static EventLoopGroup group = new NioEventLoopGroup();
    ExecutorService BehaviorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private Channel channel = null;
    private String host;
    private int port;
    private Bootstrap bootstrap = null;
    private ReentrantLock lock = new ReentrantLock();
    private HashedWheelTimer wheelTimer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS, 5000);
    private ChannelFuture future = null;
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
                                    ch.pipeline().addLast(new ReadTimeoutHandler(20));
                                    ch.pipeline().addLast(clientHandler);
                                }
                            })
                            .option(ChannelOption.SO_KEEPALIVE, true);
                    try {
                        this.future = this.bootstrap.connect(this.host, this.port).sync();
                        this.channel = this.future.channel();
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

    public HashMap<String, Object> send(RpcRequest req) {
        this.connect();
        try {
            if (this.isConnected()) {
                SynchronousQueue<Object> queue = new SynchronousQueue<>();

                clientHandler.putQueueMap(req.getRequestId(), queue);

                Timeout reqTimeout = timeOut(req, queue);

                BehaviorPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            channel.writeAndFlush(req).sync().addListeners(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    logger.info("send success {}: {}", req.getRequestId(), req.getClassName() + "." + req.getMethod());
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                return new HashMap<String, Object>() {{
                    put("timeout", reqTimeout);
                    put("queue", queue);
                }};
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null;
    }

    private Timeout timeOut(RpcRequest req, SynchronousQueue<Object> queue) {
        return wheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (future.cancel(true)) {
                    logger.info("task has been canceled. {}: {}", req.getRequestId(), req.getClassName() + "." + req.getMethod());

                    queue.put(new RpcResponse(500));

                    clientHandler.removeQueueMap(req.getRequestId());
                } else {
                    logger.info("req has been send but not get response and task has been canceled. {}: {}", req.getRequestId(), req.getClassName() + "." + req.getMethod());
                    queue.put(new RpcResponse(501));
                    clientHandler.removeQueueMap(req.getRequestId());

                }
            }
        }, 5, TimeUnit.SECONDS);
    }

}
