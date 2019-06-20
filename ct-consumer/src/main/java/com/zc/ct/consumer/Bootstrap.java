package com.zc.ct.consumer;

import com.zc.ct.common.bean.Consumer;
import com.zc.ct.consumer.bean.CalllogConsumer;

import java.io.IOException;

/**
 * 启动消费者
 *
 *  使用kafka的消费者获取flume采集的数据
 *
 *  将数据存储到hbase
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {

        //创建消费者
        Consumer consumer = new CalllogConsumer();

        //消费数据
        consumer.consume();

        //关闭资源
        consumer.close();

    }
}
