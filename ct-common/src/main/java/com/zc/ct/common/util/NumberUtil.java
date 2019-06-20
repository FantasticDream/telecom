package com.zc.ct.common.util;

import java.text.DecimalFormat;

/**
 * 数字工具类
 */
public class NumberUtil {
    public static String format(int num,int length){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ;i < length ; i++){
            stringBuilder.append("0");
        }
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());
        return df.format(num);
    }
}
