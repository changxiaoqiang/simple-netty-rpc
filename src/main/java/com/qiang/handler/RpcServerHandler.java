package com.qiang.handler;

import com.qiang.beans.RpcRequest;
import com.qiang.beans.RpcResponse;
import com.qiang.container.RpcContainer;
import com.qiang.exception.RequestNotSupportExistException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    public static Logger logger = LogManager.getLogger(RpcServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        handleMessage(ctx, request);
    }

    /**
     * 处理mqtt消息
     *
     * @param ctx
     * @param request
     */
    private void handleMessage(ChannelHandlerContext ctx, final RpcRequest request) {
        String requestName = !StringUtils.isEmpty(request.getName()) ? request.getName() : request.getClassName();
        String className = request.getClassName();

        RpcResponse response = new RpcResponse();

        if (null != RpcContainer.getService(requestName)) {
            Object serviceBean = RpcContainer.getService(requestName);
            String methodName = request.getMethod();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] args = request.getArgs();

            try {
                Method method = Class.forName(className).getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceBean, args);
                response.setResult(result);
                response.setCode(200);
            } catch (ClassNotFoundException e) {
                response.setCode(400);
                response.setThrowable(new RequestNotSupportExistException(className, e));
            } catch (NoSuchMethodException ex) {
                response.setCode(400);
                response.setThrowable(new RequestNotSupportExistException(className + "." + methodName, ex));
            } catch (Exception ee) {
                response.setCode(500);
                response.setThrowable(new RequestNotSupportExistException(className + "." + methodName, ee));
            }
        } else {
            response.setCode(404);
            response.setResponseId(request.getRequestId());
            response.setThrowable(new RequestNotSupportExistException(requestName));
        }

        ctx.writeAndFlush(response).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.out.println(request.getRequestId());
            }
        });
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
