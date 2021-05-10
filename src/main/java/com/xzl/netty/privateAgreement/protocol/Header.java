package com.xzl.netty.privateAgreement.protocol;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xzl
 * @date 2021-04-25 22:01
 **/
@Data
public class Header {
    /**
     * Netty消息的校验码，它由三部分组成：
     * 1，0xABEF：固定值，表明该消息是Netty协议消息，2字符；
     * 2，主版本号：1～255，1个字节；
     * 3，次版本号：1～255，1个字节。
     * crcCode=0xABEF+主版本号+次版本号
     */
    private int crcCode = 0xabef0101;

    /**
     * 消息长度，整个消息，包括消息头和消息体
     */
    private int length;

    /**
     * 集群节点内全局唯一，由会话ID生成器生成
     */
    private long sessionID;

    /**
     * 0:业务消息；
     * 1：业务响应消息；
     * 2：业务ONE WAY消息（即是请求也是响应消息）
     * 3：握手请求消息；
     * 4：握手应答消息：
     * 5：心跳请求消息；
     * 6：心跳应答消息。
     */
    private byte type;

    /**
     * 消息优先级：0～255
     */
    private byte priority;

    /**
     * 可选字段，用于扩展消息头
     */
    private Map<String, Object> attachment = new HashMap<>();
}
