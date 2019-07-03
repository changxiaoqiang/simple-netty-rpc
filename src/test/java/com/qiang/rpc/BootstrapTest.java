package com.qiang.rpc;

import com.qiang.rpc.client.proxy.ClientProxy;
import com.qiang.rpc.services.Hello;
import org.junit.Test;

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
    public void helloTest() {
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 1099);
        Hello hello = (Hello) clientProxy.create(Hello.class.getName(), Hello.class);
        String re = hello.getHello();
        System.out.println(re);
        System.out.println(hello.getHello());
        System.out.println(hello.getHello());
    }
}
