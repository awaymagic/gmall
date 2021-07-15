package com.atguigu.gmall.common.auth;

/**
 * Date:2021/7/14
 * Author:away
 * Description:
 */
public class AuthException extends RuntimeException {
    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }
}
