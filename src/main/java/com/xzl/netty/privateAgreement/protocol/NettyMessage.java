package com.xzl.netty.privateAgreement.protocol;

import lombok.Data;

/**
 * @author xzl
 * @date 2021-04-25 22:01
 **/
@Data
public class NettyMessage {
    private Header header;
    private Object body;
}
