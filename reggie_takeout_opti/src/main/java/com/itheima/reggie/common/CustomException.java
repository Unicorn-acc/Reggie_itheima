package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 * @author MiracloW
 * @date 2022-10-12 12:02
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
