package com.zc.ct.producer.io;

import com.zc.ct.common.bean.DataOut;

import java.io.*;

public class LocalFileDataOut implements DataOut {

    private PrintWriter writer = null;

    public LocalFileDataOut(String path){
        setPath(path);
    }
    @Override
    public void setPath(String path) {

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void write(Object date) throws Exception {
        write(date.toString());
    }

    /**
     * 将数据字符串生成到文件中
     * @param date
     * @throws Exception
     */
    @Override
    public void write(String date) throws Exception {
        writer.println(date);
        writer.flush();

    }

    @Override
    public void close() throws IOException {

        if(writer!=null){
            writer.close();
        }
    }
}
