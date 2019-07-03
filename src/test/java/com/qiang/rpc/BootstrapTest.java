package com.qiang.rpc;

import com.qiang.rpc.client.proxy.ClientProxy;
import com.qiang.rpc.services.Hello;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple Bootstrap.
 */
public class BootstrapTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void helloTest() throws Exception {
        ExecutorService BehaviorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 1099);

        for (int i = 0; i < 1000; i++) {

            Hello hello = (Hello) clientProxy.create(Hello.class.getName(), Hello.class);
            String re = hello.getHello();
            System.out.println(re);

        }
    }
}
