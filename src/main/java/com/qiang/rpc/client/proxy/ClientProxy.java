package com.qiang.rpc.client.proxy;

import com.qiang.rpc.beans.RpcRequest;
import com.qiang.rpc.beans.RpcResponse;
import com.qiang.rpc.client.RpcClient;
import com.qiang.rpc.util.KeyUtil;
import com.qiang.rpc.zk.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class ClientProxy {
    private final static ConcurrentHashMap<String, RpcClient> clients = new ConcurrentHashMap<>();
    private String host;
    private int port;

    private ServiceDiscovery discovery;

    public ClientProxy(String host, int port) {
        this.host = host;
        this.port = port;

        discovery = new ServiceDiscovery(host + ":" + port);
    }

    public <T> T create(final String requestName, final Class<?> interfaceType) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(), new Class<?>[]{interfaceType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(KeyUtil.generateSessionId());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethod(method.getName());
                        request.setName(requestName);
                        request.setParameterTypes(method.getParameterTypes());
                        request.setArgs(args);

                        String target = host + ":" + port;

                        if(null != discovery){
                            //发现服务
                            target = discovery.discovery();
                        }

                        if(discovery == null){
                            throw new RuntimeException("serverAddress is null...");
                        }

                        String[] host = target.split(":");
                        if (!clients.containsKey(target)) {
                            RpcClient client = new RpcClient(host[0], Integer.parseInt(host[1]));
                            clients.put(target, client);
                        }
//                        RpcClient client = new RpcClient(host[0], Integer.parseInt(host[1]));

                        RpcClient client = clients.get(target);

                        SynchronousQueue<RpcResponse> queue = client.send(request);
                        if (null == queue) {
                            RpcResponse response = new RpcResponse();
                            response.setCode(400);
                        }
                        try {
                            RpcResponse response = queue.take();
                            if (response.getCode() == 200) {
                                return response.getResult();
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        return null;
                    }
                });
    }
}
