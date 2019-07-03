package com.qiang.rpc.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // TCP 粘包问题
        if (byteBuf.readableBytes() > 4) {
            byteBuf.markReaderIndex();
            int size = byteBuf.readInt();

            if(size < 0){
                channelHandlerContext.close();
            }

            if(byteBuf.readableBytes() < size){
                byteBuf.resetReaderIndex();
                return;
            }

            byte[] bytes = new byte[size];
            byteBuf.readBytes(bytes);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream inn = new ObjectInputStream(in);
            Object info = inn.readObject();
            list.add(info);
        }

    }
}
