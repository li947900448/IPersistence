package com.lagou.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2020/5/5.
 */
public class BaseDataType {
    private static final List<String> baseType = new ArrayList<>();

    static{
        baseType.add("java.lang.String");
        baseType.add("java.lang.Integer");
        baseType.add("java.lang.Double");
    }

    public static List<String> getBaseType() {
        return baseType;
    }
}
