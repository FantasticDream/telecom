package com.zc.ct.common.bean;

import java.io.Closeable;

public interface DataOut extends Closeable {
    public void setPath(String path);
    public void write(Object date) throws Exception;
    public void write(String date) throws Exception;
}
