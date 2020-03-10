package com.qiang.rpc.server;

import com.qiang.rpc.beans.RpcRequest;
import com.qiang.rpc.beans.RpcResponse;
import com.qiang.rpc.container.RpcContainer;
import com.qiang.rpc.handler.HeartbeatHandlerInitializer;
import com.qiang.rpc.handler.RpcServerHandler;
import com.qiang.rpc.util.RpcDecoder;
import com.qiang.rpc.util.RpcEncoder;
import com.qiang.rpc.zk.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class RpcServer {
    final static Logger logger = LogManager.getLogger(RpcServer.class);

    private int port;

    private ServiceRegistry registry;

    public RpcServer(int port) {
        this.port = port;
    }

    public void start(ApplicationContext context) {
        RpcContainer.register(context);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            final RpcServerHandler handler = new RpcServerHandler();

            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).option(ChannelOption.SO_BACKLOG, 128)   // option在初始化时就会执行，设置tcp缓冲区
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new RpcDecoder(RpcRequest.class));
                            pipeline.addLast("encoder", new RpcEncoder(RpcResponse.class));
                            pipeline.addLast("heartbeat", new HeartbeatHandlerInitializer(15, 18, 20));
                            pipeline.addLast("handler", handler);
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);    // childOption会在客户端成功connect后才执行，设置保持连接;

            logger.info("start server " + port);

            ChannelFuture future = serverBootstrap.bind(port).sync();   // 绑定端口， 阻塞等待服务器启动完成,调用sync()方法会一直阻塞等待channel的停止
            // 注册到 ZK 中
            context.getBean(ServiceRegistry.class).register("127.0.0.1:1099");

            Channel channel = future.channel();
            channel.closeFuture().sync();                // 等待关闭 ，等待服务器套接字关闭

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
