package com.xzl.netty;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author xzl
 * @date 2021-04-29 20:49
 **/
public class TimeWheel {
    public static void main(String[] args) {
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(1, TimeUnit.SECONDS, 16);
        hashedWheelTimer.newTimeout(timeout -> {
            TimeUnit.SECONDS.sleep(10);
            System.out.println(LocalDateTime.now().toString());
        },1,TimeUnit.SECONDS);
        hashedWheelTimer.newTimeout(timeout -> System.out.println(LocalDateTime.now().toString()),2,TimeUnit.SECONDS);
        hashedWheelTimer.newTimeout(timeout -> System.out.println(LocalDateTime.now().toString()),10,TimeUnit.SECONDS);
    }
}
