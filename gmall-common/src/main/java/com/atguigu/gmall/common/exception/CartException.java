package com.atguigu.gmall.common.exception;

/**
 * Date:2021/7/16
 * Author:away
 * Description:
 */
public class CartException extends RuntimeException {
    public CartException() {
    }

    public CartException(String message) {
        super(message);
    }
}
