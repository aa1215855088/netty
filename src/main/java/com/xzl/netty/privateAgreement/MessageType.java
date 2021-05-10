package com.xzl.netty.privateAgreement;

/**
 * @author xzl
 * @date 2021-04-25 23:12
 **/
public enum MessageType {

    LOGIN_RESP((byte) 1),
    HEARTBEAT_REQ((byte) 2),
    HEARTBEAT_RESP((byte) 3),
    ;

    private final byte code;

    MessageType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
