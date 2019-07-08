package com.qiang.rpc.zk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class ServiceDiscovery {

    final static Logger logger = LogManager.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    /*注册地址*/
    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        /*连接zk服务*/
        ZooKeeper zk = connectServer();
        if(null != zk){
            /*监视zk节点*/
            watchNode(zk);
        }
    }

    /**
     * 服务发现
     * @return
     */
    public String discovery(){
        String data = null;
        int size = dataList.size();
        if(size > 0){
            if(size == 1){
                // 若只有一个地址，则获取该地址
                data = dataList.get(0);
                logger.debug("using only data: {}", data);
            }else {
                // 若存在多个地址，则随机获取一个地址
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                logger.debug("using random data: {}", data);
            }
        }
        return data;
    }

    /**
     * 连接zk服务
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            /*创建zk客户端*/
            zk = new ZooKeeper(registryAddress, 12000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("连接zk服务报错...", e);
        }
        return zk;
    }

    /**
     * 监视zk节点
     * @param zk
     */
    private void watchNode(final ZooKeeper zk){
        try {
            List<String> nodeList = zk.getChildren("/registry", new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                        watchNode(zk);
                    }
                }
            });

            List<String> dataList = new ArrayList<>();
            byte[] bytes;
            for (String node : nodeList){
                bytes = zk.getData("/registry/" + node, false, null);
                dataList.add(new String(bytes));
            }
            logger.debug("node data: {}", dataList);
            this.dataList = dataList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("监视zk节点异常...", e);
        }
    }
}
