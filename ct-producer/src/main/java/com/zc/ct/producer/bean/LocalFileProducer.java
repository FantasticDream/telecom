package com.zc.ct.producer.bean;

import com.zc.ct.common.bean.DataIn;
import com.zc.ct.common.bean.DataOut;
import com.zc.ct.common.bean.Producer;
import com.zc.ct.common.util.DateUtil;
import com.zc.ct.common.util.NumberUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*
本地数据文件的生产者
 */
public class LocalFileProducer implements Producer {
    private DataIn in;
    private DataOut out;
    public volatile boolean flag = true;
    @Override
    public void setIn(DataIn in) {
        this.in = in;
    }

    @Override
    public void setOut(DataOut out) {
        this.out = out;
    }

    /*
    生产数据
     */
    @Override
    public void produce() {

        try {
        //读取通讯录数据
            List<Contact> contacts = in.read(Contact.class);

            while(flag){

                //从通讯录中随机查找两个电话号码
                int call1Index = new Random().nextInt(contacts.size());
                int call2Index;

                while (true){
                    call2Index = new Random().nextInt(contacts.size());
                    if(call2Index != call1Index){
                        break;
                    }
                }

                Contact call1 = contacts.get(call1Index);
                Contact call2 = contacts.get(call2Index);

                //生成随机的通话时间
                String startDate = "20180101000000";
                String endDate = "20190100000000";

                long startTime = DateUtil.parse(startDate,"yyyyMMddHHmmss").getTime();
                long endTime = DateUtil.parse(endDate,"yyyyMMddHHmmss").getTime();

                long callTime = startTime + (long)((endTime - startTime) * Math.random());
                //通话时间字符串
                String callTimeString = DateUtil.format(new Date(callTime),"yyyyMMddHHmmss");

                //生成随机的通话时长
                String duration = NumberUtil.format(new Random().nextInt(3000),4);

                //生成通话记录
                Calllog calllog = new Calllog(call1.getTel(),call2.getTel(),callTimeString,duration);

                System.out.println(calllog);
                //将通话记录刷取到数据文件中
                out.write(calllog);

                Thread.sleep(500);

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

        if( in!=null ){
            in.close();
        }
        if(out != null){
            out.close();
        }
    }
}
