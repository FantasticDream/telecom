package com.zc.ct.consumer.dao;

import com.zc.ct.common.bean.BaseDao;
import com.zc.ct.common.constant.Names;
import com.zc.ct.common.constant.ValueConstant;
import com.zc.ct.consumer.bean.Calllog;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

public class HbaseDao extends BaseDao {
    /**
     * 初始化
     */
    public void init()throws Exception{
        start();

        createNamespaceNX(Names.NAMESPACE.getValue());
        //createTableXX(Names.TABLE.getValue(), "com.zc.ct.consumer.coprocessor.InsertCalleeCoprocessor",ValueConstant.REGION_COUNT,Names.CF_CALLER.getValue());
        createTableXX(Names.TABLE.getValue(), null,ValueConstant.REGION_COUNT,Names.CF_CALLER.getValue());

        end();

    }

    /**
     * 插入对象
     */
    public void insertData(Calllog log)throws Exception{
        log.setRowkey(genRegionNum(log.getCall1(),log.getCalltime())+"_"+log.getCall1()+"_"+log.getCalltime()+"_"+log.getCall2()+"_"+log.getDuration());
        putData(log);
    }



    /**
     * 插入数据
     */
    public void insertData(String value)throws Exception{

        //将通话日志保存到Hbase表中

        //1.获取通话日志

        String[] values = value.split("\t");
        String call1 = values[0];
        String call2 = values[1];
        String calltime = values[2];
        String duration = values[3];

        String rowkey = genRegionNum(call1,calltime)+"_"+call1+"_"+calltime+"_"+call2+"_"+duration+"_1";

        //2.创建数据对象
        Put put = new Put(Bytes.toBytes(rowkey));

        byte[] family = Bytes.toBytes(Names.CF_CALLER.getValue());

        put.addColumn(family,Bytes.toBytes("call1"),Bytes.toBytes(call1));
        put.addColumn(family,Bytes.toBytes("call2"),Bytes.toBytes(call2));
        put.addColumn(family,Bytes.toBytes("calltime"),Bytes.toBytes(calltime));
        put.addColumn(family,Bytes.toBytes("duration"),Bytes.toBytes(duration));
        put.addColumn(family,Bytes.toBytes("flg"),Bytes.toBytes("1"));

//        String calleerowkey = genRegionNum(call2,calltime)+"_"+call2+"_"+calltime+"_"+call1+"_"+duration+"_0";

//        //2.创建数据对象
//        Put calleeput = new Put(Bytes.toBytes(calleerowkey));
//
//        byte[] calleefamily = Bytes.toBytes(Names.CF_CALLEE.getValue());
//
//        calleeput.addColumn(calleefamily,Bytes.toBytes("call1"),Bytes.toBytes(call2));
//        calleeput.addColumn(calleefamily,Bytes.toBytes("call2"),Bytes.toBytes(call1));
//        calleeput.addColumn(calleefamily,Bytes.toBytes("calltime"),Bytes.toBytes(calltime));
//        calleeput.addColumn(calleefamily,Bytes.toBytes("duration"),Bytes.toBytes(duration));
//        calleeput.addColumn(calleefamily,Bytes.toBytes("flg"),Bytes.toBytes("0"));
//
//        List<Put> list = new ArrayList<Put>();
//        list.add(put);
//        list.add(calleeput);
//
//        putData(Names.TABLE.getValue(),list);
        putData(Names.TABLE.getValue(),put);
    }
}
