package com.hj.ssyx;

import jdk.internal.org.objectweb.asm.tree.analysis.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HuangJin
 * @date 15:17 2023/12/30
 */

// supplyAsync方法支持返回值
public class CompletableFutureDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        System.out.println("main begin....");
        //CompletableFuture创建异步对象
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程："+Thread.currentThread().getName());
            int value = 1024;
            System.out.println("value:"+value);
            return value;
        }, executorService);
        Integer value = completableFuture.get();
        System.out.println("main over:"+value);
    }
}
