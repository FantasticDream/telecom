package com.zc.ct.producer;

import com.zc.ct.common.bean.Producer;
import com.zc.ct.producer.bean.LocalFileProducer;
import com.zc.ct.producer.io.LocalFileDataIn;
import com.zc.ct.producer.io.LocalFileDataOut;

import java.io.IOException;

/*
启动对象
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {

        if(args.length<2){
            System.out.println("系统参数不正确，请按照指定格式传递:java -jar Produce.jar path1 path2");
            System.exit(1);
        }

        //构建生产者对象
        Producer producer = new LocalFileProducer();

        producer.setIn(new LocalFileDataIn(args[0]));
        producer.setOut(new LocalFileDataOut(args[1]));
        //producer.setIn(new LocalFileDataIn("D:\\数据集\\mytest\\contact.log"));
        //producer.setOut(new LocalFileDataOut("D:\\数据集\\mytest\\call.log"));

        //生产数据
        producer.produce();

        //关闭生产者对象
        producer.close();

    }
}
