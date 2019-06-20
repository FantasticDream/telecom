package com.zc.ct.consumer.coprocessor;

import com.zc.ct.common.bean.BaseDao;
import com.zc.ct.common.constant.Names;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 使用协处理器保存被叫用户的数据
 */
public class InsertCalleeCoprocessor extends BaseRegionObserver {


    /**
     * 保存主叫用户数据后保存被叫用户数据
     * @param e
     * @param put
     * @param edit
     * @param durability
     * @throws IOException
     */
    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
        Table table = e.getEnvironment().getTable(TableName.valueOf(Names.TABLE.getValue()));

        String rowkey = Bytes.toString(put.getRow());
        String[] values = rowkey.split("_");

        String call1 = values[1];
        String call2 = values[3];
        String calltime = values[2];
        String duration = values[4];
        String flg = values[5];
        if("1".equals(flg)){
            CoprocessorDao dao = new CoprocessorDao();

            String calleerowkey = dao.getRegionNum(call2,calltime)+"_"+call2+"_"+calltime+"_"+call1+"_"+duration+"_0";

            //保存数据
            Put calleeput = new Put(Bytes.toBytes(calleerowkey));

            byte[] calleefamily = Bytes.toBytes(Names.CF_CALLER.getValue());
            calleeput.addColumn(calleefamily,Bytes.toBytes("call1"),Bytes.toBytes(call2));
            calleeput.addColumn(calleefamily,Bytes.toBytes("call2"),Bytes.toBytes(call1));
            calleeput.addColumn(calleefamily,Bytes.toBytes("calltime"),Bytes.toBytes(calltime));
            calleeput.addColumn(calleefamily,Bytes.toBytes("duration"),Bytes.toBytes(duration));
            calleeput.addColumn(calleefamily,Bytes.toBytes("flg"),Bytes.toBytes("0"));

            table.put(calleeput);

            table.close();
        }


    }
    private class CoprocessorDao extends BaseDao{
        public int getRegionNum(String tel,String time){
            return genRegionNum(tel,time);
        }
    }
}
