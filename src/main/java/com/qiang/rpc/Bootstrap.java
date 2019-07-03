package com.qiang.rpc;

import com.qiang.rpc.server.RpcServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * BootStrap
 */
public class Bootstrap {
    public static void main(String[] args) {
        RpcServer server = new RpcServer(1099);
        server.start(new ClassPathXmlApplicationContext("application-server.xml"));

    }
}
