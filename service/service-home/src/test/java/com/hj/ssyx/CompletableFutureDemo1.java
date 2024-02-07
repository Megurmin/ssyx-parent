package com.hj.ssyx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HuangJin
 * @date 15:17 2023/12/30
 */

// runAsync方法不支持返回值
public class CompletableFutureDemo1 {

    public static void main(String[] args) {
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        System.out.println("main begin....");
        //CompletableFuture创建异步对象
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程："+Thread.currentThread().getName());
            int result = 1024;
            System.out.println("result:"+result);
        }, executorService);
        System.out.println("main over....");
    }
}
