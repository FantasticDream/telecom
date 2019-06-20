package com.zc.ct.common.bean;

import com.zc.ct.common.api.Column;
import com.zc.ct.common.api.Rowkey;
import com.zc.ct.common.api.TableRef;
import com.zc.ct.common.constant.Names;
import com.zc.ct.common.constant.ValueConstant;
import com.zc.ct.common.util.DateUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * 基础数据访问对象
 */
public abstract class BaseDao {

    private ThreadLocal<Connection> connHolder = new ThreadLocal<Connection>();
    private ThreadLocal<Admin> adminHolder = new ThreadLocal<Admin>();


    protected void start() throws Exception{
        getConnection();
        getAdmin();
    }

    protected void end() throws Exception{
        Admin admin = getAdmin();
        if(admin!=null){
            admin.close();
            adminHolder.remove();
        }
        Connection conn = getConnection();
        if(conn!=null){
            conn.close();
            connHolder.remove();
        }
    }

    /**
     * 创建表，如果表已经存在，那么删除创建新的
     * @param name
     * @param families
     */
    protected void createTableXX(String name,String... families)throws Exception{
        createTableXX(name,null,null,families);
    }
    protected void createTableXX(String name,String coprocessorClass,Integer regionCount,String... families)throws Exception{
        Admin admin = getAdmin();

        TableName tableName = TableName.valueOf(name);

        if(admin.tableExists(tableName)){
            deleteTable(name);
        }
        createTable(name,coprocessorClass,regionCount,families);

    }

    private void createTable(String name,String coprocessorClass,Integer regionCount,String... families) throws Exception{
        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        HTableDescriptor tableDescriptor =
                new HTableDescriptor(tableName);

        if(families == null|| families.length == 0){
            families = new String[1];
            families[0] = Names.CF_INFO.getValue();
        }
        for (String family : families) {
            HColumnDescriptor columnDescriptor =
                    new HColumnDescriptor(family);
            tableDescriptor.addFamily(columnDescriptor);
        }
        if(coprocessorClass != null && !"".equals(coprocessorClass)){
            tableDescriptor.addCoprocessor(coprocessorClass);
        }

        //增加预分区
        if(regionCount == null || regionCount <=0){
            admin.createTable(tableDescriptor);
        }else{
            //分区键
            byte[][] splitKeys = genSplitKeys(regionCount);
            admin.createTable(tableDescriptor,splitKeys);
        }

    }

    /**
     * 获取查询时startRow和stopRow的集合
     */
    protected List<String[]> genStartStopRowKeys(String tel,String start,String end){
        List<String[]> rowkeyss = new ArrayList<String[]>();
        String startTime = start.substring(0,6);
        String endTime = end.substring(0,6);

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(DateUtil.parse(startTime,"yyyyMM"));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(DateUtil.parse(endTime,"yyyyMM"));

        while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()){
            String nowTime = DateUtil.format(startCal.getTime(),"yyyyMM");

            int regionNum = genRegionNum(tel,nowTime);

            String startRow = regionNum + "_" + tel + "_" + nowTime;
            String stopRow =startRow + "|";

            String[] rowkeys = {startRow,stopRow};
            rowkeyss.add(rowkeys);

            startCal.add(Calendar.MONTH,1);
        }

        return rowkeyss;
    }

    /**
     * 生成分区号
     */
    protected  int genRegionNum(String tel,String date){

        String usercode = tel.substring(tel.length()-4);
        String yearMonth = date.substring(0,6);
        int userCodeHash = usercode.hashCode();
        int yearMonthhash = yearMonth.hashCode();

        int crc = Math.abs(userCodeHash^yearMonthhash);

        int regionNum = crc % ValueConstant.REGION_COUNT;

        return regionNum;
    }


    /**
     * 生成分区键
     * @return
     */
    private byte[][] genSplitKeys(int regionCount){

        int splitkeyCount = regionCount - 1;
        byte[][] bs = new byte[splitkeyCount][];
        List<byte[]> bsList = new ArrayList<byte[]>();
        for(int i = 0;i < splitkeyCount;i++){
            String splitkey = i + "|";
            bsList.add(Bytes.toBytes(splitkey));
        }

        //Collections.sort(bsList,Bytes.ByteArrayComparator());

        bsList.toArray(bs);

        return bs;

    }

    /**
     * 增加对象，将对象数据直接保存
     * @param obj
     */
    protected void putData(Object obj) throws Exception{

        //反射
        Class clazz = obj.getClass();
        TableRef tableRef =  (TableRef) clazz.getAnnotation(TableRef.class);
        String tableName = tableRef.value();

        Field[] fs = clazz.getDeclaredFields();
        String stringrowkey = "";
        for (Field f : fs) {
            Rowkey rowkey = f.getAnnotation(Rowkey.class);
            if(rowkey != null){
                f.setAccessible(true);
                stringrowkey = (String) f.get(obj);
                break;
            }
        }

        //获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(stringrowkey));

        for (Field f : fs) {
            Column column = f.getAnnotation(Column.class);
            if(column != null){
                String family = column.family();
                String colName = column.column();
                if(colName ==null && "".equals(colName)){
                    colName = f.getName();
                }
                f.setAccessible(true);
                String value = (String) f.get(obj);
                put.addColumn(Bytes.toBytes(family),Bytes.toBytes(colName),Bytes.toBytes(value));
            }
        }

        //增加数据
        table.put(put);

        //关闭表
        table.close();
    }

    /**
     * 插入一组数据
     * @param name
     * @param put
     * @throws Exception
     */
    protected void putData(String name, List<Put> put)throws Exception{

        //获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        //增加数据
        table.put(put);

        //关闭表
        table.close();
    }


    protected void putData(String name, Put put)throws Exception{

        //获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));

        //增加数据
        table.put(put);

        //关闭表
        table.close();
    }

    /**
     * 删除表格
     * @param name
     * @throws Exception
     */
    protected void deleteTable(String name)throws Exception{
        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    /**
     *  创建命名空间
     */
    protected void createNamespaceNX(String namespace)throws Exception{
        Admin admin = getAdmin();
        try{
            admin.getNamespaceDescriptor(namespace);
        }catch (NamespaceExistException e){
            NamespaceDescriptor namespaceDescriptor =
                    NamespaceDescriptor.create(namespace).build();
            admin.createNamespace(namespaceDescriptor);
        }

    }

    /**
     * 获取连接对象
     * @return
     * @throws Exception
     */

    protected synchronized Connection getConnection()throws Exception{
        Connection conn = connHolder.get();
        if(conn == null){
            Configuration conf = HBaseConfiguration.create();
            conn = ConnectionFactory.createConnection(conf);
            connHolder.set(conn);
            //System.out.println(conn.getConfiguration().toString());
        }
        return conn;
      }

    /**
     * 获取管理对象
     * @return
     * @throws Exception
     */

    protected synchronized Admin getAdmin()throws Exception{
        Admin admin = adminHolder.get();
        if(admin == null){
            admin = getConnection().getAdmin();
            adminHolder.set(admin);
        }
        return admin;
    }
}
