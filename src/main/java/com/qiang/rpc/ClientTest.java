package com.qiang.rpc;

import com.qiang.rpc.client.proxy.ClientProxy;
import com.qiang.rpc.services.Hello;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static void main(String[] args) {
        ExecutorService BehaviorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 2181);

        for (int i = 0; i < 10; i++) {
            int a = i;
            BehaviorPool.execute(new Runnable() {
                @Override
                public void run() {
                    Hello hello = (Hello) clientProxy.create(Hello.class.getName(), Hello.class);
                    String re = hello.getHello();
                    System.out.println(re + "\t" + a);
                }
            });
        }
    }
}
