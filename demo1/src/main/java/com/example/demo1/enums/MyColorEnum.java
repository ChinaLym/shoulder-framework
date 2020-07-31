package com.example.demo1.enums;

import java.io.Serializable;

/**
 * 自定义枚举
 */
public enum MyColorEnum {

    /**
     * 定义了几种颜色
     */
    RED,
    BLUE,
    GREEN,

    ;

}


class SingleTon implements Serializable {
    private static volatile SingleTon singleTon;

    public static SingleTon getInstance(){
        if(singleTon != null){
            return singleTon;
        }
        synchronized (SingleTon.class) {
            if(singleTon == null){
                singleTon = new SingleTon();
                return singleTon;
            }
        }
    }

    private SingleTon(){

    }

    @Override
    public Object clone(){
        return getInstance();
    }

    private Object readResolve(){
        return getInstance();
    }
}