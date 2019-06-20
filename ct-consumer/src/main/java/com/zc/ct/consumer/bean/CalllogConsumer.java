package com.zc.ct.consumer.bean;

import com.zc.ct.common.bean.Consumer;
import com.zc.ct.common.constant.Names;
import com.zc.ct.consumer.dao.HbaseDao;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class CalllogConsumer implements Consumer {
    @Override
    public void consume() {

        try{

            //创建配置对象
            Properties prop = new Properties();

            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer.properties"));

            //获取flume采集的数据
            KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(prop);

            //关注主题
            consumer.subscribe(Arrays.asList(Names.TOPIC.getValue()));

            HbaseDao dao = new HbaseDao();
            dao.init();

            //获取数据
            while(true){
                ConsumerRecords<String,String> consumerRecords = consumer.poll(100);
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.value());
                    dao.insertData(consumerRecord.value());
                    //Calllog log = new Calllog(consumerRecord.value());
                    //dao.insertData(log);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {

    }
}
