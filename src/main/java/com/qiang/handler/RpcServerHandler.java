package com.qiang.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<MqttMessage> {
    public static Logger logger = LogManager.getLogger(RpcServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage message) throws Exception {
        handleMessage(ctx, message);
    }

    /**
     * 处理mqtt消息
     *
     * @param ctx
     * @param message
     */
    private void handleMessage(ChannelHandlerContext ctx, MqttMessage message) {

    }

    /**
     * ping 响应
     *
     * @param ctx
     * @param request
     */
    private void doPingreoMessage(ChannelHandlerContext ctx, MqttMessage request) {

    }

    /**
     * 封装发布
     *
     * @param str
     * @param topicName
     */
    public static MqttPublishMessage buildPublish(String str, String topicName, Integer messageId) {
        return null;
    }

    /**
     * 处理连接请求
     *
     * @param ctx
     * @param message
     */
    private void doConnectMessage(ChannelHandlerContext ctx, MqttMessage message) {

    }

    /**
     * 处理 客户端订阅消息
     *
     * @param ctx
     * @param request
     */
    private void doSubMessage(ChannelHandlerContext ctx, MqttMessage request) {

    }

    /**
     * 处理客户端回执消息
     *
     * @param ctx
     * @param request
     */
    private void doPubAck(ChannelHandlerContext ctx, MqttMessage request) {

    }

    /**
     * 处理 客户端发布消息。此处只有终端上报的 指令消息
     * 终端上报 指令执行结果。
     *
     * @param ctx
     * @param request
     */
    private void doPublishMessage(ChannelHandlerContext ctx, MqttMessage request) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.channel().close();
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        logger.error(cause);
        ctx.channel().close();
    }
}
