package com.qiang.rpc.services;

@RpcService(Hello.class)
public class HelloImpl implements Hello {
    public String getHello() {
        return "hello world";
    }
}
