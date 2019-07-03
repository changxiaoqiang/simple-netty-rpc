package com.qiang.rpc.container;

import com.qiang.rpc.services.RpcService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcContainer {
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public static void register(ApplicationContext context) {
        /*获取所有带@RpcService注解的Spring Bean*/
        Map<String, Object> serviceBeanMap = context.getBeansWithAnnotation(RpcService.class);
        if (null != serviceBeanMap && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcServiceAnnotation = serviceBean.getClass().getAnnotation(RpcService.class);
                String name = !StringUtils.isEmpty(rpcServiceAnnotation.name()) ? rpcServiceAnnotation.name() : rpcServiceAnnotation.value().getName();
                serviceMap.put(name, serviceBean);
            }
        }
    }

    public static Object getService(String name) {
        return serviceMap.get(name);
    }
}
