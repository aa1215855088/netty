package com.xzl.netty.serialization;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xzl
 * @date 2021-04-18 14:18
 **/
@Data
public class Book implements Serializable {

    private String name;

    private String author;

    private double price;

    private LocalDateTime createTime;


}
