package com.qiang.rpc.zk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ServiceRegistry {
    final static Logger logger = LogManager.getLogger(ServiceRegistry.class);

    /*计数器*/
    private CountDownLatch latch = new CountDownLatch(1);

    /*注册地址*/
    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data){
        if(null != data){
            /*连接zk服务*/
            ZooKeeper zk = connectServer();
            if(null != zk){
                /*创建zk节点*/
                createNode(zk, data);
            }
        }
    }

    /**
     * 连接服务
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, 12000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    // 判断是否已连接ZK,连接后计数器递减.
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            // 若计数器不为0,则等待.
            latch.await();
            if (null == zk.exists("/registry", false)) {
                zk.create("/registry", "[]".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.debug("create zookeeper node registry");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("连接zk服务报错: {}", e.getMessage());
        }
        return zk;
    }

    /**
     * 创建zk临时节点
     * @param zk
     * @param data
     */
    private void createNode(ZooKeeper zk, String data){
        try {
            byte[] bytes = data.getBytes();
            // 创建 registry 节点（临时）
            String path = zk.create("/registry/data", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.debug("create zookeeper node: ({} => {})", path, data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("创建zk节点出错：{}", e.getMessage());
        }
    }
}
