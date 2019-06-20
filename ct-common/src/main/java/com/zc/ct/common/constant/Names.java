package com.zc.ct.common.constant;

import com.zc.ct.common.bean.Val;

/*
名称常量枚举类
 */
public enum  Names implements Val {
    NAMESPACE("ct"),
    TOPIC("test"),
    TABLE("ct:calllog"),
    CF_CALLER("caller"),
    CF_CALLEE("callee"),
    CF_INFO("info");

    private String name;

    private Names( String name ){
        this.name = name;
    }

    @Override
    public void setValue(Object value) {
        this.name = (String) value;
    }

    @Override
    public String getValue() {
        return name;
    }
}
